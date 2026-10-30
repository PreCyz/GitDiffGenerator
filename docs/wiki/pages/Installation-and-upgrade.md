# Installation and upgrade

## Choose a distribution

Download the appropriate asset from [Releases](https://github.com/PreCyz/GitDiffGenerator/releases). The current source produces these distribution types:

| Distribution | How to launch | Java requirement |
| --- | --- | --- |
| Portable EXE | Extract the whole archive and launch `Gipter.exe`. | Included runtime; a separate Java installation is unnecessary. |
| Portable JAR | Extract the whole archive and use its launcher/shortcut, or the bundled-runtime command below. | Included runtime; use it instead of an older system Java. |
| MSI | Run the installer, then launch the installed shortcut. | Included runtime. |
| Standalone application JAR | Run with a compatible Java 25 installation. | Java 25; preserve the native sign-in helper alongside the application when using Toolkit. |

Keep the distribution directory together: runtime, application files, and native `auth` helper are parts of the installation. Moving just the executable/JAR can break startup or sign-in.

From the root of a portable JAR distribution on Windows:

```powershell
.\runtime\bin\java.exe -jar .\Gipter.jar
```

Run with the application directory as the working directory. Settings and logs use relative paths. A current JAR cannot run on Java 8 or Java 11 even if an older archive filename contains `11+`.

## Toolkit requirements

Toolkit device SSO requires Windows, Microsoft Edge WebView2 Evergreen Runtime, the native `Gipter.AuthHost.exe` supplied with the application, and access to the configured Toolkit site and work-account sign-in services. See [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in).

The current helper publish is self-contained and bundles .NET 10. End users do not need the .NET SDK or a separate .NET runtime installation. Building that helper requires the .NET 10 SDK; see [Building and testing](https://github.com/PreCyz/GitDiffGenerator/wiki/Building-and-testing).

Linux-specific producer classes exist, but the shared VCS executor currently launches `powershell.exe`, and the packaged UI/native dependencies are Windows-specific. These pages do not establish a ready-to-run Linux distribution. There is no non-Windows device-SSO fallback in this source.

## Upgrade

Use **Help → Upgrade** to start the application's upgrade workflow. The selected distribution depends on the running installation. Portable updates extract the downloaded archive and restart the application. An existing `gifs.json` is preserved by the archive updater.

MSI updates download an installer and back up application files under `%TEMP%\Gipter-backup` before starting it. On subsequent startup the application attempts to restore missing non-executable files from that backup. This is an upgrade recovery mechanism, not a complete backup policy.

Before a manual replacement, close Gipter and retain a copy of your configuration and local job data. Extract/install the full matching distribution. Do not assume a saved Windows sign-in session transfers to another Windows user or device.

The source baseline for these pages is 5.0.8 at `af01f8c8`, including its .NET 10 change. Release assets can predate that commit. This page does not assert that any particular downloaded archive already contains .NET 10.

Source: [portable-jar.ps1](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/installer/portable-jar.ps1), [portable-exe.ps1](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/installer/portable-exe.ps1), [installer.ps1](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/installer/installer.ps1), [UpgradeZip.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/upgrade/UpgradeZip.java), [BackupService.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/upgrade/BackupService.java).
