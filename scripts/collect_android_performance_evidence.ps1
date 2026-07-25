param(
    [string]$DeviceSerial = "",
    [string]$EvidenceRoot = "",
    [switch]$SkipBenchmarks
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$androidRoot = Join-Path $repoRoot "androidApp"
$adb = Join-Path $env:LOCALAPPDATA "Android\SDK1\platform-tools\adb.exe"
if (-not (Test-Path -LiteralPath $adb)) {
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if (-not $adbCommand) {
        throw "adb was not found. Install Android platform-tools or add adb to PATH."
    }
    $adb = $adbCommand.Source
}

if (-not $SkipBenchmarks) {
    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        throw "DeviceSerial is required unless -SkipBenchmarks is used."
    }
    $connectedDevices = & $adb devices
    if ($connectedDevices -notmatch "(?m)^$([regex]::Escape($DeviceSerial))\s+device$") {
        throw "Device '$DeviceSerial' is not connected and ready."
    }
}

if ([string]::IsNullOrWhiteSpace($EvidenceRoot)) {
    $EvidenceRoot = Join-Path $androidRoot "performance-evidence"
}
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$runRoot = Join-Path $EvidenceRoot $timestamp
$composeDestination = Join-Path $runRoot "compose-compiler"
$benchmarkDestination = Join-Path $runRoot "macrobenchmark"
New-Item -ItemType Directory -Path $composeDestination, $benchmarkDestination -Force | Out-Null

$commit = (git -C $repoRoot rev-parse HEAD).Trim()
$branch = (git -C $repoRoot branch --show-current).Trim()
$dirty = [bool](git -C $repoRoot status --porcelain)
$deviceModel = "not captured"
$androidVersion = "not captured"
$apiLevel = "not captured"
if (-not $SkipBenchmarks) {
    $deviceModel = (& $adb -s $DeviceSerial shell getprop ro.product.model).Trim()
    $androidVersion = (& $adb -s $DeviceSerial shell getprop ro.build.version.release).Trim()
    $apiLevel = (& $adb -s $DeviceSerial shell getprop ro.build.version.sdk).Trim()
}

$commands = @(
    ".\gradlew.bat :app:compileReleaseKotlin :feature:dashboard:compileReleaseKotlin :feature:search:compileReleaseKotlin :feature:transaction:compileReleaseKotlin -Pfolentra.composeReports=true --console=plain"
)
if (-not $SkipBenchmarks) {
    $commands += ".\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest --console=plain"
}

Push-Location $androidRoot
try {
    $composeArgs = @(
        ":app:compileReleaseKotlin",
        ":feature:dashboard:compileReleaseKotlin",
        ":feature:search:compileReleaseKotlin",
        ":feature:transaction:compileReleaseKotlin",
        "-Pfolentra.composeReports=true",
        "--console=plain"
    )
    & .\gradlew.bat @composeArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Compose compiler evidence build failed with exit code $LASTEXITCODE."
    }

    Get-ChildItem -Path $androidRoot -Directory -Recurse -Filter compose_compiler |
        Where-Object { $_.FullName -like "*\build\compose_compiler" } |
        ForEach-Object {
            $modulePath = $_.FullName.Substring($androidRoot.Length + 1)
            $modulePath = $modulePath.Replace("\build\compose_compiler", "").Replace("\", "__")
            Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $composeDestination $modulePath) -Recurse -Force
        }

    if (-not $SkipBenchmarks) {
        $env:ANDROID_SERIAL = $DeviceSerial
        & .\gradlew.bat :macrobenchmark:connectedBenchmarkBenchmarkAndroidTest --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "Macrobenchmark evidence run failed with exit code $LASTEXITCODE."
        }

        $benchmarkOutputs = Join-Path $androidRoot "macrobenchmark\build\outputs"
        foreach ($relativePath in @(
            "connected_android_test_additional_output",
            "androidTest-results\connected\benchmarkBenchmark"
        )) {
            $source = Join-Path $benchmarkOutputs $relativePath
            if (Test-Path -LiteralPath $source) {
                $safeName = $relativePath.Replace("\", "__")
                Copy-Item -LiteralPath $source -Destination (Join-Path $benchmarkDestination $safeName) -Recurse -Force
            }
        }
    }
}
finally {
    Pop-Location
    Remove-Item Env:ANDROID_SERIAL -ErrorAction SilentlyContinue
}

$manifest = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    git = [ordered]@{
        commit = $commit
        branch = $branch
        dirty = $dirty
    }
    device = [ordered]@{
        serial = $DeviceSerial
        model = $deviceModel
        androidVersion = $androidVersion
        apiLevel = $apiLevel
    }
    benchmarksSkipped = [bool]$SkipBenchmarks
    commands = $commands
}
$manifest | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $runRoot "manifest.json")

$summary = @"
# Recomposition and jank evidence

- Commit: ``$commit``
- Branch: ``$branch``
- Dirty working tree: ``$dirty``
- Device: ``$deviceModel`` (``$DeviceSerial``)
- Android: ``$androidVersion`` / API ``$apiLevel``
- Generated: ``$($manifest.generatedAt)``

## Review

1. Inspect ``compose-compiler`` for unstable parameters and non-skippable
   composables in critical Dashboard, Transaction, Search, and Settings paths.
2. Inspect benchmark JSON for frame-duration and frame-overrun metrics.
3. Open each Perfetto trace and correlate slow frames with Compose
   composition/recomposition slices.
4. Compare only against evidence captured on the same device, OS, build type,
   and thermal conditions.
"@
$summary | Set-Content -LiteralPath (Join-Path $runRoot "summary.md")

Write-Output "Performance evidence written to: $runRoot"
