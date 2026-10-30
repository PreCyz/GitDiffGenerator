# Last-item reminder

The last-item reminder checks Toolkit's latest item modified date and can display a message prompting you to review your submissions. It does not generate or upload an item. The separate scheduled upload workflow is described in [Job window](https://github.com/PreCyz/GitDiffGenerator/wiki/Job-window).

Enable or disable **Last item job** in **Settings → Application**. The default Quartz CRON expression is:

```text
0 5 12 20 * ?
```

This schedules the check on the 20th day of each month at **12:05:00**, using the scheduler's local time zone. The application must be running and have usable Toolkit access for a meaningful check.

To change the expression manually, close Gipter and merge these fields into your existing `applicationProperties.json` (retain the other sections/settings):

```json
{
  "appConfig": {
    "checkLastItemEnabled": true,
    "checkLastItemJobCronExpression": "0 15 14 20 * ?"
  }
}
```

The example moves the check to **14:15:00** on the 20th. The JSON key is `checkLastItemEnabled`; the related command-line option is `checkLastItem`.

The current check compares month numbers and shows a message when no last-item result is available. It is not a complete year-aware proof that all expected submissions exist. A missing result can also reflect access/connectivity problems. Verify the actual Toolkit list when confirming coverage, particularly around the year boundary.

Source: [LastItemJob.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/jobs/LastItemJob.java), [LastItemJobCreator.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/jobs/LastItemJobCreator.java), [ArgName.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/ArgName.java), [ApplicationConfig.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/ApplicationConfig.java).
