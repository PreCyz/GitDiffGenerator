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
Remove-Item "$target\dist" -Force -Recurse -Verbose
Remove-Item "$target\input" -Force -Recurse -Verbose

Write-Host "Copy jar to $target\input\ " -ForegroundColor Green
mkdir "$target\input"
Copy-Item -Path "$target\Gipter-$version.jar" -Destination "$target\input\Gipter-$version.jar" -Verbose
Copy-Item -Path ..\docs\*.* -Destination "$target\input\" -Force -Exclude *.odt -Verbose

Write-Host "Create runtime-image with jlink: " -NoNewline -ForegroundColor Green
Write-Host "$jLink"

& $jlink `
    --output "$target\dist\gipter-jvm" `
    --module-path "$javaHome\jmods;$jfxMods" `
    --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.web,javafx.media,javafx.swing `
    --add-modules=java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management `
    --add-modules=java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se `
    --add-modules=java.security.jgss,java.xml,jdk.crypto.mscapi,jdk.httpserver,jdk.internal.opt,jdk.jfr `
    --add-modules=java.security.sasl,java.smartcardio,java.sql,java.sql.rowset,java.transaction.xa,java.xml.crypto `
    --add-modules=jdk.accessibility,jdk.attach,jdk.charsets,jdk.compiler,jdk.crypto.cryptoki,jdk.crypto.ec `
    --add-modules=jdk.dynalink,jdk.editpad,jdk.graal.compiler,jdk.graal.compiler.management,jdk.hotspot.agent `
    --add-modules=jdk.incubator.vector,jdk.internal.ed,jdk.internal.jvmstat,jdk.internal.le,jdk.internal.md `
    --add-modules=jdk.internal.vm.ci,jdk.jartool,jdk.javadoc,jdk.jcmd,jdk.jconsole,jdk.jdeps,jdk.jdi,jdk.jdwp.agent `
    --add-modules=jdk.jlink,jdk.jpackage,jdk.jshell,jdk.jsobject,jdk.jstatd,jdk.localedata,jdk.management.agent `
    --add-modules=jdk.management.jfr,jdk.management,jdk.naming.dns,jdk.naming.rmi,jdk.net,jdk.nio.mapmode,jdk.sctp `
    --add-modules=jdk.security.auth,jdk.security.jgss,jdk.unsupported.desktop,jdk.unsupported,jdk.xml.dom,jdk.zipfs `
    --strip-native-commands `
    --strip-debug `
    --no-man-pages `
    --no-header-files `
    --compress zip-6 `
    --verbose

#--add-modules=java.base,java.compiler,java.logging,java.management,java.naming,java.net.http,java.rmi,java.desktop `
#--add-modules=java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml,jdk.xml.dom `
#--add-modules=java.base,java.compiler,java.desktop,java.logging,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.xml,jdk.jfr,jdk.xml.dom `
#--add-modules=jdk.xml.dom,java.xml,jdk.httpserver,java.security.sasl,jdk.zipfs,java.base,jdk.jsobject,jdk.sctp,jdk.unsupported,java.security.jgss,java.compiler,jdk.nio.mapmode,jdk.dynalink,jdk.security.jgss,java.sql,java.xml.crypto,java.logging,jdk.internal.md,jdk.net,jdk.attach,jdk.internal.ed,java.net.http,jdk.internal.opt,jdk.internal.le,jdk.internal.jvmstat,jdk.management,jdk.security.auth `

<#--add-modules=java.rmi,java.xml,java.datatransfer,java.desktop `
--add-modules=jdk.management.jfr,jdk.jdi,jdk.xml.dom,jdk.httpserver,jdk.zipfs,jdk.javadoc,jdk.management.agent, `
--add-modules=jdk.jshell,jdk.jsobject,jdk.sctp,jdk.unsupported,jdk.nio.mapmode,jdk.dynalink,jdk.unsupported.desktop `
--add-modules=jdk.accessibility,jdk.security.jgss,jdk.incubator.vector,jdk.jfr,jdk.internal.md,jdk.net,jdk.internal.ed `
--add-modules=jdk.compiler,jdk.internal.opt,jdk.jconsole,jdk.attach,jdk.internal.le,jdk.jdwp.agent,jdk.internal.jvmstat `
--add-modules=jdk.jartool `
--add-modules=java.security.sasl,java.base,java.sql.rowset,java.smartcardio,java.security.jgss,java.compiler `
--add-modules=java.sql,java.transaction.xa,java.logging,java.xml.crypto,java.naming,java.prefs,java.net.http `
--add-modules=java.management,java.instrument,jdk.management,jdk.security.auth,java.scripting,java.management.rmi `
#>


Write-Host "Create msi installer with jpackage: " -NoNewline -ForegroundColor Green
Write-Host "$jPackage"

& $jpackage `
    --name Gipter `
    --runtime-image "$target/dist/gipter-jvm" `
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

Write-Host "=======================================================" -ForegroundColor DarkYellow
Write-Host "jpackage DONE!" -ForegroundColor Green

$ls = Get-ChildItem -Path "$currentLocation\$target\dist"
Write-Host "Directory: $currentLocation\$target\dist"
Write-Host $ls