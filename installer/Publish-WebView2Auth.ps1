param (
    [string]$Destination = (Join-Path $PSScriptRoot '../target/webview2-auth'),
    [ValidateSet('win-x64', 'win-arm64')][string]$Runtime = 'win-x64'
)

$ErrorActionPreference = 'Stop'
$project = Join-Path $PSScriptRoot '../native/webview2-auth/Gipter.AuthHost.csproj'
$output = [System.IO.Path]::GetFullPath($Destination)
& dotnet publish $project --configuration Release --runtime $Runtime --self-contained true `
    -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true --output $output --nologo
if ($LASTEXITCODE -ne 0) { throw "WebView2 authentication host publish failed ($LASTEXITCODE)." }
if (-not (Test-Path -LiteralPath (Join-Path $output 'Gipter.AuthHost.exe'))) {
    throw 'WebView2 authentication host was not produced.'
}
