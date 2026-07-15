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

if (-not (Test-Path -LiteralPath $adb)) {
  throw "ADB not found. Run npm run android:setup-emulator first."
}

$onlineDevices = @(& $adb devices | Select-String -Pattern "\tdevice$")
if ($onlineDevices.Count -eq 0) {
  Write-Host "Starting medium_phone emulator..."
  android emulator start medium_phone
  if ($LASTEXITCODE -ne 0) {
    throw "The medium_phone emulator could not be started."
  }
}

Write-Host "Building debug APK..."
Push-Location $androidProject
try {
  & .\gradlew.bat assembleDebug --console=plain
  if ($LASTEXITCODE -ne 0) {
    throw "Android build failed with exit code $LASTEXITCODE."
  }
} finally {
  Pop-Location
}

Write-Host "Installing and opening Minimapa..."
& $adb install -r $apk
if ($LASTEXITCODE -ne 0) {
  throw "APK installation failed with exit code $LASTEXITCODE."
}

& $adb shell am start -W -n app.minimapa/.MainActivity
if ($LASTEXITCODE -ne 0) {
  throw "Minimapa could not be opened."
}
