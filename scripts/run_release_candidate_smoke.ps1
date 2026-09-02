[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$DeviceSerial,
    [string]$EvidenceDirectory = "androidApp/release-smoke-evidence",
    [switch]$SkipBuild,
    [switch]$ReplaceExistingPackage
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repoRoot "androidApp"
$gradle = Join-Path $androidRoot "gradlew.bat"
$apk = Join-Path $androidRoot "app/build/outputs/apk/benchmark/app-benchmark.apk"
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$evidence = Join-Path $repoRoot (Join-Path $EvidenceDirectory $runId)
$testClass = "com.mojtaba.folentra.macrobenchmark.ReleaseCandidateSmokeTest"

function Invoke-Checked {
    param([string]$Label, [scriptblock]$Command)
    Write-Host "[$Label]"
    & $Command
    if ($LASTEXITCODE -ne 0) { throw "$Label failed with exit code $LASTEXITCODE" }
}

$devices = & adb devices
$adbDevicesExitCode = $LASTEXITCODE
$devicePattern = "^$([regex]::Escape($DeviceSerial))\s+device\s*$"
$isConnected = @($devices | Where-Object { $_ -match $devicePattern }).Count -gt 0
if ($adbDevicesExitCode -ne 0 -or -not $isConnected) {
    throw "Device '$DeviceSerial' is not connected and authorized. Run 'adb devices' first."
}

New-Item -ItemType Directory -Path $evidence -Force | Out-Null
$deviceDescription = (& adb -s $DeviceSerial shell getprop ro.product.model).Trim()
$androidVersion = (& adb -s $DeviceSerial shell getprop ro.build.version.release).Trim()
$apiLevel = (& adb -s $DeviceSerial shell getprop ro.build.version.sdk).Trim()
& adb -s $DeviceSerial shell input keyevent KEYCODE_WAKEUP | Out-Null
& adb -s $DeviceSerial shell wm dismiss-keyguard | Out-Null
$currentUserTrust = & adb -s $DeviceSerial shell dumpsys trust |
    Where-Object { $_ -match "\(current\).*deviceLocked=" } |
    Select-Object -First 1
if ($currentUserTrust -match "deviceLocked=1") {
    throw "Device '$DeviceSerial' is locked. Unlock it, keep its screen on, and run this command again."
}
$originalStayAwake = (& adb -s $DeviceSerial shell settings get global stay_on_while_plugged_in).Trim()
Invoke-Checked "Keep screen awake while connected by USB" {
    & adb -s $DeviceSerial shell settings put global stay_on_while_plugged_in 2
}

Push-Location $androidRoot
try {
    if (-not $SkipBuild) {
        Invoke-Checked "Build minified candidate and smoke test" {
            & $gradle :app:assembleBenchmark :macrobenchmark:assemble :app:validateBackupAndDeviceTransferRules --console=plain
        }
    }
    if (-not (Test-Path $apk -PathType Leaf)) { throw "Candidate APK not found: $apk" }

    $previousErrorActionPreference = $ErrorActionPreference
    try {
        # A signature mismatch is an expected, actionable install result. Capture stderr instead of
        # letting PowerShell turn adb's non-zero native exit into a terminating NativeCommandError.
        $ErrorActionPreference = "Continue"
        $installOutput = & adb -s $DeviceSerial install -r $apk 2>&1
        $installExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $installOutput | Write-Host
    if ($installExitCode -ne 0 -and ($installOutput -join "`n") -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE") {
        if (-not $ReplaceExistingPackage) {
            throw @"
The installed com.mojtaba.folentra app has a different signing certificate (usually the Play build).
Uninstalling it deletes its local app data. Re-run with -ReplaceExistingPackage only if that data may
be erased, or run the smoke test on a separate test device/emulator.
"@
        }
        Write-Warning "Removing the differently signed Folentra installation and ALL of its local app data."
        Invoke-Checked "Uninstall differently signed package" {
            & adb -s $DeviceSerial uninstall com.mojtaba.folentra
        }
        Invoke-Checked "Install candidate after approved replacement" {
            & adb -s $DeviceSerial install $apk
        }
    } elseif ($installExitCode -ne 0) {
        throw "Install candidate failed with exit code $installExitCode"
    }
    & adb -s $DeviceSerial logcat -c

    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $testOutput = & $gradle :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest `
            "-Pandroid.testInstrumentationRunnerArguments.class=$testClass" `
            "-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR" `
            --console=plain 2>&1
        $testExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $testOutput | Tee-Object -FilePath (Join-Path $evidence "instrumentation.txt")
    if ($testExitCode -ne 0 -or ($testOutput -join "`n") -match "FAILURES!!!|Tests failed") {
        $remoteScreenshot = "/sdcard/folentra-smoke-failure.png"
        $remoteHierarchy = "/sdcard/folentra-smoke-failure.xml"
        & adb -s $DeviceSerial shell screencap -p $remoteScreenshot | Out-Null
        & adb -s $DeviceSerial shell uiautomator dump $remoteHierarchy | Out-Null
        & adb -s $DeviceSerial pull $remoteScreenshot (Join-Path $evidence "failure.png") | Out-Null
        & adb -s $DeviceSerial pull $remoteHierarchy (Join-Path $evidence "failure-hierarchy.xml") | Out-Null
        & adb -s $DeviceSerial shell rm $remoteScreenshot $remoteHierarchy | Out-Null
        throw "Release-candidate instrumentation smoke failed."
    }

    & adb -s $DeviceSerial logcat -d -v threadtime | Set-Content -Path (Join-Path $evidence "logcat.txt")
    $fatal = Select-String -Path (Join-Path $evidence "logcat.txt") -Pattern "FATAL EXCEPTION|AndroidRuntime: Process: com.mojtaba.folentra"
    if ($fatal) { throw "A fatal application exception was found in logcat." }

    $metadata = [ordered]@{
        runId = $runId
        gitCommit = (git -C $repoRoot rev-parse HEAD).Trim()
        deviceSerial = $DeviceSerial
        deviceModel = $deviceDescription
        androidVersion = $androidVersion
        apiLevel = $apiLevel
        artifact = "androidApp/app/build/outputs/apk/benchmark/app-benchmark.apk"
        artifactSha256 = (Get-FileHash $apk -Algorithm SHA256).Hash
        result = "PASSED"
    }
    $metadata | ConvertTo-Json | Set-Content -Path (Join-Path $evidence "result.json")
    Write-Host "Release-candidate smoke PASSED. Evidence: $evidence"
} catch {
    & adb -s $DeviceSerial logcat -d -v threadtime | Set-Content -Path (Join-Path $evidence "logcat.txt")
    throw
} finally {
    if ($originalStayAwake -match "^\d+$") {
        & adb -s $DeviceSerial shell settings put global stay_on_while_plugged_in $originalStayAwake | Out-Null
    } else {
        & adb -s $DeviceSerial shell settings delete global stay_on_while_plugged_in | Out-Null
    }
    Pop-Location
}
