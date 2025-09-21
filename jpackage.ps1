$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Cyan

# --- Define the path to your Java executable ---
$javaHome = $env:J25
Write-Host "Java: $env:J25" -NoNewline
Write-Host "$javaHome" -ForegroundColor Green

$jPackage = Join-Path -Path $javaHome -ChildPath "bin\jpackage.exe"
if (-not (Test-Path $jPackage)) {
    Write-Error "jpackage executable not found at: $jPackage"
    exit 1
}
Write-Host "jpackage: " -NoNewline
Write-Host "jpackage: $jPackage" -ForegroundColor Green

& $jPackage `
    --name Gipter `
    --input target `
    --main-class pg.gipter.Java11Main `
    --main-jar Gipter-5.0.0.jar `
    --type exe `
    --dest target/dist `
    --verbose `
    --copyright "Paweł Gawędzki" `
    --vendor "pawgit" `
    --win-dir-chooser `
    --win-shortcut `
    --win-shortcut-prompt `
    --win-menu `
    --win-menu-group NCPawg `
    --icon "src/main/resources/img/png/minion.png" `
    --app-version 5.0.0

Write-Host "=======================================================" -ForegroundColor DarkYellow
Write-Host "jpackage DONE!" -ForegroundColor Green

$ls = Get-ChildItem -Path "$currentLocation\target\dist"
Write-Host "Directory: $currentLocation\target\dist"
Write-Host $ls