param (
    [Parameter(Mandatory=$true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

# --- Define the path to your Java executable ---
$javaHome = $env:J25
Write-Host "$javaHome" -ForegroundColor Green
$jre25 = "C:\Install\Java\jdk-25+36-jre"
Write-Host "jre25: " -NoNewline -ForegroundColor Green
Write-Host "$jre25"

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

Write-Host "Preparing input to $target\input\ " -ForegroundColor Green
Copy-Item -Path "$target\classes\launch4j.xml" -Destination "$target\input\" -Force -Verbose
Copy-Item -Path "..\src\main\resources\img\icons\gipter.ico" -Destination "$target\input\" -Force -Verbose
#Copy-Item -Path "$target\classes\installer-config.iss" -Destination "$target\input\" -Force -Verbose

Copy-Item -Path "$jre25" -Destination "$target\dist\portable\runtime\" -Force -Recurse
Copy-Item -Path ..\docs\*.* -Destination "$target\dist\portable\" -Force -Exclude *.odt -Verbose

Write-Host "launch4j: " -NoNewline -ForegroundColor Green
Write-Host "$launch4j"

& $launch4j "$target\input\launch4j.xml"

Write-Host "===============launch4j DONE!==========================" -ForegroundColor Green

#Write-Host "inno compile setup: " -NoNewline -ForegroundColor Green
#Write-Host "$innoSetupCompiler"

#& $innoSetupCompiler "$target\input\installer-config.iss"

Write-Host "Zipping portable ..."

& 7z a "$target\dist\Gipter-$version-portable.7z" "$target\dist\portable\*"

$lsDist = Get-ChildItem -Path "$currentLocation\$target\dist"
$lsPortable = Get-ChildItem -Path "$currentLocation\$target\dist"
Write-Host "Item produced: " -NoNewline -ForegroundColor Cyan
Write-Host "$lsDist $lsPortable"