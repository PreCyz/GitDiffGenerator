# Examples

These examples use synthetic identities and paths. Replace them with your repository's author information and existing directories. Run from the application directory. Use Java 25, or the bundled `runtime\bin\java.exe` from the portable JAR distribution.

## A saved local-generation configuration

Back up your settings before editing. With Gipter closed, create or merge these sections into `applicationProperties.json`; retain any other configurations you need:

```json
{
  "appConfig": {
    "useUI": false,
    "noSSO": true,
    "uploadItem": false,
    "confirmationWindow": false
  },
  "runConfigs": [
    {
      "configurationName": "local",
      "author": "Anakin Skywalker",
      "itemType": "SIMPLE",
      "projectPath": "C:\\Projects\\Example",
      "itemPath": "C:\\Gipter\\items",
      "periodInDays": 7,
      "fetchAll": false,
      "skipRemote": true
    }
  ]
}
```

Launch the saved configuration:

```powershell
java -jar .\Gipter.jar useUI=N configurationName=local uploadItem=N confirmationWindow=N
```

Keep `noSSO` in the saved application settings and omit its CLI override; see the current limitation in [Gipter from CLI](https://github.com/PreCyz/GitDiffGenerator/wiki/Gipter-from-CLI). This configuration skips Toolkit SSO/uploads. It is for local VCS generation, not Toolkit document downloads.

## Multiple repositories and authors

Using the same saved application settings:

```powershell
java -jar .\Gipter.jar useUI=N configurationName=local 'author=Anakin Skywalker,Obi-Wan Kenobi' 'projectPath=C:\Projects\One,C:\Projects\Two' 'itemPath=C:\Gipter\items' periodInDays=12 uploadItem=N confirmationWindow=N
```

For a Git-specific author, set `gitAuthor` in the run configuration. It takes precedence over the general author list for Git; Mercurial/SVN have their own author options. `committerEmail` adds a Git author-email filter despite its name.

## Explicit historical dates

```powershell
java -jar .\Gipter.jar useUI=N configurationName=local 'author=Anakin Skywalker' 'projectPath=C:\Projects\Example' 'itemPath=C:\Gipter\items' startDate=2026-10-01 endDate=2026-10-14 periodInDays=7 uploadItem=N confirmationWindow=N
```

The explicit `periodInDays` above prevents the saved-period fallback from replacing the explicit start date in the current CLI implementation. `startDate` and `endDate` are not persisted in JSON run configurations. The prefix can be set with `itemFileNamePrefix=review-{START_DATE}-{END_DATE}`.

## Protected output

```powershell
java -jar .\Gipter.jar useUI=N configurationName=local itemType=PROTECTED uploadItem=N confirmationWindow=N
```

This generates commit-summary output using the stored local paths/authors. Review the result before sharing it; custom commands can replace the built-in output behavior.

## Statement file

```powershell
java -jar .\Gipter.jar useUI=N configurationName=local itemType=STATEMENT 'itemPath=C:\Gipter\statement.docx' uploadItem=N confirmationWindow=N
```

`itemPath` identifies an existing statement file, not an output directory. This local example does not upload it. For submission, use the UI with a valid Toolkit session and uploading enabled.

## Toolkit documents and uploads

Use the UI with SSO enabled and sign in as the configured account. Create a `TOOLKIT_DOCS` configuration, choose Toolkit projects, set author/date filters and document list names (for example `Deliverables,DocumentLibrary`), and choose a local output directory. Save and execute it.

Gipter builds a ZIP from relevant document versions. Consecutive changes by the requested author are grouped into before/after pairs; do not assume a single final document is the whole result. The selected document lists and your Toolkit permissions determine what can be read.

To upload VCS output or a statement, likewise re-enable SSO and **Upload item** in application settings and complete sign-in. The generation-only `appConfig` above deliberately disables those capabilities until you change it.

Source: [RunConfig.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/RunConfig.java), [ApplicationConfig.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/ApplicationConfig.java), [CliApplicationProperties.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/CliApplicationProperties.java), [ToolkitDocumentsDiffProducer.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/producers/ToolkitDocumentsDiffProducer.java).
