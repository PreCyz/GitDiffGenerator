# Troubleshooting

## Sign-in component missing

Re-extract/reinstall the complete distribution. The helper is `Gipter.AuthHost.exe`, normally in an `auth` directory beside the application. A Maven source build publishes it under `target/webview2-auth`.

For a developer launch, run the native publish step and start from the application root. The helper can also be selected with a JVM `-Dgipter.webview2.helper=...` option before `-jar`; see [Building and testing](https://github.com/PreCyz/GitDiffGenerator/wiki/Building-and-testing).

## WebView2 Runtime missing

Install Microsoft Edge WebView2 Evergreen Runtime, then retry. Installing a .NET runtime alone does not supply the browser runtime. The self-contained helper supplies its own .NET runtime, while developers still need the .NET 10 SDK to build it.

## Wrong account or browser-only access

Check the configured Toolkit username and expected work-account email. Sign in with that account rather than an unrelated account offered by the browser. Gipter rejects a different Toolkit identity.

If sign-in completes but Toolkit rejects Java access, check network access and organizational device/account requirements with IT. A successful browser page alone does not prove the session can be used for application requests.

## Expired or unusable session

Retry in the sign-in window when offered. If that window has closed, restart Gipter with SSO enabled so startup can check the session and request sign-in. The expiry label is a status display, not a refresh control. Session-only cookies do not provide a saved session after the application session ends. Saved data is Windows-user-bound; copying it to another account is not a migration method.

**Cancel** stops the current attempt; **Retry** starts another when offered. Closing/cancelling sign-in does not submit the waiting upload. If another login window/process owns the authentication profile, close that attempt and retry.

## Local output exists but nothing was uploaded

Check **Upload item**, **No SSO**, the destination folder, and the Toolkit session. Generation and submission are separate. A local-generation configuration intentionally skips submission. TOOLKIT_DOCS still needs Toolkit access to assemble its local output.

Review `logs/gipter.log` and the actual Toolkit folder before retrying an upload whose result is uncertain. Do not assume an alert or file's existence proves a new Toolkit item exists.

## Repository or document generation fails

Check that the selected paths exist, the relevant VCS executable is on PATH, the requested author/date filters match real history, and the output directory is writable. Check a custom command with local generation before using it for submissions.

For Toolkit documents, check project selection, document list names, permissions, and author/modifier metadata settings in **Settings → Toolkit**. A site's document-query capabilities may differ. Recheck the query error before changing these options.

## Repositories unavailable after startup

A locked or disconnected drive can cause one warning listing unavailable repositories in the selected configuration. The paths remain configured and appear as **Unavailable** in the project editor, including after saving or searching. Unlock/reconnect the drive and check that each path contains a supported repository, then retry generation without restarting Gipter. If a repository was permanently moved or removed, update or explicitly remove its entry.

An empty project list no longer prevents the main window from opening; add the required repositories before generating code items. Git author/email default controls require an accessible Git repository.

## Portable EXE Upgrade only opens a browser

This is expected in 5.0.9. Download the portable EXE package from the release page, close Gipter, retain configuration/job data, and replace the full distribution manually. For JAR installations, including a JAR launched with its bundled runtime, Upgrade uses the JAR archive updater. See [Installation and upgrade](https://github.com/PreCyz/GitDiffGenerator/wiki/Installation-and-upgrade).

## Job did not run

Keep Gipter running, save/select the intended configurations, check the schedule and system clock/time zone, and complete interactive sign-in if requested. A job cannot upload while the machine/application is off. Use the missed-execution prompt on restart when offered, then verify its output and submission.

The reminder and upload schedules are different jobs. `logs/quartz-job.log` and `logs/gipter.log` help diagnose their execution.

## Current source limitations

These are observations in the documented source, not configuration guarantees:

- **CLI SSO override:** an explicit CLI `noSSO` value reads the UI flag in the current CLI property implementation. For generation-only CLI runs, store `appConfig.noSSO=true` and `uploadItem=false`, then omit the `noSSO` argument; see [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples).
- **Historical start-date precedence:** a saved `periodInDays` can replace an explicit CLI start date if the period argument is omitted. Supply the explicit period argument too when following the historical-range example.
- **CLI exit status:** the CLI launcher exits with `-1` after running. Do not treat it as a reliable distinction between successful and failed generation.
- **Theme override:** select/save the theme through the UI; the current theme argument extractor always returns its default.
- **Monthly/CRON upload date windows:** monthly scheduling subtracts a number of months equal to the current month's day count, and the CRON day calculation uses a narrowing integer conversion. These can produce unexpected coverage dates. Verify scheduled output carefully; use manually reviewed date ranges where needed until corrected.
- **Reminder boundary:** the reminder compares month numbers rather than complete year/month periods. Check Toolkit directly around year boundaries and when no item result is returned.
- **Non-Windows VCS execution:** the shared executor explicitly starts `powershell.exe`. Linux producer classes alone do not make this a working Linux execution path.

These documentation changes do not modify the application behavior. For support, include the application version, distribution type, operation, and relevant error text. Inspect/redact logs before sharing; never include session cookies or other credentials.

Source: [WebView2AuthClient.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/services/auth/WebView2AuthClient.java), [CliApplicationProperties.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/core/CliApplicationProperties.java), [CLILauncher.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/launchers/CLILauncher.java), [UploadItemJob.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/jobs/UploadItemJob.java), [LastItemJob.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/jobs/LastItemJob.java).
