# --- Define the path to your Java executable ---
$javaHome = $env:JAVA_HOME
$javaHome = $env:J25
if (-not $javaHome) {
    Write-Error "JAVA_HOME environment variable is not set."
    exit 1
}
$jLink = Join-Path -Path $javaHome -ChildPath "bin\jlink.exe"
if (-not (Test-Path $jLink)) {
    Write-Error "jlink executable not found at: $jLink"
    exit 1
}
$jfxMods = "C:\Install\Java\javafx-jmods-21.0.8"
if (-not (Test-Path $jfxMods)) {
    Write-Error "jfxMods executable not found at: $jfxMods"
    exit 1
}
$target = "../target"

& $jLink `
    --verbose `
    --module-path "$javaHome/jmods;$jfxMods" `
    --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.web,javafx.media,javafx.swing `
    --add-modules=java.base,java.net.http,java.desktop,java.xml,jdk.xml.dom,java.sql,java.naming,java.management,java.rmi,java.logging,java.scripting,jdk.jsobject,jdk.unsupported,jdk.jfr `
    --bind-services `
    --compress zip-6 `
    --no-header-files `
    --no-man-pages `
    --strip-debug `
    --output "$target/java-runtime"

Write-Host "jlink done"

$jPackage = Join-Path -Path $javaHome -ChildPath "bin\jpackage.exe"
if (-not (Test-Path $jPackage)) {
    Write-Error "jlink executable not found at: $jPackage"
    exit 1
}

& $jPackage `
    --name Gipter `
    --input target `
    --main-jar Gipter-1.0-SNAPSHOT.jar `
    --runtime-image "$target/java-runtime" `
    --type exe `
    --dest target `
    --verbose `
    --win-dir-chooser `
    --win-shortcut `
    --win-shortcut-prompt `
    --app-version 1.0

Write-Host "jpackage done"
