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
    if (-not $adbCommand) { throw "adb was not found." }
    $adb = $adbCommand.Source
}

if (-not $SkipBenchmarks) {
    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        throw "DeviceSerial is required unless -SkipBenchmarks is used."
    }
    $connectedSerials = & $adb devices |
        Select-String '^\S+\s+device$' |
        ForEach-Object { ($_ -split '\s+')[0] }
    if ($DeviceSerial -notin $connectedSerials) {
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
    $commands += ".\gradlew.bat :app:installBenchmark :macrobenchmark:assembleBenchmarkBenchmark --console=plain"
    $commands += "adb -s $DeviceSerial shell am instrument <six benchmark classes>"
    if ([int]$apiLevel -ge 33) {
        $commands += ".\gradlew.bat :app:generateReleaseBaselineProfile --console=plain"
    }
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
        & .\gradlew.bat :app:installBenchmark :macrobenchmark:assembleBenchmarkBenchmark --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "Benchmark APK build/install failed with exit code $LASTEXITCODE."
        }

        $testApk = Join-Path $androidRoot "macrobenchmark\build\outputs\apk\benchmarkBenchmark\macrobenchmark-benchmarkBenchmark.apk"
        & $adb -s $DeviceSerial install -r -t $testApk
        if ($LASTEXITCODE -ne 0) {
            throw "Benchmark test APK installation failed with exit code $LASTEXITCODE."
        }

        $benchmarkClasses = @(
            "DashboardBenchmark",
            "TransactionListScrollBenchmark",
            "SearchBenchmark",
            "SettingsBenchmark",
            "LargeDatasetBenchmark",
            "StartupBenchmark"
        ) | ForEach-Object { "com.mojtaba.folentra.macrobenchmark.$_" }
        $logPath = Join-Path $benchmarkDestination "macrobenchmark-suite.txt"
        $benchmarkOutput = & $adb -s $DeviceSerial shell am instrument -w -r `
            -e androidx.benchmark.suppressErrors EMULATOR `
            -e class ($benchmarkClasses -join ",") `
            "com.mojtaba.folentra.macrobenchmark/androidx.test.runner.AndroidJUnitRunner" 2>&1 |
            Tee-Object -FilePath $logPath
        $benchmarkFailed = $benchmarkOutput -match "FAILURES!!!|INSTRUMENTATION_STATUS_CODE: -2"
        if ($LASTEXITCODE -ne 0 -or $benchmarkFailed) {
            throw "Macrobenchmark suite failed. See $logPath."
        }

        & $adb -s $DeviceSerial pull `
            "/sdcard/Android/media/com.mojtaba.folentra.macrobenchmark/." `
            (Join-Path $benchmarkDestination "device-output") | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Pulling benchmark evidence failed with exit code $LASTEXITCODE."
        }

        if ([int]$apiLevel -ge 33) {
            $profileArgs = @(
                ":app:generateReleaseBaselineProfile",
                "--console=plain"
            )
            if ($DeviceSerial.StartsWith("emulator-", [StringComparison]::OrdinalIgnoreCase)) {
                $profileArgs += "-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR"
            }
            & .\gradlew.bat @profileArgs
            if ($LASTEXITCODE -ne 0) {
                throw "Baseline Profile generation failed with exit code $LASTEXITCODE."
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
    git = [ordered]@{ commit = $commit; branch = $branch; dirty = $dirty }
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

1. Inspect ``compose-compiler`` for unstable parameters and non-skippable composables.
2. Inspect benchmark JSON for frame-duration and startup metrics.
3. Open each Perfetto trace and correlate slow frames with Compose slices.
4. Compare only runs from the same device, OS, build type, and thermal state.
"@
$summary | Set-Content -LiteralPath (Join-Path $runRoot "summary.md")
Write-Output "Performance evidence written to: $runRoot"
