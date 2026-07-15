$ErrorActionPreference = "Stop"

Write-Host "Installing Android Emulator and API 36 Google Play image..."
android sdk install emulator system-images/android-36/google_apis_playstore/x86_64
if ($LASTEXITCODE -ne 0) {
  throw "Android emulator dependencies could not be installed."
}

$devices = @(android emulator list)
if ($devices -notcontains "medium_phone") {
  Write-Host "Creating medium_phone virtual device..."
  android emulator create medium_phone
  if ($LASTEXITCODE -ne 0) {
    throw "The medium_phone virtual device could not be created."
  }
} else {
  Write-Host "Virtual device medium_phone already exists."
}

Write-Host "Android emulator is ready."
