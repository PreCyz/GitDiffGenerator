# Gipter wiki sources

This `wiki` branch contains the reviewable Markdown sources for Gipter's wiki. Start with [Home](docs/wiki/pages/Home.md).

The pages describe application source version **5.0.8** at commit `af01f8c8` on `codex/webview2-device-sso`, including its committed .NET 10 build update. Release archives may predate that commit. Build commands belong in an application source checkout; this documentation branch retains an old POM and is not the current application build tree.

## Pages

- [Home](docs/wiki/pages/Home.md)
- [Installation and upgrade](docs/wiki/pages/Installation-and-upgrade.md)
- [Toolkit sign in](docs/wiki/pages/Toolkit-sign-in.md)
- [Wizard](docs/wiki/pages/Wizard.md)
- [Main window](docs/wiki/pages/Main-window.md)
- [Application settings](docs/wiki/pages/Application-settings.md)
- [Toolkit details](docs/wiki/pages/Toolkit-details.md)
- [Item types](docs/wiki/pages/Item-types.md)
- [Parameters](docs/wiki/pages/Parameters.md)
- [Gipter from CLI](docs/wiki/pages/Gipter-from-CLI.md)
- [Examples](docs/wiki/pages/Examples.md)
- [Job window](docs/wiki/pages/Job-window.md)
- [Reminder job](docs/wiki/pages/Reminder-job.md)
- [Custom command](docs/wiki/pages/Custom-command.md)
- [Custom item name](docs/wiki/pages/Custom-item-name.md)
- [Tips](docs/wiki/pages/Tips.md)
- [Troubleshooting](docs/wiki/pages/Troubleshooting.md)
- [Building and testing](docs/wiki/pages/Building-and-testing.md)
- [License](docs/wiki/pages/License.md)

## Layout and publishing

- `docs/wiki/pages/` contains the 15 existing wiki pages, four new topic pages, and `_Sidebar.md`. Existing page filenames/public names are preserved.
- Page-to-page links target the public wiki names so the Markdown is ready to copy into the separate Wiki repository. Use the local index above to review the new pages before publication.
- `docs/wiki/UPDATE-NOTES.md` records the source evidence, verification, and current limitations.
- `docs/wiki/*.png` are legacy screenshots retained for existing asset URLs. The revised pages do not reference unverified old UI captures.

GitHub stores the live pages in [GitDiffGenerator.wiki.git](https://github.com/PreCyz/GitDiffGenerator.wiki.git), separately from this branch. Publishing requires copying the reviewed page files to that repository's root and pushing there. Updating `origin/wiki` alone does not publish the pages. No publishing has been performed as part of this local documentation refresh.

## Legacy guides

[ODT guide](docs/Gipter-ui-description.odt) and [PDF guide](docs/Gipter-ui-description.pdf) are retained as **legacy documentation**. They contain older requirements/screens and are not the current 5.0.8 instructions. Use the Markdown pages above for current behavior.

## License

[MIT](LICENSE).
