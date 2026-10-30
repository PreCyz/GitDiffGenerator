# Application settings

Open **Settings → Application**. The window has application preferences and a **Custom command** tab. Settings are saved on closing the window, and several controls also save immediately; **Ctrl+S** saves explicitly.

| Setting | Behavior |
| --- | --- |
| Confirmation window | Enables completion alerts. CLI/error paths can still involve UI; disabling this is useful for local CLI generation. |
| Minimize to tray | Closing the main window can hide it in the tray. Disabled if the environment does not support the tray. |
| Auto-start | Starts Gipter with the user session where supported; the control depends on tray support/settings. |
| Language | English (`en`) or Polish (`pl`). |
| Theme | Selects one of the themes below. |
| Last item job | Enables the separate Toolkit reminder check. See [Reminder job](https://github.com/PreCyz/GitDiffGenerator/wiki/Reminder-job). |
| Upload item | Enables submission after generation when SSO/access is available. |
| Smart zip | Compresses eligible items larger than 500,000 bytes before upload. |
| No SSO | Skips Toolkit authentication and its upload flow. It cannot grant access to Toolkit documents. |
| Refresh settings | Reloads the application's centrally supplied settings; it is not an upload or a replacement for sign-in. |

**Use UI**, **Silent mode**, and **Preferred argument source** are displayed but disabled in this settings window. Launch/configuration modes are described in [Parameters](https://github.com/PreCyz/GitDiffGenerator/wiki/Parameters) and [Gipter from CLI](https://github.com/PreCyz/GitDiffGenerator/wiki/Gipter-from-CLI). Do not infer editability from an old screenshot.

## Themes

The chooser uses these display names:

| Display name | Underlying theme |
| --- | --- |
| YODA | Default JavaFX/Modena |
| VADER | Dracula |
| SIDIOUS | Primer Dark |
| MAUL | Cupertino Dark |
| DUKU | Nord Dark |
| WINDU | Nord Light |
| KENOBI | Primer Light |
| ASHOKA | Cupertino Light |

Use the chooser to change theme. The stored `uiTheme` value is the displayed name (for example `VADER`), not a dependable command-line override. The current argument extractor always falls back to its default theme; UI mode reads the saved selection.

## Custom commands

Select a VCS override to enable its command field. Unselected overrides leave the normal generator in use. Commands and their placeholders are explained in [Custom command](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-command).

For generation-only CLI setup, use the valid saved-settings example in [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples); the current explicit CLI `noSSO` override has a known limitation.

Source: [ApplicationSettingsController.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/menu/ApplicationSettingsController.java), [UITheme.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/UITheme.java), [translation.properties](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/resources/bundle/translation.properties).
