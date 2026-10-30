# Building and testing

Run these commands in an **application source checkout**. The `wiki` branch is a documentation branch with an old POM and no current application source; it is not the build checkout.

The source used for this documentation is `codex/webview2-device-sso` at `af01f8c8` (application version 5.0.8). Its .NET 10 update is committed there. This describes source requirements rather than the contents of every published release archive.

## Prerequisites

| Requirement | Purpose |
| --- | --- |
| JDK 25, with `JAVA_HOME` and `java` on `PATH` | Compile/run the Java application. |
| Maven 3.6.3 or newer, with `mvn` on `PATH` | Restore Java dependencies, run Java tests, and build the JAR. |
| .NET 10 SDK, with `dotnet` on `PATH` on Windows | Build the native sign-in helper and its regression harness. A runtime-only installation is insufficient. |
| Access to Maven Central and NuGet | Restore dependencies. |
| Microsoft Edge WebView2 Evergreen Runtime on Windows | Run native sign-in and the native regression browser fixture. |

Maven supplies JavaFX **25.0.5** and other Java dependencies. A separate JavaFX SDK download is unnecessary for the Maven build. The native projects target `net10.0-windows`; the auth host pins runtime `10.0.14` and restores WebView2 SDK `1.0.4258.31`.

Check the tools selected by the shell:

```shell
java -version
mvn --version
dotnet --list-sdks
```

The `dotnet` check applies to Windows builds. If only .NET 8 is installed, install/select the .NET 10 SDK before building the current source.

## Build and Java tests

From the application repository root:

```shell
mvn clean package
```

The build runs the configured Java tests and creates `target/Gipter-5.0.8.jar` for the current POM version. On Windows, the `windows-webview2` Maven profile automatically invokes `dotnet publish` during `generate-resources`. It publishes a self-contained `win-x64` helper under `target/webview2-auth`; place/keep that helper with the application for sign-in.

To verify just that Maven integration:

```shell
mvn generate-resources
```

Maven skips the Windows helper profile on non-Windows hosts. This removes the .NET requirement for that Java build, but the POM still selects Windows JavaFX native classifiers and the common VCS executor calls `powershell.exe`. A working Linux distribution requires additional platform work; a compile alone is insufficient. Device SSO remains Windows-only.

## Native publishing and regression checks

Publish only the helper from PowerShell:

```powershell
.\installer\Publish-WebView2Auth.ps1
```

The script defaults to `win-x64` and `target/webview2-auth`. It also accepts `-Runtime win-arm64` and `-Destination`; that option builds the helper only and does not establish ARM64 support for the complete Java distribution.

Run the native executable regression harness separately from Maven's Java tests:

```powershell
dotnet run --project native/webview2-auth-tests/Gipter.AuthHost.Tests.csproj --configuration Release
```

It checks identity/origin validation, cookie/session rules, Windows user encryption, tamper rejection, and real WebView2 initialization with intercepted synthetic responses. It does not contact a real Toolkit tenant. Browser fixture execution requires an environment able to run Windows/WebView2.

End users of the published self-contained helper do not need the .NET SDK or a separate runtime.

## IDE launches

Use JDK 25. For an IDE setup using a separate JavaFX SDK 25.0.5, set the module path before the application entry point/JAR:

```text
--module-path C:\Install\Java\javafx-sdk-25.0.5\lib --add-modules=javafx.fxml,javafx.controls,javafx.media,javafx.web,javafx.swing --add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED
```

Launch from the application root or set the helper path as a JVM option, before `-jar`:

```powershell
java '-Dgipter.webview2.helper=C:\Gipter\auth\Gipter.AuthHost.exe' -jar .\Gipter.jar
```

## Distribution packaging

`mvn package` builds the JAR and Windows helper; the release scripts under `installer` assemble distributions separately. Review their paths before running them: they contain machine-specific paths rather than a portable packaging configuration.

- `installer.ps1` uses the `J25` environment variable for the JDK, JavaFX 25.0.5 JMODs at its configured path, JDK `jlink`/`jpackage`, and WiX for Windows MSI creation. Oracle's [JDK 25 packaging prerequisites](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-overview.html) specify WiX 3.0 or later; configure a version supported by the selected JDK.
- `portable-exe.ps1` uses the configured Java 25 runtime directory, Launch4j, and `7z`.
- `portable-jar.ps1` uses the configured Java 25 runtime directory and `7z`.
- Each distribution script calls `Publish-WebView2Auth.ps1`, so the .NET 10 SDK must be available there too.

The scripts expect the installer directory as their working directory and use the supplied version to find the already-built JAR. Configure the external paths/tools for your machine before using them. The presence of an Inno Setup file does not mean the current MSI script invokes Inno Setup; its current packaging path uses `jpackage`.

Source: [pom.xml](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/pom.xml), [Gipter.AuthHost.csproj](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/native/webview2-auth/Gipter.AuthHost.csproj), [Program.cs](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/native/webview2-auth-tests/Program.cs), [Publish-WebView2Auth.ps1](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/installer/Publish-WebView2Auth.ps1), [installer.ps1](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/installer/installer.ps1).
