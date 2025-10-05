param (
    [Parameter(Mandatory=$true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

$jre25 = "C:\Install\Java\jdk-25+36-jre"
Write-Host "jre25: " -NoNewline -ForegroundColor Green
Write-Host "$jre25"
if (-not (Test-Path $jre25)) {
    Write-Error "jre not found at: [$jre25]"
    exit 1
}
$target = "..\target"
if (-not (Test-Path $target))
{
    Write-Error "target folder not found at: $target"
    exit 1
}
mkdir "$target\dist\portable-jar"
Write-Host "Preparing dist to $target\dist\ " -ForegroundColor Green
Copy-Item -Path "..\src\main\resources\img\icons\gipter.ico" -Destination "$target\dist\portable-jar\" -Force -Verbose
Copy-Item -Path "$jre25" -Destination "$target\dist\portable-jar\runtime\" -Force -Recurse
Copy-Item -Path ..\docs\*.* -Destination "$target\dist\portable-jar\" -Force -Exclude *.odt -Verbose
Copy-Item -Path "$target\Gipter-$version.jar" -Destination "$target\dist\portable-jar\Gipter.jar" -Force -Verbose

if (-not (Test-Path "$target\dist\portable-jar"))
{
    Write-Error "portable folder not found at: [$target\dist\portable-jar]"
    exit 1
}
Write-Host "Zipping portable jar ..."
& 7z a "$target\dist\Gipter-v$version.7z" "$target\dist\portable-jar\*"

$lsDist = Get-ChildItem -Path "$currentLocation\$target\dist"
$lsPortable = Get-ChildItem -Path "$currentLocation\$target\dist"
Write-Host "Item produced: " -NoNewline -ForegroundColor Cyan
Write-Host "$lsDist $lsPortable"