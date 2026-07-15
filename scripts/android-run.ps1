param(
  [switch]$Verify
)

$ErrorActionPreference = "Stop"

$workspace = Split-Path -Parent $PSScriptRoot
$androidProject = Join-Path $workspace "apps\android"
$apk = Join-Path $androidProject "app\build\outputs\apk\debug\app-debug.apk"

if (-not $env:JAVA_HOME) {
  $javaCandidates = @(
    (Join-Path $env:ProgramFiles "Android\Android Studio\jbr")
    Get-ChildItem (Join-Path $env:ProgramFiles "Microsoft") -Directory -Filter "jdk-17*" -ErrorAction SilentlyContinue |
      Select-Object -ExpandProperty FullName
  )
  $env:JAVA_HOME = $javaCandidates | Where-Object { Test-Path (Join-Path $_ "bin\java.exe") } | Select-Object -First 1
  if (-not $env:JAVA_HOME) {
    throw "JDK 17 not found. Install Android Studio or Microsoft OpenJDK 17."
  }
}

if (-not $env:ANDROID_HOME) {
  $env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
}
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
$emulator = Join-Path $env:ANDROID_HOME "emulator\emulator.exe"

if (-not (Test-Path -LiteralPath $adb)) {
  throw "ADB not found. Run npm run android:setup-emulator first."
}

$onlineEmulators = @(& $adb devices | Select-String -Pattern "^emulator-[0-9]+\s+device$")
if ($onlineEmulators.Count -eq 0) {
  Write-Host "Starting medium_phone emulator..."
  if (-not (Test-Path -LiteralPath $emulator)) {
    throw "Android Emulator not found. Run npm run android:setup-emulator first."
  }
  $availableDevices = @(& $emulator -list-avds)
  if ($availableDevices -notcontains "medium_phone") {
    throw "Virtual device medium_phone not found. Run npm run android:setup-emulator first."
  }
  Start-Process -FilePath $emulator -ArgumentList @("-avd", "medium_phone") | Out-Null
}

Write-Host "Waiting for Android to finish booting..."
$bootDeadline = (Get-Date).AddMinutes(3)
do {
  Start-Sleep -Seconds 2
  $onlineEmulators = @(& $adb devices | Select-String -Pattern "^emulator-[0-9]+\s+device$")
  $deviceSerial = if ($onlineEmulators.Count -gt 0) {
    ($onlineEmulators[0].ToString() -split "\s+")[0]
  } else {
    $null
  }
  $bootCompleted = if ($deviceSerial) {
    ([string](& $adb -s $deviceSerial shell getprop sys.boot_completed 2>$null)).Trim()
  } else {
    ""
  }
} until ($bootCompleted -eq "1" -or (Get-Date) -ge $bootDeadline)

if ($bootCompleted -ne "1") {
  throw "Android emulator did not finish booting within 3 minutes."
}

# An already-running AVD may have gone to sleep between development sessions.
# Compose UI tests need a resumed, visible Activity, and the final manual test
# should never leave the user looking at a powered-off emulator display.
& $adb -s $deviceSerial shell input keyevent KEYCODE_WAKEUP | Out-Null
& $adb -s $deviceSerial shell wm dismiss-keyguard | Out-Null
& $adb -s $deviceSerial shell settings put system screen_off_timeout 1800000 | Out-Null

# Inject only the public local Auth configuration into BuildConfig. The key is
# discovered at build time and is never written to a tracked file.
if (-not $env:MINIMAPA_SUPABASE_PUBLISHABLE_KEY) {
  Push-Location $workspace
  try {
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $supabaseEnvironment = @(& npx supabase status -o env 2>$null)
    $supabaseStatusExitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousErrorActionPreference
  } finally {
    $ErrorActionPreference = "Stop"
    Pop-Location
  }
  $publishableLine = $supabaseEnvironment | Where-Object { $_ -match "^PUBLISHABLE_KEY=" } | Select-Object -First 1
  if (-not $publishableLine) {
    $publishableLine = $supabaseEnvironment | Where-Object { $_ -match "^ANON_KEY=" } | Select-Object -First 1
  }
  if ($supabaseStatusExitCode -eq 0 -and $publishableLine) {
    $env:MINIMAPA_SUPABASE_PUBLISHABLE_KEY = (($publishableLine -split "=", 2)[1]).Trim('"')
    $env:MINIMAPA_SUPABASE_URL = "http://10.0.2.2:54321"
    Write-Host "Using public credentials from the local Supabase stack."
  } else {
    Write-Warning "Supabase local is unavailable; real Auth will remain disabled and demo access will still work."
  }
}

if ($Verify) {
  Write-Host "Running unit tests, UI tests, lint and debug build..."
  $gradleTasks = @("test", "connectedDebugAndroidTest", "lintDebug", "assembleDebug")
} else {
  Write-Host "Building debug APK..."
  $gradleTasks = @("assembleDebug")
}

Push-Location $androidProject
try {
  & .\gradlew.bat @gradleTasks --console=plain
  if ($LASTEXITCODE -ne 0) {
    throw "Android build failed with exit code $LASTEXITCODE."
  }
} finally {
  Pop-Location
}

Write-Host "Installing and opening Minimapa..."
& $adb -s $deviceSerial install -r $apk
if ($LASTEXITCODE -ne 0) {
  throw "APK installation failed with exit code $LASTEXITCODE."
}

& $adb -s $deviceSerial shell am start -W -n app.minimapa/.MainActivity
if ($LASTEXITCODE -ne 0) {
  throw "Minimapa could not be opened."
}

Write-Host "Minimapa is open in medium_phone. The emulator will remain open for manual testing."
