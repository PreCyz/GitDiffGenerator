# Configuration wizard

Gipter opens the wizard when the UI starts without a saved named run configuration. You can also open **Help → Launch wizard**.

The current wizard has five steps:

1. **Welcome** introduces the configuration workflow.
2. **Configuration details** asks for a name and item type. An omitted name is assigned the wizard's default name, `wizard-config`.
3. **Committer details** collects author and relevant VCS identity information. For Git, check `git config user.name` and `git config user.email` in the repository rather than assuming they match the work-account login.
4. **Paths** selects the projects and item location. For STATEMENT, choose an existing statement file; repository selection is hidden for that item type. Other item types use an output directory and their relevant project selection.
5. **Finish** completes setup and saves the configuration.

The screens adapt to the selected item type; configure Toolkit project/document lists where TOOLKIT_DOCS needs them. Review and save the resulting configuration in the [Main window](https://github.com/PreCyz/GitDiffGenerator/wiki/Main-window) before running it.

Work-account sign-in is a separate flow and can precede initial configuration. The current wizard has no Toolkit password-entry step. See [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in).

Cancelling exits the wizard without completing the new configuration. When editing an existing installation, return to the main window and check which configuration is selected before executing work.

Source: [WizardLauncher.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/WizardLauncher.java), [LauncherFactory.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/launchers/LauncherFactory.java).
