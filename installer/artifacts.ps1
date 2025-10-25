param (
    [Parameter(Mandatory = $true)][string]$version = "5.0.0"
)

Write-Host "executing installer.ps1" -ForegroundColor Cyan
.\installer.ps1 -version $version

Write-Host "executing portable-exe.ps1" -ForegroundColor Cyan
.\portable-exe.ps1 -version $version

Write-Host "executing portable-jar.ps1" -ForegroundColor Cyan
.\portable-jar.ps1 -version $version