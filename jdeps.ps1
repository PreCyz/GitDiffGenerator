$javaHome = $Env:JAVA_HOME;
$jDeps = Join-Path -Path $javaHome -ChildPath "bin\jdeps.exe"
if (-not (Test-Path $jDeps)) {
    Write-Error "$jDeps executable not found at: $jDeps"
    exit 1
}

# This uses the '-s' (summary) flag for a concise output.
& $jDeps -summary -recursive  target\Gipter-1.0-SNAPSHOT.jar | Where-Object { $_ -match '->' } | ForEach-Object {
    $parts = $_.Trim() -split '\s+->\s+|\s+(?=[^ ]+$)'
    [PSCustomObject]@{
        SourceClass      = $parts[0]
        DependencyClass  = $parts[1]
        DependencyModule = $parts[2]
    }
} | Export-Csv -Path ".\jdeps_results.csv" -NoTypeInformation

Write-Host "jdeps done!"