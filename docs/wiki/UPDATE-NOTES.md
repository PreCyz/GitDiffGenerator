# Wiki refresh notes

## PR #33 follow-up (2026-11-21)

This follow-up documents the proposed 5.0.9 changes in [PR #33](https://github.com/PreCyz/GitDiffGenerator/pull/33), reviewed at head `e569b2d75dab791217132437b87a964021655fe7`. The application PR is open; this documentation does not claim that the release is published. The original 5.0.8 audit below remains historical evidence for unchanged topics.

- First UI launch saves wizard configuration before Toolkit sign-in, including the selected username. First-run cancellation exits; cancellation from Help returns to the previous configuration. Project chooser cancellation preserves selections.
- Unavailable repositories remain configured, appear as Unavailable in the editor, survive save/search, and produce one warning listing the affected paths for the current configuration. Users can unlock/reconnect a drive and retry generation in the same session. Empty project lists no longer prevent opening the main window, and Git identity defaults use an accessible Git repository.
- Main-window initialization now skips its connectivity check with No SSO enabled. This supersedes the UI generation-only limitation recorded in the original audit below; the explicit CLI override issue is unchanged by this PR.
- Portable EXE Upgrade opens the latest release in the browser without closing Gipter. JAR installations select the JAR `.7z` archive even with a bundled runtime.

Changed page source links are pinned to the reviewed PR head. No application files, screenshots, PDF/ODT guides, or live GitHub Wiki pages are modified. Verification for this follow-up covers page/local link targets, balanced Markdown fences, heading structure, JSON example parsing, the documentation-only diff, and Git whitespace checks. Application tests are not rerun for prose-only changes; PR #33's own test results remain separate evidence.

### PR #35 review corrections

The review aligned build/CLI output filenames with PR #33's 5.0.9 POM and identified the exact build source branch. It moved last-item date guidance out of the unavailable-repositories subsection, clarified that Finish saves wizard setup, and limited the folder-cancellation warning description to the local repository editor. Git default guidance now describes the first detected Git repository rather than implying a search for usable identity values across every Git repository. Upgrade guidance distinguishes the new-version-gated JAR/MSI menu item from the always-enabled portable EXE release link. Added source links point directly to the controllers and services implementing these behaviors.

## Original 5.0.8 refresh

Documentation baseline: application version 5.0.8, commit `af01f8c8` on `codex/webview2-device-sso`, reviewed on 2026-10-29. The committed .NET 10 source change is included. This is not a statement that published release assets have been rebuilt.

## Delivered scope

The 15 existing GitHub Wiki page names are preserved under `pages/`. Four topics have been added: installation and upgrade, Toolkit sign-in, building and testing, and troubleshooting. `_Sidebar.md` links the complete set. The branch README provides local review links and explains publication to the separate Wiki repository.

The original Markdown inventory came from `GitDiffGenerator.wiki.git` at `36098b6`. The documentation worktree began at `origin/wiki` commit `82a307c9`; that branch previously contained legacy README/PDF/ODT/screenshots rather than the live page Markdown.

## Source evidence

All application links in the new pages refer to the application baseline, rather than the old documentation branch's POM.

| Documentation | Checked implementation |
| --- | --- |
| Requirements, installation, build | POM; native project/lock files; installer and publishing scripts; upgrade and backup services |
| Parameters and examples | All 45 `ArgName` entries; extractor; CLI/file/UI property implementations; JSON models and serializers |
| Sign-in and persistence | Main startup; FXWebService; CookiesService; WebView2AuthClient; native protocol, form, and SessionStore |
| UI and wizard | Current default/themed FXML, English/Polish bundles, controllers, UITheme, and five-step WizardLauncher |
| Schedules and reminders | JobController, JobType, UploadItemJob, JobService, LastItemJob and its trigger creator |
| Item behavior and customization | Four-value ItemType enum, VCS producers, Toolkit document generation, SmartZipService, naming/command enums and parser |

## Verification

The refresh checks cover:

- All original page names and the four new topic pages.
- Documentation of all 45 configuration enum entries, all 18 filename placeholders, and all seven custom-command placeholders.
- JSON syntax, property names, types, and item values against the current configuration models; transient run dates are excluded from saved JSON examples.
- Page link targets against the complete page inventory; local files and pinned source links against the checkouts.
- Balanced fences, one top-level heading per file, and Markdown rendering with tables and fenced code enabled.
- PowerShell example parsing and POSIX-shell tokenization; JVM/application option placement and supported application option names.
- Removal of obsolete argument names and unverified screenshot references from the new page content.
- A final documentation-only diff and whitespace check in the wiki worktree.

No live Toolkit sign-in, upload, installer execution, or scheduled submission is required for these documentation checks. CLI examples were syntax/source checked rather than launched through Main, whose startup integrates with external settings/services. Full application regression tests are not rerun for this documentation-only change. The preceding .NET upgrade validation passed 14 native checks, standalone helper publishing, and Maven's native publish step.

## Screenshots and legacy guides

The new pages use verified text descriptions. The old captures include password-era screens and are not referenced as current UI. Existing PNG assets remain unchanged to preserve old asset URLs. No live account/profile was opened to obtain replacement screenshots; fresh synthetic-data captures can be a later visual improvement.

The PDF/ODT guides are preserved and explicitly marked as legacy in the branch README. They were not regenerated.

## Existing application limitations surfaced during review

The pages describe these observed limits rather than introducing fixes:

- Explicit CLI `noSSO` reads the UI flag; the saved JSON setting is used by the local-generation examples.
- Main-window connectivity checks can initiate sign-in despite the No SSO preference; the generation-only example uses CLI mode.
- A saved period can override an explicit CLI start date unless a period argument is also supplied.
- CLI always exits with `-1`, so output/log review is required to determine success.
- The theme argument extractor returns the default; the UI reads saved display-name selections.
- Monthly upload coverage subtracts the month's day count as months. CRON coverage narrows milliseconds to an integer before converting to days. The docs do not promise correct monthly/CRON coverage.
- Reminder checks compare month numbers without a year-aware period check.
- The current Maven POM selects Windows JavaFX native classifiers, and the shared VCS executor launches `powershell.exe` even for platform-specific producer classes. A non-Windows compile does not establish a working Linux distribution or device SSO.

These are follow-up implementation concerns outside this wiki refresh. Review `Troubleshooting.md` for user-facing guidance.

## Publication status

Documentation changes are submitted for review through a pull request targeting the wiki branch. Live GitHub Wiki publication remains a separate step; no live Wiki pages have been changed. Page-to-page links use preserved public Wiki names; the README supplies local file links for review. Publishing would copy the page Markdown and sidebar to the root of the separate Wiki repository after review.
