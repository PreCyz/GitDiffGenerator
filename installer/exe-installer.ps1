param (
    [Parameter(Mandatory = $true)][string]$version = "5.0.0"
)

$currentLocation = Get-Location
Write-Host "Current location: " -NoNewline
Write-Host "$currentLocation" -ForegroundColor Yellow

# --- Define the path to your Java executable ---
$javaHome = $env:J25
Write-Host "$javaHome" -ForegroundColor Green

$jPackage = Join-Path -Path $javaHome -ChildPath "bin\jpackage.exe"
if (-not (Test-Path $jPackage))
{
    Write-Error "jpackage executable not found at: $jPackage"
    exit 1
}
$jLink = Join-Path -Path $javaHome -ChildPath "bin\jlink.exe"
if (-not (Test-Path $jLink))
{
    Write-Error "jlink executable not found at: $jLink"
    exit 1
}
$jfxMods = "C:\Install\Java\JavaFX\javafx-jmods-25"
if (-not (Test-Path $jfxMods))
{
    Write-Error "jfxMods mods not found at: $jfxMods"
    exit 1
}
$target = "..\target"
if (-not (Test-Path $target))
{
    Write-Error "target folder not found at: $target"
    exit 1
}
if (Test-Path "$target\dist") {
    Write-Host "Removing $target\dist" -ForegroundColor Yellow
    Remove-Item "$target\dist" -Force -Recurse
}
if (Test-Path "$target\input") {
    Write-Host "Removing $target\input" -ForegroundColor Yellow
    Remove-Item "$target\input" -Force -Recurse
}

Write-Host "Copy jar to $target\input\ " -ForegroundColor Green
mkdir "$target\input"
Copy-Item -Path "$target\Gipter-$version.jar" -Destination "$target\input\Gipter-$version.jar" -Verbose
Copy-Item -Path ..\docs\*.* -Destination "$target\input\" -Force -Exclude *.odt -Verbose

Write-Host "Create runtime-image with jlink: " -NoNewline -ForegroundColor Green
Write-Host "$jLink"

& $jlink `
    --output "$target\dist\runtime" `
    --add-modules jdk.naming.dns,jdk.management.jfr,java.rmi,jdk.jdi,java.xml,jdk.xml.dom,java.datatransfer `
    --add-modules jdk.httpserver,java.desktop,java.security.sasl,jdk.zipfs,java.base,jdk.javadoc `
    --add-modules jdk.management.agent,jdk.jshell,jdk.jsobject,java.sql.rowset,jdk.sctp,java.smartcardio `
    --add-modules jdk.unsupported,java.security.jgss,java.compiler,jdk.nio.mapmode,jdk.dynalink `
    --add-modules jdk.unsupported.desktop,jdk.accessibility,jdk.security.jgss,jdk.incubator.vector,java.sql `
    --add-modules java.logging,java.transaction.xa,java.xml.crypto,jdk.jfr,jdk.internal.md,jdk.net,java.naming `
    --add-modules jdk.internal.ed,java.prefs,java.net.http,jdk.compiler,jdk.internal.opt,jdk.jconsole,jdk.attach `
    --add-modules jdk.internal.le,java.management,jdk.jdwp.agent,jdk.internal.jvmstat,java.instrument `
    --add-modules jdk.management,jdk.security.auth,java.scripting,jdk.jartool,java.management.rmi `
    --strip-native-commands `
    --strip-debug `
    --no-man-pages `
    --no-header-files `
    --verbose

Write-Host "Create msi installer with jpackage: " -NoNewline -ForegroundColor Green
Write-Host "$jPackage"

& $jpackage `
    --name Gipter `
    --runtime-image "$target/dist/runtime" `
    --main-class pg.gipter.Java11Main `
    --main-jar "Gipter-$version.jar" `
    --dest "$target/dist" `
    --input "$target/input" `
    --app-version $version `
    --icon "../src/main/resources/img/icons/gipter.ico" `
    --type msi `
    --vendor pawgit `
    --win-dir-chooser `
    --win-shortcut `
    --win-shortcut-prompt `
    --win-menu `
    --win-menu-group NCPawg `
    --description "Gipter JavaFX msi installer." `
    --java-options "--add-exports=jdk.naming.dns/com.sun.jndi.dns=ALL-UNNAMED" `
    --verbose

Rename-Item -Path "$target/dist/Gipter-$version.msi" -NewName "Gipter-v$version.msi"

Write-Host "=======================================================" -ForegroundColor DarkYellow
Write-Host "jpackage DONE!" -ForegroundColor Green

$ls = Get-ChildItem -Path "$currentLocation\$target\dist"
Write-Host "Directory: $currentLocation\$target\dist" -ForegroundColor Cyan
Write-Host $ls