param (
    [Parameter(Mandatory=$true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

$target = "..\target"
if (-not (Test-Path $target))
{
    Write-Error "target folder not found at: $target"
    exit 1
}

mkdir "$target\input"

Write-Host "Preparing input to [$target\input\]" -ForegroundColor Green
Copy-Item -Path "..\docs\*.*" -Destination "$target\input\" -Force -Exclude *.odt -Verbose
Copy-Item -Path "$target\Gipter-$version.jar" -Destination "$target\input\Gipter.jar" -Force -Verbose

Write-Host "Zipping portable ..."
& 7z a "$target\dist\11+Gipter-v$version.7z" "$target\input\*"

$lsDist = Get-ChildItem -Path "$target\dist"
Write-Host "Item produced: " -NoNewline -ForegroundColor Cyan
Write-Host "$lsDist"