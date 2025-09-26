param (
    [Parameter(Mandatory=$true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

# --- Define the path to your Java executable ---
$javaHome = $env:J25
Write-Host "$javaHome" -ForegroundColor Green

$jPackage = Join-Path -Path $javaHome -ChildPath "bin\jpackage.exe"
if (-not (Test-Path $jPackage)) {
    Write-Error "jpackage executable not found at: $jPackage"
    exit 1
}

Write-Host "Copy jar to target\input\ " -ForegroundColor Green
mkdir target\input
Copy-Item -Path "target\Gipter-$version.jar" -Destination target\input\ -Verbose
Copy-Item -Path docs\*.* -Destination target\input\ -Verbose -Force -Exclude *.odt

Write-Host "Execute jpackage: " -NoNewline
Write-Host "$jPackage" -ForegroundColor Green

& $jPackage `
    --name Gipter `
    --input target\input `
    --main-class pg.gipter.Java11Main `
    --main-jar "Gipter-$version.jar" `
    --type msi `
    --vendor pawgit `
    --dest target/dist `
    --win-dir-chooser `
    --win-shortcut `
    --win-shortcut-prompt `
    --win-menu `
    --win-menu-group NCPawg `
    --icon "src/main/resources/img/icons/chicken.ico" `
    --app-version "$version" `
    --verbose

Write-Host "=======================================================" -ForegroundColor DarkYellow
Write-Host "jpackage DONE!" -ForegroundColor Green

$ls = Get-ChildItem -Path "$currentLocation\target\dist"
Write-Host "Directory: $currentLocation\target\dist"
Write-Host $ls