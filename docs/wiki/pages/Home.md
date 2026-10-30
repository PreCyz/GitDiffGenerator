# Gipter

Gipter generates copyright items from Git, Mercurial, SVN, or Toolkit documents. You can generate items locally, upload them to Toolkit, and schedule work while the application is running.

These pages describe application source version **5.0.8**, checked against commit `af01f8c8`. That source includes the .NET 10 build update. Check the contents of a downloaded release separately; a source change does not mean an existing release archive has been rebuilt.

## Get started

1. Choose a distribution in [Installation and upgrade](https://github.com/PreCyz/GitDiffGenerator/wiki/Installation-and-upgrade). Windows portable and MSI distributions include their Java runtime.
2. For Toolkit uploads, complete [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in) on Windows with Microsoft Edge WebView2 Runtime installed.
3. Create a named configuration using the [Wizard](https://github.com/PreCyz/GitDiffGenerator/wiki/Wizard) or [Main window](https://github.com/PreCyz/GitDiffGenerator/wiki/Main-window).
4. Choose an [Item types](https://github.com/PreCyz/GitDiffGenerator/wiki/Item-types) value, check authors, projects, output location, and dates, then save the configuration.
5. Execute one configuration or use All-in for all saved configurations. Uploading depends on the application settings.

## Guides

- [Gipter from CLI](https://github.com/PreCyz/GitDiffGenerator/wiki/Gipter-from-CLI) and [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples): launch commands and configuration examples.
- [Parameters](https://github.com/PreCyz/GitDiffGenerator/wiki/Parameters): inputs, defaults, and configuration precedence.
- [Application settings](https://github.com/PreCyz/GitDiffGenerator/wiki/Application-settings) and [Toolkit details](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-details): application preferences and Toolkit settings.
- [Job window](https://github.com/PreCyz/GitDiffGenerator/wiki/Job-window) and [Reminder job](https://github.com/PreCyz/GitDiffGenerator/wiki/Reminder-job): scheduled uploads and reminders.
- [Custom item name](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-item-name) and [Custom command](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-command): naming patterns and VCS overrides.
- [Tips](https://github.com/PreCyz/GitDiffGenerator/wiki/Tips) and [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting): practical checks and recovery.
- [Building and testing](https://github.com/PreCyz/GitDiffGenerator/wiki/Building-and-testing): build prerequisites, native helper, and packaging.
- [License](https://github.com/PreCyz/GitDiffGenerator/wiki/License): MIT license.

The current execution path is Windows-oriented: Toolkit device SSO requires Windows, Maven selects Windows JavaFX native artifacts, and the shared VCS executor launches `powershell.exe`. Linux-specific producer classes exist, but a non-Windows Java compilation does not establish a working Linux distribution.

Source: [pom.xml](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/pom.xml), [WebView2AuthClient.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/auth/WebView2AuthClient.java).
