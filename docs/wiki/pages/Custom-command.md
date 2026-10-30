# Custom VCS commands

Open **Settings → Application → Custom command**. Enable the override checkbox for Git, SVN, or Mercurial, enter the corresponding command, then save/close the settings window. Disable the override to restore normal generation for that VCS.

The command's output becomes the generated item content. A custom command is responsible for its own author/date/output filtering; choosing an item type does not sanitize arbitrary custom output.

## Placeholders

Use `${name}` with one of these case-sensitive names:

```text
author
gitAuthor
mercurialAuthor
svnAuthor
committerEmail
startDate
endDate
```

Values come from the current run configuration. Ensure the chosen author field is populated. Dates are formatted as `yyyy-MM-dd`. The command fields offer placeholder autocomplete when entering a brace.

For example, enter this text into the Git custom-command field (not into PowerShell, which has its own `${...}` interpretation):

```text
git log --all --oneline --since=${startDate} --until=${endDate}
```

This is a date-only summary example; it does not restrict the author. Author placeholders resolve from the relevant configured identity; `${author}` uses the first general author. Populate that identity even when preparing a command based on other fields, because the substitution map reads the author values.

Test custom author filtering locally before enabling upload. The current command parser has limited quoting support; embedded quotes and quoted fragments can behave differently from a shell. Prefer the built-in author filters when an override is unnecessary.

The custom-command parser builds argument tokens, then the shared VCS executor passes the command through `powershell.exe -NoProfile -NoLogo -NonInteractive`. PowerShell interpretation and installed tools therefore matter. A pipeline such as `git log | grep ...` depends on a `grep` installation and is not portable Windows guidance. Prefer a VCS command with its own filters, or a script checked under the actual executor. The common executor's explicit `powershell.exe` dependency also limits non-Windows execution despite the platform-specific producer classes.

Filename placeholders use a different syntax and set of values; see [Custom item name](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-item-name).

Source: [CommandPatternValue.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/CommandPatternValue.java), [CustomCommand.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/dao/command/CustomCommand.java), [AbstractDiffProducer.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/producers/AbstractDiffProducer.java).
