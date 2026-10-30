# Toolkit details

Open **Settings → Toolkit** to inspect the configured destination and adjust the supported Toolkit settings.

| Field/control | Editable here? | Purpose |
| --- | --- | --- |
| Username | No | Configured identity used for Toolkit account matching. |
| Folder name | Yes | The destination folder for the user's copyright items. |
| List name | No | The configured copyright-item list. |
| URL | No | Toolkit host information. |
| Link to folder | Clickable | Opens the current Toolkit folder in the default browser. |
| Include author | Yes | Includes author metadata in Toolkit document queries. |
| Include modifier | Yes | Includes last-modifier metadata in Toolkit document queries. |

Changes save on window close or **Escape**. **Ctrl+S** saves and refreshes the displayed folder link. Check the link after changing the folder and confirm it points to the intended destination.

The username can be configured from the main Toolkit section/settings file. It identifies the expected account; the sign-in itself happens in WebView2. A destination folder is not the same setting as the signed-in identity.

The session-expiry display is in the main window's Toolkit section rather than this detail form. Connection/startup checks open sign-in when required. See [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in) for persistent sessions and cancellation.

TOOLKIT_DOCS also needs the correct project selection and document list names in the run configuration. Metadata-inclusion switches affect the document queries; if your Toolkit site rejects an expansion, see [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting).

Source: [toolkitSettings.fxml](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/resources/fxml/toolkitSettings.fxml), [ToolkitSettingsController.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/menu/ToolkitSettingsController.java), [ToolkitSectionController.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/main/ToolkitSectionController.java).
