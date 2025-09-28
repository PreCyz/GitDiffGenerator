param (
    [Parameter(Mandatory=$true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

# --- Define the path to your Java executable ---
$javaHome = $env:J25
Write-Host "$javaHome" -ForegroundColor Green

$java = Join-Path -Path $javaHome -ChildPath "bin\java.exe"
if (-not (Test-Path $java)) {
    Write-Error "java executable not found at: $java"
    exit 1
}

$target = "../target"

& $java -XX:StartFlighRecording: `
    jdk.MethodTrace#filter=java.utli.HashMap::resize, `
    filename=recording.jfr -jar "$target/Gipter-$version.jar" useUI=Y

$jfr = Join-Path -Path $javaHome -ChildPath "bin\jfr.exe"
if (-not (Test-Path $jfr)) {
    Write-Error "jfr executable not found at: $jfr"
    exit 1
}

& $jfr print --events jdk.MethodTrace `
    --stack-depth 20 `
    recording.jfr