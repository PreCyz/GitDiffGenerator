$javaHome = $Env:J25;
$jDeps = Join-Path -Path $javaHome -ChildPath "bin\jdeps.exe"
if (-not (Test-Path $jDeps)) {
    Write-Error "jdeps executable not found at: $jDeps"
    exit 1
}

# This uses the '-s' (summary) flag for a concise output.
& $jDeps -summary -recursive --class-path "../target/app/*"--multi-release 25 ..\target\Gipter-5.0.0.jar | Where-Object { $_ -match '->' } | ForEach-Object {
    $parts = $_.Trim() -split '\s+->\s+|\s+(?=[^ ]+$)'
    [PSCustomObject]@{
        SourceClass      = $parts[0]
        DependencyClass  = $parts[1]
        DependencyModule = $parts[2]
    }
} | Export-Csv -Path ".\jdeps_results.csv" -NoTypeInformation

Write-Host "1. jdeps done!"

& $jDeps -summary -recursive --class-path "../target/app/*"--multi-release 25 "..\target\Gipter-5.0.0.jar"

Write-Host "2. jdeps done!"