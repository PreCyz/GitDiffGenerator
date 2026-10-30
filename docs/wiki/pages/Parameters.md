# Parameters and configuration

These tables cover every entry in the current `ArgName` enum. Defaults shown are **base defaults**, before saved configuration, property-mode rules, or platform behavior apply. An enum entry is not always a working CLI override; exceptions are stated explicitly.

Command-line options use `name=value` after the JAR. Boolean arguments normally accept Y/N or true/false; JSON uses actual `true`/`false` values. Use the settings UI for Toolkit document-metadata switches where the argument converters differ.

## Settings file and scopes

`applicationProperties.json` is read from the working directory. Its main sections are `appConfig`, `toolkitConfig`, and a `runConfigs` array. A run configuration describes authors, repositories/documents, output, and the period. Application settings control execution/preferences; Toolkit settings identify the user and destination.

`startDate` and `endDate` are transient in the JSON run model; pass historical dates as arguments for a specific run. Do not assume arbitrary enum names become persistent JSON properties.

Important CLI/JSON differences:

| CLI concept | Saved JSON location/property |
| --- | --- |
| `checkLastItem` | `appConfig.checkLastItemEnabled` |
| `loggerLevel` | Logging level is transient; the startup argument controls diagnostic logging. |
| `uiTheme` | `appConfig.uiTheme` stores a UI display name, such as `VADER`; choose via UI. |
| `startDate`, `endDate` | Transient run dates, not persisted. |
| `itemType`, `uploadItem`, `noSSO` | Same names in their relevant run/application sections. |

See [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples) for a valid complete local-generation file. Configuration defaults describe fallback behavior, not recommended identity/path values.

## Run configuration

| Name | Base default | Meaning and limits |
| --- | --- | --- |
| `configurationName` | Empty | Name of a saved run configuration; choose unique nonempty names. |
| `author` | `NO_AUTHORS` | Comma-separated general authors; missing identity is represented by `NO_AUTHORS` until configured. VCS-specific authors can take precedence. |
| `gitAuthor` | Empty | Git-specific author filter, overriding the general author list for Git. |
| `mercurialAuthor` | Empty | Mercurial-specific author filter. |
| `svnAuthor` | Empty | SVN-specific author filter. |
| `committerEmail` | Empty | Additional Git author-email filter; despite the name it is added as a Git author filter. |
| `itemType` | `SIMPLE` | SIMPLE, PROTECTED, STATEMENT, or TOOLKIT_DOCS. |
| `itemPath` | `NO_ITEM_PATH` | Existing output directory, or an existing input file for STATEMENT. The base default is a missing-input sentinel. |
| `projectPath` | `NO_PROJECT_PATH` | Comma-separated project paths. VCS items need repositories; Toolkit documents use their project selection. The base default is a sentinel. |
| `itemFileNamePrefix` | Empty | Generated base name/pattern; empty uses the default date/week name. See Custom item name. |
| `periodInDays` | `7` | Lookback period for a relative date range; resolved date behavior depends on property mode. |
| `startDate` | Today minus 7 days (base default) | Explicit ISO date, `yyyy-MM-dd`. Run model dates are transient and not persisted to JSON. |
| `endDate` | Today | Explicit ISO date, `yyyy-MM-dd`; not persisted to JSON. |
| `skipRemote` | `Y` | Y omits the built-in Git remote restriction; N adds `--remotes=origin*`. Both forms retain `--all`; Y is not a local-branches-only guarantee. |
| `fetchAll` | `N` | Fetch repositories before generating. Unlike old guides, the default is N. |
| `fetchTimeout` | `60` | Timeout in seconds for repository fetching. |
| `toolkitProjectListNames` | `Deliverables` | Comma-separated Toolkit document-list display names; use the list URL name if needed. |
| `deleteDownloadedFiles` | `Y` | Delete downloaded temporary documents after producing the Toolkit document item. |

## Application preferences

| Name | Base default | Meaning and limits |
| --- | --- | --- |
| `confirmationWindow` | `Y` | Completion confirmation preference. Disable for local CLI examples; not a guarantee that error paths are headless. |
| `preferredArgSource` | `CLI` | CLI, FILE, or UI property selection; see precedence notes below. |
| `useUI` | `Y` | UI/CLI launch selection; does not itself disable authentication. |
| `activeTray` | `Y` | Tray preference where supported. CLI property mode returns false regardless of this base default. |
| `silentMode` | `N` | Startup/minimized behavior; displayed but disabled in the application settings form. |
| `enableOnStartup` | `Y` | User-session auto-start preference where supported. CLI property mode returns false. |
| `loggerLevel` | Empty | Optional logging level such as INFO or DEBUG; avoid sharing unreviewed diagnostic output. |
| `checkLastItem` | `Y` | Enable the separate reminder check. JSON property: `checkLastItemEnabled`. |
| `checkLastItemJobCronExpression` | `0 5 12 20 * ?` | Quartz CRON schedule for the reminder check. |
| `uiLanguage` | `en` | Supported UI language code `en` or `pl`. |
| `uploadItem` | `Y` | Enable item submission; also requires SSO/access. JSON property uses this same name. |
| `smartZip` | `Y` | Compress eligible non-ZIP items larger than 500,000 bytes before upload. |
| `noSSO` | `N` | Skip SSO/upload flow. Current explicit CLI override is unreliable; store the JSON appConfig value and omit its CLI override. |
| `uiTheme` | `DEFAULT` (UI display: YODA) | Choose via the UI; saved values are display names such as VADER. The current CLI theme extractor ignores an override. |

## Toolkit identity and documents

| Name | Base default | Meaning and limits |
| --- | --- | --- |
| `toolkitUsername` | Operating-system username | Configured username; expected sign-in email is username plus the configured email domain. |
| `toolkitFolderName` | Operating-system username | Destination folder name; edit in Settings → Toolkit. Folder and identity can differ. |
| `toolkitFileAuthorIncluded` | `Y` | Include author expansion in Toolkit document queries. Prefer the UI/JSON boolean; current ToolkitConfig argument conversion uses Boolean.parseBoolean. |
| `toolkitFileModifiedByIncluded` | `Y` | Include modifier expansion in Toolkit document queries. Prefer the UI/JSON boolean. |

## Advanced integration and internal entries

| Name | Base default | Meaning and limits |
| --- | --- | --- |
| `toolkitCopyListName` | `WorkItems` | Integration constant in the current extractor; naming an argument does not make the list freely overridable. |
| `toolkitHostUrl` | `https://goto.netcompany.com` | Integration host. Host overrides do not consistently recompute every derived URL; do not use as a complete site-migration mechanism. |
| `toolkitCopyCase` | `/cases/GTE106/NCSCOPY` | Integration case path; the current extractor returns its default rather than a general override. |
| `toolkitUserFolderUrl` | Default host + case + `/Lists/WorkItems/` | Derived integration base, with the resolved user/folder appended by property implementations. Not a generic direct CLI URL override. |
| `toolkitSiteAssetsUrl` | Default host + case + `/SiteAssets/` | Derived integration settings location; not a normal end-user option. |
| `emailDomain` | `@netcompany.com` | Expected-account email suffix. Advanced integration setting; not a destination-folder setting. |
| `githubToken` | Empty | Optional advanced GitHub access setting; can also come from centrally supplied settings. Never copy real tokens into examples. |
| `upgradeFinished` | `N` | Internal upgrade/restart marker, not a normal user setting. |
| `flowType` | `REGULAR` | Internal lifecycle routing, including initialization and job flows; do not use it to bypass authentication. |


## Precedence and mode limits

With `preferredArgSource=CLI` and `useUI=N`, explicit CLI values generally win, then saved values, then defaults. `preferredArgSource=FILE` gives stored values priority. UI mode uses the saved configuration and applies UI-specific behavior; `preferredArgSource=UI` selects that implementation.

These are not interchangeable property implementations. In particular, the current CLI implementation reads the wrong flag for an explicit `noSSO` override, can apply a saved lookback period over an explicit start date when the period argument is absent, and suppresses tray/startup preferences. The current theme argument extractor returns its default. Follow the specific examples rather than assuming a blanket precedence rule covers these exceptions.

`skipRemote=Y` removes a remote restriction; it does not remove Git's `--all`. `fetchAll=N` and `confirmationWindow=Y` are the current base defaults.

Supported item values are SIMPLE, PROTECTED, STATEMENT, and TOOLKIT_DOCS. Configure Toolkit sign-in through [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in); there is no supported Toolkit password argument in the current enum.

See [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting) for source limitations and [Application settings](https://github.com/PreCyz/GitDiffGenerator/wiki/Application-settings) for UI editability.

Source: [ArgName.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/ArgName.java), [ArgExtractor.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/ArgExtractor.java), [CliApplicationProperties.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/CliApplicationProperties.java), [RunConfig.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/RunConfig.java), [ApplicationConfig.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/ApplicationConfig.java).
