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
$target = "..\target"
if (-not (Test-Path $target))
{
    Write-Error "target folder not found at: $target"
    exit 1
}
$jfxMods = "C:\Install\Java\JavaFX\javafx-jmods-25"
Write-Host "JavaFX mods location " -NoNewline -ForegroundColor Green
Write-Host "JavaFX mods location: $jfxMods"

Remove-Item "$target\dist" -Force -Recurse -Verbose
Remove-Item "$target\input" -Force -Recurse -Verbose

Write-Host "Preparing input to $target\input\ " -ForegroundColor Green
mkdir "$target\input"
Copy-Item -Path "$target\Gipter-$version.jar" -Destination "$target\input\Gipter-$version.jar" -Verbose
Copy-Item -Path ..\docs\*.* -Destination "$target\input\" -Force -Exclude *.odt -Verbose

Write-Host "Creating msi installer with jpackage: " -NoNewline -ForegroundColor Green
Write-Host "$jPackage"

& $jPackage `
    --module-path "$javaHome/jmods;$jfxMods" `
    --add-modules=jdk.naming.dns `
    --name Gipter `
    --input "$target/input" `
    --dest "$target/dist" `
    --main-class pg.gipter.Java11Main `
    --main-jar "Gipter-$version.jar" `
    --type msi `
    --icon "../src/main/resources/img/icons/gipter.ico" `
    --app-version "$version" `
    --vendor pawgit `
    --win-dir-chooser `
    --win-shortcut `
    --win-shortcut-prompt `
    --win-menu `
    --win-menu-group NCPawg `
    --java-options "--add-exports=jdk.naming.dns/com.sun.jndi.dns=ALL-UNNAMED" `
    --java-options "--add-opens=jdk.naming.dns/com.sun.jndi.dns=ALL-UNNAMED" `
    --verbose

Write-Host "=======================================================" -ForegroundColor DarkYellow
Write-Host "jpackage DONE!" -ForegroundColor Green

$ls = Get-ChildItem -Path "$currentLocation\$target\dist"
Write-Host "Item produced: " -NoNewline
Write-Host "$ls" -ForegroundColor Green