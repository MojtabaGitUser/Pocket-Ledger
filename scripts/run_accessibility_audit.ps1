[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$DeviceSerial,
    [string]$AndroidUserId = "",
    [string]$PackageName = "com.mojtaba.folentra",
    [string]$BuildVariant = "benchmark",
    [ValidateSet("Compact", "Medium", "Expanded")]
    [string]$LayoutDeviceClass = "Compact",
    [string]$ApkPath = "",
    [string]$Reviewer = "",
    [string]$EvidenceDirectory = "androidApp/accessibility-audit-evidence"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$evidence = Join-Path $repoRoot (Join-Path $EvidenceDirectory $runId)
$screens = @(
    "Dashboard",
    "Transactions list and detail",
    "Transaction editor",
    "Search filters and results",
    "Budget setup",
    "Settings",
    "App lock"
)

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        # Native stderr must be captured so PowerShell does not terminate before we can report
        # the adb command that failed.
        $ErrorActionPreference = "Continue"
        $output = & adb -s $DeviceSerial @Arguments 2>&1
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $joinedOutput = $output -join "`n"
    if ($exitCode -ne 0 -or $joinedOutput -match '(?m)^(Error: java\.|java\.lang\.(SecurityException|IllegalArgumentException))') {
        throw "adb failed: adb -s $DeviceSerial $($Arguments -join ' ')`n$joinedOutput"
    }
    return $output
}

function Read-AuditResult {
    param([string]$Prompt)
    while ($true) {
        $answer = (Read-Host "$Prompt [Pass/Fail/Blocked]").Trim()
        switch -Regex ($answer) {
            '^(?i:pass|p)$' { return "Pass" }
            '^(?i:fail|f)$' { return "Fail" }
            '^(?i:blocked|b)$' { return "Blocked" }
            default { Write-Warning "Enter Pass, Fail, or Blocked." }
        }
    }
}

function Save-DeviceScreenshot {
    param([Parameter(Mandatory = $true)][string]$Destination)

    for ($attempt = 1; $attempt -le 3; $attempt++) {
        $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
        $startInfo.FileName = "adb"
        $startInfo.Arguments = "-s `"$DeviceSerial`" exec-out screencap -p"
        $startInfo.UseShellExecute = $false
        $startInfo.CreateNoWindow = $true
        $startInfo.RedirectStandardOutput = $true
        $startInfo.RedirectStandardError = $true
        $process = [System.Diagnostics.Process]::new()
        $process.StartInfo = $startInfo
        $file = $null
        try {
            if (-not $process.Start()) { throw "Could not start adb screencap." }
            $errorRead = $process.StandardError.ReadToEndAsync()
            $file = [System.IO.File]::Open(
                $Destination,
                [System.IO.FileMode]::Create,
                [System.IO.FileAccess]::Write,
                [System.IO.FileShare]::None
            )
            $process.StandardOutput.BaseStream.CopyTo($file)
            $file.Flush()
            $file.Dispose()
            $file = $null
            $process.WaitForExit()
            $errorText = $errorRead.GetAwaiter().GetResult()
            if ($process.ExitCode -eq 0 -and (Get-Item $Destination).Length -gt 0) { return }
        } finally {
            if ($null -ne $file) { $file.Dispose() }
            $process.Dispose()
        }
        if (Test-Path $Destination -PathType Leaf) { Remove-Item -LiteralPath $Destination -Force }
        if ($attempt -lt 3) { Start-Sleep -Milliseconds 500 }
    }
    throw "Direct screenshot capture failed after three attempts. Last adb error: $errorText"
}

$devices = & adb devices
$devicePattern = "^$([regex]::Escape($DeviceSerial))\s+device\s*$"
if ($LASTEXITCODE -ne 0 -or @($devices | Where-Object { $_ -match $devicePattern }).Count -eq 0) {
    throw "Device '$DeviceSerial' is not connected and authorized. Run 'adb devices' first."
}

if ([string]::IsNullOrWhiteSpace($AndroidUserId)) {
    $AndroidUserId = ((Invoke-Adb shell am get-current-user) -join "").Trim()
}
if ($AndroidUserId -notmatch '^\d+$') {
    throw "Android user id must be numeric; received '$AndroidUserId'."
}

if (-not [string]::IsNullOrWhiteSpace($ApkPath)) {
    $resolvedApk = (Resolve-Path $ApkPath -ErrorAction Stop).Path
    Write-Host "[Install audit artifact]"
    & adb -s $DeviceSerial install --user $AndroidUserId -r $resolvedApk
    if ($LASTEXITCODE -ne 0) {
        throw "APK installation failed. A signature mismatch requires a separate test device or explicit uninstall; this script never deletes app data."
    }
} else {
    $resolvedApk = $null
}

$installedPackages = Invoke-Adb shell pm list packages --user $AndroidUserId $PackageName
if (($installedPackages -join "`n") -notmatch "package:$([regex]::Escape($PackageName))$") {
    throw "Package '$PackageName' is not installed on device '$DeviceSerial'."
}

$enabledServices = ((Invoke-Adb shell settings --user $AndroidUserId get secure enabled_accessibility_services) -join "").Trim()
$accessibilityEnabled = ((Invoke-Adb shell settings --user $AndroidUserId get secure accessibility_enabled) -join "").Trim()
$talkBackComponent = ($enabledServices -split ':') |
    Where-Object { $_ -match '(?i)talkback|marvin' } |
    Select-Object -First 1
if ($accessibilityEnabled -ne "1" -or [string]::IsNullOrWhiteSpace($talkBackComponent)) {
    throw "TalkBack is not enabled. Enable it in Android Accessibility settings, then rerun the audit."
}

$talkBackPackage = ($talkBackComponent -split '/')[0]
$talkBackVersion = (Invoke-Adb shell dumpsys package $talkBackPackage |
    Where-Object { $_ -match 'versionName=' } |
    Select-Object -First 1).Trim()
if ([string]::IsNullOrWhiteSpace($talkBackVersion)) { $talkBackVersion = "Unknown ($talkBackPackage)" }
if ([string]::IsNullOrWhiteSpace($Reviewer)) {
    $Reviewer = (Read-Host "Reviewer name").Trim()
}
if ([string]::IsNullOrWhiteSpace($Reviewer)) { throw "Reviewer is required for an auditable QA record." }

New-Item -ItemType Directory -Path $evidence -Force | Out-Null
$deviceModel = ((Invoke-Adb shell getprop ro.product.model) -join "").Trim()
$androidVersion = ((Invoke-Adb shell getprop ro.build.version.release) -join "").Trim()
$apiLevel = ((Invoke-Adb shell getprop ro.build.version.sdk) -join "").Trim()
$fontScale = ((Invoke-Adb shell settings --user $AndroidUserId get system font_scale) -join "").Trim()
$versionName = (Invoke-Adb shell dumpsys package $PackageName |
    Where-Object { $_ -match 'versionName=' } |
    Select-Object -First 1).Trim()
$gitCommit = (git -C $repoRoot rev-parse HEAD).Trim()
$results = @()

Invoke-Adb shell am force-stop --user $AndroidUserId $PackageName | Out-Null
Invoke-Adb shell am start --user $AndroidUserId -n "$PackageName/com.mojtaba.folentra.MainActivity" | Out-Null

Write-Host ""
Write-Host "Manual accessibility audit for $PackageName"
Write-Host "TalkBack: $talkBackVersion"
Write-Host "For each screen: navigate forward and backward, activate every primary action, then repeat with Tab/D-pad."
Write-Host "Do not enter personal financial data. Evidence is stored at: $evidence"

for ($index = 0; $index -lt $screens.Count; $index++) {
    $screen = $screens[$index]
    $slug = ($screen.ToLowerInvariant() -replace '[^a-z0-9]+', '-').Trim('-')
    Read-Host "[$($index + 1)/$($screens.Count)] Open '$screen', complete both passes, then press Enter to capture evidence"

    $focusedApp = (Invoke-Adb shell dumpsys window |
        Where-Object { $_ -match 'mFocusedApp=' } |
        Select-Object -First 1) -join ""
    if ($focusedApp -notmatch "\s$([regex]::Escape($PackageName))/") {
        throw "The focused app is not '$PackageName'. Return to the requested Folentra build and rerun this audit. Current focus: $focusedApp"
    }

    # Stream screenshot bytes directly to the host. Some Samsung multi-user builds return success
    # before a remote screencap file is visible, making shell-side temporary screenshots unreliable.
    $localScreenshot = Join-Path $evidence "$slug.png"
    $remoteHierarchy = "/data/local/tmp/folentra-accessibility-$slug.xml"
    Save-DeviceScreenshot -Destination $localScreenshot

    Invoke-Adb shell uiautomator dump $remoteHierarchy | Out-Null
    Invoke-Adb shell test -s $remoteHierarchy | Out-Null
    Invoke-Adb pull $remoteHierarchy (Join-Path $evidence "$slug.xml") | Out-Null
    Invoke-Adb shell rm $remoteHierarchy | Out-Null

    $talkBack = Read-AuditResult "TalkBack forward/backward traversal, spoken labels/states, and activation"
    $keyboard = Read-AuditResult "Keyboard/D-pad forward/backward traversal, visible focus, and activation"
    $notes = (Read-Host "Notes or follow-up issue (Enter for none)").Trim()
    $results += [pscustomobject]@{
        Screen = $screen
        TalkBack = $talkBack
        KeyboardDpad = $keyboard
        Notes = $notes
    }
}

$overallResult = if (@($results | Where-Object { $_.TalkBack -ne 'Pass' -or $_.KeyboardDpad -ne 'Pass' }).Count -eq 0) {
    "Pass"
} elseif (@($results | Where-Object { $_.TalkBack -eq 'Fail' -or $_.KeyboardDpad -eq 'Fail' }).Count -gt 0) {
    "Fail"
} else {
    "Blocked"
}

$metadata = [ordered]@{
    runId = $runId
    gitCommit = $gitCommit
    packageName = $PackageName
    buildVariant = $BuildVariant
    layoutDeviceClass = $LayoutDeviceClass
    artifact = $resolvedApk
    artifactSha256 = if ($resolvedApk) { (Get-FileHash $resolvedApk -Algorithm SHA256).Hash } else { $null }
    appVersion = $versionName
    deviceSerial = $DeviceSerial
    androidUserId = $AndroidUserId
    deviceModel = $deviceModel
    androidVersion = $androidVersion
    apiLevel = $apiLevel
    talkBackComponent = $talkBackComponent
    talkBackVersion = $talkBackVersion
    fontScale = $fontScale
    reviewer = $Reviewer
    date = (Get-Date).ToString("yyyy-MM-dd")
    result = $overallResult
    screens = $results
}
$metadata | ConvertTo-Json -Depth 5 | Set-Content -Path (Join-Path $evidence "result.json") -Encoding utf8

$tableRows = $results | ForEach-Object {
    $safeNotes = $_.Notes -replace '\|', '\|'
    "| $($_.Screen) | $($_.TalkBack) | $($_.KeyboardDpad) | $safeNotes |"
}
$record = @"
# Primary-screen accessibility audit — $runId

| Field | Value |
| --- | --- |
| Build variant and commit | $BuildVariant / ``$gitCommit`` |
| App package and version | ``$PackageName`` / $versionName |
| Artifact | $(if ($resolvedApk) { "``$resolvedApk`` (SHA-256: ``$($metadata.artifactSha256)``)" } else { "Preinstalled package; no local artifact supplied" }) |
| Device and Android version | $deviceModel ($DeviceSerial), Android $androidVersion (API $apiLevel) |
| Android user | $AndroidUserId |
| TalkBack version | $talkBackVersion |
| Font scale | $fontScale |
| Layout/device class | $LayoutDeviceClass |
| Reviewer | $Reviewer |
| Date | $((Get-Date).ToString("yyyy-MM-dd")) |
| Overall result | **$overallResult** |

| Screen | TalkBack | Keyboard/D-pad | Notes/follow-up |
| --- | --- | --- | --- |
$($tableRows -join "`n")

Evidence in this directory contains a screenshot and UI hierarchy for every reviewed screen. The
Pass/Fail values are the named reviewer's manual attestation; screenshots and hierarchy files do not
prove spoken output by themselves.
"@
$record | Set-Content -Path (Join-Path $evidence "qa-record.md") -Encoding utf8

Write-Host "Accessibility audit result: $overallResult"
Write-Host "QA record: $(Join-Path $evidence 'qa-record.md')"
if ($overallResult -ne "Pass") { exit 1 }
