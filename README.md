# Gipter wiki sources

This `wiki` branch contains the reviewable Markdown sources for Gipter's wiki. Start with [Home](docs/wiki/pages/Home.md).

The pages include the proposed **5.0.9** behavior from [PR #33](https://github.com/PreCyz/GitDiffGenerator/pull/33), reviewed at commit `e569b2d75dab791217132437b87a964021655fe7`. PR #33 is still open; these updates do not imply that 5.0.9 has been released. Unchanged topics retain their pinned 5.0.8 source references. Release archives may predate these changes. Build commands belong in an application source checkout; this documentation branch retains an old POM and is not the current application build tree.

## Pages

- [Home](docs/wiki/pages/Home.md)
- [Upload and scheduling workflows](docs/wiki/pages/Upload-and-scheduling-workflows.md)
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

- `docs/wiki/pages/` contains the wiki pages and `_Sidebar.md`.
- `docs/wiki/diagrams/` contains editable draw.io sources and PNG previews. The workflow page links to a specific revision of these assets so the images also render in the separate GitHub Wiki.
- Page-to-page links target the public wiki names so the Markdown is ready to copy into the separate Wiki repository. Use the local index above to review the new pages before publication.
- `docs/wiki/UPDATE-NOTES.md` records the source evidence, verification, and current limitations.
- `docs/wiki/*.png` are legacy screenshots retained for existing asset URLs. The revised pages do not reference unverified old UI captures.

GitHub stores the live pages in [GitDiffGenerator.wiki.git](https://github.com/PreCyz/GitDiffGenerator.wiki.git), separately from this branch. Publishing requires copying the reviewed page files to that repository's root and pushing there. Updating `origin/wiki` alone does not publish the pages. No publishing has been performed as part of this local documentation refresh.

## Legacy guides

[ODT guide](docs/Gipter-ui-description.odt) and [PDF guide](docs/Gipter-ui-description.pdf) are retained as **legacy documentation**. They contain older requirements/screens and are not the current application instructions. Use the Markdown pages above for current behavior.

## License

[MIT](LICENSE).
