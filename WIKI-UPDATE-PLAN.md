# Wiki update plan

Status: Approved by the user; implemented locally. See [refresh notes](docs/wiki/UPDATE-NOTES.md) for verification and limitations.

## Baseline and workspace

- Worktree: `C:\work\workspace\Other\GitDiffGenerator-wiki`.
- Branch: `wiki`, tracking `origin/wiki`, starting at `82a307c9` (documentation for 4.1.0). The remote was fetched before creating the worktree.
- Implementation baseline: `codex/webview2-device-sso` at `af01f8c8`, application version 5.0.8, including the committed .NET 10 update.
- At planning time, the .NET 10 changes were uncommitted. They are now committed at `af01f8c8`. The pages describe current source requirements without claiming published release assets were rebuilt.
- The `wiki` branch contains README, PDF/ODT documentation, and screenshots. GitHub stores the 15 published Markdown wiki pages separately at `https://github.com/PreCyz/GitDiffGenerator.wiki.git`.
- The published wiki was read into the ignored application directory `target/wiki-audit-source`; its baseline is `36098b6`. This is an audit copy, not the editing workspace.

## Proposed file organization

Stage the existing wiki pages under `docs/wiki/pages/` in the requested worktree, retaining their existing filenames and public page names. Add four new topic pages and a `_Sidebar.md` navigation file. Keep image assets under `docs/wiki/`. Update the branch README to explain the layout, link the staged pages, and identify the application source branch for build instructions.

This lets the complete change be reviewed in the requested branch. Copying or pushing these pages to the separate GitHub Wiki repository is a later publishing action, outside this execution plan.

## Page-by-page changes

| Priority | Existing page(s) | Planned changes |
| --- | --- | --- |
| 1 | Home | Replace Java 8/11 installation guidance with a concise current overview; link installation, sign-in, usage, and development topics. Distinguish bundled runtime distributions from a standalone JAR. |
| 1 | Parameters | Rebuild the reference from `ArgName`, `ArgExtractor`, and configuration models. Use `itemType` instead of obsolete `uploadType`, `uploadItem` instead of `itemUpload`, remove `toolkitPassword`, and verify every documented default and supported value. Explain CLI names versus JSON property names and application/run/Toolkit scopes. Include themes, noSSO, folder settings, reminder settings, fetch timeout, and Smart ZIP where applicable. Clearly identify internal-only options rather than recommending them for normal use. |
| 1 | Gipter-from-CLI | Correct Java 25 launch examples, quoting and Windows paths, bundled-runtime commands, option precedence and JSON configuration. Explain generation-only use and the actual authentication requirements for uploads. Avoid promising unattended first-time sign-in. |
| 1 | Item-types | Document the four implemented values: SIMPLE, PROTECTED, STATEMENT, TOOLKIT_DOCS. Remove SHARE_POINT_DOCS. Explain outputs, input requirements, document version pairs, and Smart ZIP behavior. |
| 1 | Examples; Tips | Replace password-based examples with supported SSO/setup guidance. Supply valid JSON and current CLI examples for generation-only use, multiple repositories/authors, explicit dates, protected output, statements, and Toolkit documents. Fix obsolete option names, trailing commas, and path escaping. |
| 2 | Main-window | Align sections, controls, save behavior, individual/all-configuration execution, generate/upload settings, last-item dates, and project selection with current FXML/controllers. Replace credential-entry guidance with the current Toolkit identity/sign-in flow. |
| 2 | Application-settings | Document the current settings, English/Polish language selection, supported themes, startup/tray behavior, confirmation alerts, Smart ZIP/upload controls, and links to the custom-command/reminder topics. Verify which settings are actually editable and how changes are saved. |
| 2 | Toolkit-details | Describe the editable Toolkit folder and links, configured user/expected account, session state, and sign-in/refresh behavior according to current controls. |
| 2 | Wizard | Match the current five-step wizard, including conditional behavior for item types. Remove the old password-entry screen and explain the separate SSO flow. |
| 2 | Job-window; Reminder-job | Verify weekly/fortnightly/monthly/Quartz CRON scheduling, selected configurations, application-running requirement, missed executions, and authentication before scheduled uploads. Retain the verified default reminder expression `0 5 12 20 * ?`; distinguish reminder checks from upload jobs. |
| 3 | Custom-command | Verify supported placeholders and autocomplete against `CommandPatternValue` and command execution. Replace shell-dependent examples with platform-appropriate commands and correct quoting. |
| 3 | Custom-item-name | Verify supported placeholders against `NamePatternValue` and the filename implementation; fix the November example that currently expands to AUGUST. Explain case and date/week behavior without assuming a fixed locale. |
| 3 | License | Check the MIT license link and preserve the existing licensing statement. |

## New topic pages

1. `Installation-and-upgrade.md`: portable EXE, portable JAR, and MSI workflows; bundled Java runtime versus standalone JAR requirements; Windows/WebView2 requirements; upgrade/restart/backup behavior verified against installer and upgrade code. Explain platform limits accurately: Java generation paths and Windows-only Toolkit device SSO are different capabilities.
2. `Toolkit-sign-in.md`: Windows work-account SSO using native WebView2, expected-account matching, cancel/retry, startup continuation without an application restart, persistent sessions encrypted for the Windows user, and session-only cookie limits. Remove advice to store Toolkit passwords or create an empty cookie file. Use synthetic identities and explain user-visible behavior without exposing credentials.
3. `Building-and-testing.md`: JDK 25, JavaFX 25.0.5, Maven, .NET 10 SDK on PATH for Windows builds, Maven/NuGet access; `mvn clean package`, automatic Windows native publishing, standalone helper publishing, native regression command, and non-Windows build differences. Separate development/build dependencies from end-user dependencies. Explain that commands run in an application source checkout, not the documentation-only wiki branch. Put distribution-packaging prerequisites in a separate subsection verified against the installer scripts.
4. `Troubleshooting.md`: missing helper, missing WebView2 Runtime, wrong signed-in account, expired/unusable saved session, sign-in cancellation/failure, generation versus upload errors, and scheduled-job prerequisites. Reference supported UI recovery actions and logs; do not recommend cookie/password workarounds or unsupported platform behavior.

## Screenshots and existing guides

- Inventory the existing screenshot references and compare them with current FXML, translations, and available UI.
- Replace materially stale images for the main window, application settings/themes, Toolkit settings, wizard, job window, and new sign-in flow when captures can be made safely with synthetic data.
- If an accurate capture is unavailable, remove the misleading screenshot from the rewritten page and provide verified text. Do not present old password screens as current.
- Keep existing screenshot filenames where they still identify the same UI and preserve asset URLs used by existing pages where practical.
- The PDF/ODT guide is outside the wiki-page refresh. Preserve those files and label them as legacy in the branch README rather than implying they describe 5.0.8.

## Evidence and verification

Use application code as the primary authority; cross-check release notes and the latest README. The old wiki and PDF/ODT are inventory sources, not behavior specifications.

Key evidence:

- `pom.xml`, native project files, lock file, and `installer/*.ps1`: build/runtime requirements, publishing, and distributions.
- `core/ArgName.java`, `ArgExtractor.java`, application property implementations, and `core/model/*Config.java`: supported inputs, defaults, persistence, and precedence.
- `core/producers/command/ItemType.java`, command implementations, `NamePatternValue.java`, `CommandPatternValue.java`, and `SmartZipService.java`: item behavior, placeholders, and compression.
- Current FXML, translation bundles, wizard/controllers, jobs, and upgrade services: UI labels and operational workflows.
- `FXWebService.java`, `CookiesService.java`, `services/auth/*`, and `native/webview2-auth/*`: sign-in, helper lookup, identity checks, and session persistence.
- Release notes 4.2.0 through 5.0.8: change history, confirmed against implementation.

Before delivering the documentation diff:

- Validate internal links, preserved wiki names, image paths, headings, and Markdown rendering.
- Parse every JSON example. Check every parameter, default, item type, and placeholder against the code.
- Check shell syntax and argument placement for CLI/build examples. Execute only local, non-uploading examples where appropriate; no real Toolkit changes are required.
- Search rewritten documentation for obsolete Java 8/11 requirements, toolkitPassword, uploadType, itemUpload, SHARE_POINT_DOCS, password-based wizard instructions, cookie-file bypass advice, and automatic restart claims.
- Distinguish released behavior from the pending .NET 10 source changes throughout.
- Review the final diff to ensure it contains documentation/assets only. Existing application code changes remain in the application worktree.

## Execution order and approval boundary

1. After approval, import the 15 current wiki pages into `docs/wiki/pages/` and add navigation.
2. Correct installation, parameters, item types, CLI/examples, and authentication first.
3. Update UI, jobs, customization, and screenshot references.
4. Add development and troubleshooting pages; update the branch README and legacy-guide labels.
5. Validate and present the complete local documentation diff and remaining evidence limits.

The user approved this scope and local implementation is complete. A subsequent instruction authorized committing, pushing, and opening a pull request targeting wiki. Publication to the live GitHub Wiki remains a separate step.
