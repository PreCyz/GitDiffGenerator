# Gipter from CLI

Use Java 25, or the Java runtime included in a portable JAR distribution. Run commands from the application's directory so relative configuration/data/log paths resolve consistently. The examples below assume `Gipter.jar` is the distribution filename; use `target/Gipter-5.0.8.jar` for the current Maven build.

## Launch modes

```powershell
java -jar .\Gipter.jar
```

The default is UI mode. With saved configuration in place, CLI mode is selected by:

```powershell
java -jar .\Gipter.jar useUI=N
```

CLI mode is not a guarantee of a headless run. An absent/expired Toolkit session can open interactive sign-in. Some error/confirmation paths also use JavaFX. `useUI=N` does not itself disable uploading or authentication.

For a portable JAR distribution, replace `java` with the bundled executable:

```powershell
.\runtime\bin\java.exe -jar .\Gipter.jar useUI=N
```

## Generation without Toolkit

First configure `appConfig.noSSO` as JSON `true`, `appConfig.uploadItem` as `false`, and `appConfig.confirmationWindow` as `false` in `applicationProperties.json`. See the complete file in [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples). Then run a command such as:

```powershell
java -jar .\Gipter.jar useUI=N configurationName=local 'author=Anakin Skywalker' 'projectPath=C:\Projects\Example' 'itemPath=C:\Gipter\items' periodInDays=7 uploadItem=N confirmationWindow=N
```

The output directory and repository must exist. The shared VCS executor currently invokes `powershell.exe`, including for Linux-specific producer classes. A non-Windows build needs platform work before it should be treated as runnable; replacing Windows paths with `/home/...` is insufficient.

Use ordinary backslashes in PowerShell arguments; JSON strings require escaped backslashes. Keep each command on one line, or use the continuation syntax for your shell.

**Current limitation:** in the CLI property implementation, an explicit `noSSO` argument reads the UI flag instead of the requested SSO flag. For generation-only CLI use, save `appConfig.noSSO: true` and omit `noSSO` from the command. This does not eliminate all application startup integrations; it skips the Toolkit authentication/upload flow. It cannot retrieve Toolkit documents without authentication.

## Configuration and precedence

The settings file is `applicationProperties.json` in the working directory, containing `appConfig`, `toolkitConfig`, and `runConfigs`.

- `preferredArgSource=CLI` is the default selector. With `useUI=N`, explicitly supplied options generally take precedence over saved values; missing options fall back to the selected saved configuration and defaults.
- `preferredArgSource=FILE` gives stored values priority and falls back to arguments/defaults for missing values.
- UI launches use the UI property implementation, based on saved configuration. It has additional UI/tray behavior, so the selector alone does not make all modes equivalent.

Use [Parameters](https://github.com/PreCyz/GitDiffGenerator/wiki/Parameters) for default values and known exceptions. Named saved configurations are managed in the UI; selecting one on the command line is not a promise to execute every saved configuration. UI **All-in** and scheduled jobs handle multiple configurations.

For an explicit historical range, pass `startDate=2026-10-01 endDate=2026-10-14` and avoid conflicting saved period settings; see [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting) for the current precedence limitation. The JSON run model does not persist explicit start/end dates, so use command arguments for reproducible historical ranges.

## Java options

JVM options go before `-jar`; application `name=value` arguments go after the JAR. For a separate JavaFX SDK:

```powershell
java --module-path 'C:\Install\Java\javafx-sdk-25.0.5\lib' --add-modules=javafx.fxml,javafx.controls,javafx.media,javafx.web,javafx.swing -jar .\Gipter.jar useUI=N
```

See [Building and testing](https://github.com/PreCyz/GitDiffGenerator/wiki/Building-and-testing) for developer setup and [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in) for upload authentication. Do not treat the current CLI process exit status as a reliable success indicator: `CLILauncher` exits with `-1` after running. Check generated output and logs.

Source: [Main.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/Main.java), [ApplicationPropertiesFactory.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/ApplicationPropertiesFactory.java), [CliApplicationProperties.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/CliApplicationProperties.java), [CLILauncher.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/launchers/CLILauncher.java).
