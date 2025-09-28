param (
    [Parameter(Mandatory=$true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

# --- Define the path to your Java executable ---
$javaHome = $env:J25
Write-Host "$javaHome" -ForegroundColor Green

$launch4j = "C:\Install\Launch4j\launch4jc.exe"
if (-not (Test-Path $launch4j)) {
    Write-Error "launch4j executable not found at: $launch4j"
    exit 1
}
$innoSetupCompiler = "C:\Install\Inno Setup 6\ISCC.exe"
if (-not (Test-Path $innoSetupCompiler)) {
    Write-Error "innoSetupCompiler executable not found at: $innoSetupCompiler"
    exit 1
}
$target = "..\target"
if (-not (Test-Path $target))
{
    Write-Error "target folder not found at: $target"
    exit 1
}
Remove-Item "$target\input" -Force -Recurse -Verbose
Remove-Item "$target\dist" -Force -Recurse -Verbose

Write-Host "Preparing input to $target\input\ " -ForegroundColor Green
mkdir "$target\input"
mkdir "$target\dist"
Copy-Item -Path "$target\Gipter-$version.jar" -Destination "$target\input\Gipter-$version.jar" -Verbose
Copy-Item -Path ..\docs\*.* -Destination "$target\input\" -Force -Exclude *.odt -Verbose
Copy-Item -Path "$target\classes\*.xml" -Destination "$target\input\" -Exclude logback.xml -Force -Verbose
Copy-Item -Path "$target\classes\installer-config.iss" -Destination "$target\input\" -Force -Verbose
Copy-Item -Path "..\src\main\resources\img\icons\gipter.ico" -Destination "$target\input\" -Force -Verbose
Copy-Item -Path "$env:J25" -Destination "$target\dist\runtime\" -Force -Recurse -Verbose
Copy-Item -Path ..\docs\*.* -Destination "$target\dist\" -Force -Exclude *.odt -Verbose
Remove-Item "$target\dist\runtime\jmods" -Force -Recurse -Verbose

Write-Host "launch4j: " -NoNewline -ForegroundColor Green
Write-Host "$launch4j"

& $launch4j "$target\input\launch4j.xml"

Write-Host "===============launch4j DONE!==========================" -ForegroundColor Green

Write-Host "inno compile setup: " -NoNewline -ForegroundColor Green
Write-Host "$innoSetupCompiler"

& $innoSetupCompiler "$target\input\installer-config.iss"

$ls = Get-ChildItem -Path "$currentLocation\$target\dist"
Write-Host "Item produced: " -NoNewline
Write-Host "$ls" -ForegroundColor Green