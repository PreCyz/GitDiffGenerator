# Upload and scheduling workflows

## First upload to Toolkit

On first launch, finish the wizard with your Toolkit username, then sign in if prompted. The path you choose in the wizard depends on whether you are uploading repository changes, a statement file, or Toolkit documents.

![First-run wizard steps and item-type branches leading to a successful Toolkit upload](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/first-run-toolkit-upload.png)

[Download the editable draw.io diagram](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/first-run-toolkit-upload.drawio)

## Upload with a saved configuration

Select the saved configuration, check its destination and date range, and choose **Execute**. The progress message **Finished with SUCCESS** confirms the upload.

![Manual upload from a saved configuration, with sign-in only when prompted](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/configured-toolkit-upload.png)

[Download the editable draw.io diagram](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/configured-toolkit-upload.drawio)

## Create a scheduled upload job

Open **Job**, choose the configuration, and set a recurrence or Quartz cron expression. Select **Schedule** to save the job.

![Job setup using the recurrence controls or a Quartz cron expression](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/configured-create-job.png)

[Download the editable draw.io diagram](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/configured-create-job.drawio)

## Run a saved job at Windows startup

With **Auto-start** enabled, sign in to Windows and leave Gipter running. If a missed-run prompt appears, **Trigger now** runs the missed upload and **Don’t** skips to the next scheduled run.

![Windows startup with a saved job, showing sign-in, missed-run choices, and the next scheduled upload](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/startup-with-scheduled-job.png)

[Download the editable draw.io diagram](https://raw.githubusercontent.com/PreCyz/GitDiffGenerator/5b6002d807e21bf96f443ec923a3b7f13e4e499e/docs/wiki/diagrams/startup-with-scheduled-job.drawio)
