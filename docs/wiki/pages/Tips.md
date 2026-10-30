# Tips

- Save a named run configuration before scheduling it or switching to another one. Application settings and Toolkit settings have their own save/close behavior.
- Launch with the application's directory as the working directory. `applicationProperties.json`, local job data, and `logs` use relative paths.
- Separate author identity from Toolkit sign-in identity. VCS author filters determine whose changes are generated; SSO determines access to the destination/site.
- Keep a portable installation's runtime and native helper with its JAR/EXE. Use the bundled runtime to avoid accidentally selecting an older system Java.
- Use ordinary Windows backslashes in PowerShell arguments and doubled backslashes in JSON strings. Quote whole `name=value` arguments containing spaces.
- For local generation, start with [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples). CLI mode alone does not disable uploads or sign-in, and the explicit CLI SSO flag currently has a limitation.
- Review the time frame when reusing a configuration. Explicit JSON dates are not persisted by the current run model; scheduled jobs calculate their own windows.
- Smart ZIP applies before upload to eligible files larger than 500,000 bytes. Toolkit document output is already a ZIP.
- Use [Custom item name](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-item-name) for reproducible date-based names and [Custom command](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-command) for intentional VCS overrides. Review overridden output before submission.
- The latest-item display uses Toolkit's modified date. A modified item is not necessarily a newly submitted item.
- Keep a backup of the settings and local data before manually replacing a distribution. Use [Installation and upgrade](https://github.com/PreCyz/GitDiffGenerator/wiki/Installation-and-upgrade) for the supported upgrade entry point.
- Use [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting) for sign-in and job failures; inspect generated files and logs rather than relying only on a process exit code.

Source: [DaoConstants.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/dao/DaoConstants.java), [RunConfig.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/RunConfig.java), [SmartZipService.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/SmartZipService.java).
