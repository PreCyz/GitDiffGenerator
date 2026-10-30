# Main window

The main window manages run configurations and starts generation/upload operations. Controls and labels follow the selected language/theme; the names below use English.

## Toolkit

The top section shows the configured username, destination folder, and session-expiry information. The folder field is read-only here; edit it in **Settings → Toolkit**. The expiry label reports session status; a failed connection check can open sign-in automatically. See [Toolkit details](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-details) and [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in).

This section uses work-account SSO rather than a password-entry form. Gipter checks the signed-in identity against the configured account. Generation/upload actions are blocked while authentication is in progress.

## Configurations

Use the configuration selector to switch between saved named configurations. **Add config** creates a configuration, **Remove config** removes the selected one, and **Save config** saves the current run settings. Save changes before switching configurations or relying on a scheduled run.

| Section | What to set |
| --- | --- |
| Paths | Optional item filename/pattern, projects, and output location. For a statement, select the existing file instead of an output directory. |
| VCS | General/VCS-specific authors, Git email filter, item type, and Toolkit document list names where relevant. |
| Time frame | Start and end dates. The last-item checkbox is available after a usable Toolkit last-item date is obtained. |
| Additional settings | Delete downloaded document files, remote filtering, repository fetching, and fetch timeout. Availability depends on the item type. |

Project selection differs for local VCS repositories and Toolkit documents. Check the selected projects after changing the item type. See [Item types](https://github.com/PreCyz/GitDiffGenerator/wiki/Item-types) and [Custom item name](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-item-name).

The last-item date comes from the most recent Toolkit item's **modified date**. Selecting it as the next start date is a convenience; review the intended coverage period before submission.

## Actions

| Button | Action |
| --- | --- |
| Execute | Run the current configuration. |
| All-in | Run all saved configurations. |
| Job | Open the schedule window after a run configuration has been saved. |
| Tray | Move to the system tray where supported. |
| Exit | Terminate the application. |

Execute and All-in generate items; whether they also upload depends on **Upload item**, **No SSO**, and valid Toolkit access. Disabling upload does not make Toolkit document retrieval local.

## Menus

**Settings → Application** controls preferences, themes, uploading, and custom commands. **Settings → Toolkit** controls the destination folder and document metadata options.

**Help** provides Wiki, UI description, Upgrade, and Launch wizard. The bundled PDF is a legacy guide and may show obsolete screens; use these wiki pages for current behavior.

Closing the window can minimize it to the tray when that feature is enabled and supported. Jobs run only while Gipter is running; see [Job window](https://github.com/PreCyz/GitDiffGenerator/wiki/Job-window).

Source: [main.fxml](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/resources/fxml/main.fxml), [MainController.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/main/MainController.java), [ToolkitSectionController.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/main/ToolkitSectionController.java), [translation.properties](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/resources/bundle/translation.properties).
