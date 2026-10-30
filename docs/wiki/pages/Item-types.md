# Item types

The `itemType` option has four supported values:

| Value | Input | Result |
| --- | --- | --- |
| `SIMPLE` | VCS repositories, authors, dates | Full diff text, normally a `.txt` file. |
| `PROTECTED` | VCS repositories, authors, dates | Commit-summary text without the built-in full patch output. |
| `STATEMENT` | An existing statement file at `itemPath` | Uses that file for submission; it does not generate a repository diff. |
| `TOOLKIT_DOCS` | Toolkit projects, document lists, authors, dates, valid Toolkit access | A ZIP containing selected document versions. |

For SIMPLE/PROTECTED, `projectPath` is a comma-separated list of repositories and `itemPath` is the output directory. Git, Mercurial, and SVN must be installed/on PATH when used. Repository permissions and external VCS credentials are handled by those tools.

For STATEMENT, `itemPath` is the existing file, including its filename. A statement is useful when code and commit information cannot be shared. Check that the selected file is the intended document before enabling upload.

TOOLKIT_DOCS reads documents from the configured Toolkit project lists. It selects document history around changes by the requested author, including before/after version pairs. It needs an authenticated session even when uploading the resulting ZIP is disabled.

The current enum contains only the four values above. The former SharePoint-documents item type is not supported.

## Generation and upload are separate

The item type chooses how an item is assembled. **Upload item** determines whether it is submitted; **No SSO** disables the Toolkit authentication/upload path. See [Examples](https://github.com/PreCyz/GitDiffGenerator/wiki/Examples) and [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in).

## Smart ZIP

When enabled, Smart ZIP compresses an item before upload if it is **larger than 500,000 bytes** and does not already have a `.zip` extension. TOOLKIT_DOCS already creates a ZIP. The compression setting does not change the selected item type.

Custom VCS commands override normal generation behavior. Do not assume PROTECTED will remove code emitted by your own command; review custom output before submission.

Source: [ItemType.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/producers/command/ItemType.java), [SmartZipService.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/services/SmartZipService.java), [DiffUploader.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/toolkit/DiffUploader.java).
