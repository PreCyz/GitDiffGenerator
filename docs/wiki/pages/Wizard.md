# Configuration wizard

Gipter opens the wizard when the UI starts without a saved named run configuration. You can also open **Help → Launch wizard**.

The current wizard has five steps:

1. **Welcome** introduces the configuration workflow.
2. **Configuration details** asks for a name, item type, and Toolkit username. An omitted name is assigned the wizard's default name, `wizard-config`.
3. **Committer details** collects author and relevant VCS identity information. For Git, check `git config user.name` and `git config user.email` in the repository rather than assuming they match the work-account login.
4. **Paths** selects the projects and item location. For STATEMENT, choose an existing statement file; repository selection is hidden for that item type. Other item types use an output directory and their relevant project selection.
5. **Finish** completes setup and saves the configuration.

The screens adapt to the selected item type; configure Toolkit project/document lists where TOOLKIT_DOCS needs them. Review and save the resulting configuration in the [Main window](https://github.com/PreCyz/GitDiffGenerator/wiki/Main-window) before running it.

On first UI launch, finish and save the wizard before Toolkit sign-in opens. Sign-in uses the account selected during setup and continues startup in the same session. With No SSO enabled, startup skips Toolkit sign-in. The current wizard has no Toolkit password-entry step. See [Toolkit sign in](https://github.com/PreCyz/GitDiffGenerator/wiki/Toolkit-sign-in).

Cancelling the first-run wizard closes Gipter without completing setup. When launched from Help on an existing installation, cancellation returns to the previously selected configuration. Cancelling a project folder chooser returns to the project list with the existing selections intact and logs one warning; it does not cancel the whole wizard.

Source: [WizardLauncher.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/ui/WizardLauncher.java), [LauncherFactory.java](https://github.com/PreCyz/GitDiffGenerator/blob/e569b2d75dab791217132437b87a964021655fe7/src/main/java/pg/gipter/launchers/LauncherFactory.java).
