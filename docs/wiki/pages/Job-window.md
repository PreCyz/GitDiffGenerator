# Scheduled upload job

Save at least one run configuration, then open **Job** from the main window. Gipter maintains one configured upload job, which can target several named configurations.

## Define the schedule

1. Select the configurations to run.
2. Choose **Every week**, **Every 2 weeks**, **Every month**, or the **Cron way** tab.
3. Set the offered date/time fields, or a valid Quartz CRON expression.
4. Press **Schedule**. The defined-job area shows the saved schedule.

To change a schedule, edit the fields and press **Schedule** again. **Cancel job** cancels the configured upload job; it is distinct from cancelling a sign-in attempt.

The application and scheduler must remain running for the scheduled execution. Auto-start and tray settings can help keep it available, but they do not create an independent Windows service or run work while the computer is off.

## Authentication and submission

The job checks the Toolkit session and requests sign-in when necessary. It waits for authentication before running the selected configurations. Failed, timed-out, or cancelled sign-in prevents that submission. You may need to be present to complete account/device checks.

Confirm the configurations, destination, authors, and date coverage before relying on a schedule. The job calculates its own date window for the selected schedule; do not assume it uses the exact dates visible in an unrelated manual run. The current monthly/CRON date calculations have limitations noted in [Troubleshooting](https://github.com/PreCyz/GitDiffGenerator/wiki/Troubleshooting).

## Missed executions

After restart, Gipter checks saved job timing and may offer **Trigger now** or **Don't** for a missed execution. Accepting it starts the recovery flow and obtains authentication if needed. It is not an unconditional replay of every occurrence missed while the computer was off.

Check the generated output, Toolkit destination, and logs after a recovered run. Job state and next-fire timing are saved locally; keep that data with the application when moving an installation.

For the separate check that reminds you about recent Toolkit submissions, see [Reminder job](https://github.com/PreCyz/GitDiffGenerator/wiki/Reminder-job).

Source: [JobType.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/jobs/JobType.java), [JobController.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/ui/job/JobController.java), [UploadItemJob.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/jobs/UploadItemJob.java), [JobService.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/jobs/JobService.java).
