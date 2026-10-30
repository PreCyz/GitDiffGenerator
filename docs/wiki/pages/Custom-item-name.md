# Custom item names

Set **File name prefix** in the main window to choose the generated item's base name. Despite the field name, a nonempty value supplies the base name rather than always being prepended to the default weekly name. The normal `.txt` or `.zip` extension is added by the generator.

Use case-sensitive `{NAME}` placeholders within your text. For example:

```text
review-{START_DATE}-{END_DATE}
```

For a range from 2026-10-01 through 2026-10-14 this gives `review-2026-10-01-2026-10-14.txt` for SIMPLE output. TOOLKIT_DOCS uses `.zip`.

## Supported values

```text
CURRENT_DATE
CURRENT_YEAR
CURRENT_WEEK_NUMBER
CURRENT_MONTH_NAME
current_month_name
CURRENT_MONTH_NUMBER
START_DATE
START_DATE_YEAR
START_DATE_MONTH_NAME
start_date_month_name
START_DATE_MONTH_NUMBER
START_DATE_WEEK_NUMBER
END_DATE
END_DATE_YEAR
END_DATE_MONTH_NAME
end_date_month_name
END_DATE_MONTH_NUMBER
END_DATE_WEEK_NUMBER
```

`CURRENT_*` uses today's local date. `START_DATE*` and `END_DATE*` use the resolved run dates. Uppercase month-name patterns yield values such as `NOVEMBER`; lowercase variants yield `november`. Numeric months are zero-padded.

For example, on 2019-11-25, `{CURRENT_YEAR}-{CURRENT_MONTH_NAME}` expands to `2019-NOVEMBER`. Week numbers use the default Java locale's week rules; they can differ near year boundaries and between locales. A weekly pattern is not a fixed ISO-week guarantee.

With an empty prefix, today's end date produces a lowercase name such as `2026-october-week-44.txt`; a historical range uses the end-date year/month and compact start/end dates. The exact week number depends on the locale. The generated base name is also used for the Toolkit item title.

Use characters valid in a filename on your platform and omit the extension from your pattern. For STATEMENT, the chosen existing file remains the input; changing a generated-name pattern is not a way to create that statement file.

Custom command placeholders are separate `${name}` values; see [Custom command](https://github.com/PreCyz/GitDiffGenerator/wiki/Custom-command).

Source: [NamePatternValue.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/model/NamePatternValue.java), [ApplicationProperties.java](https://github.com/PreCyz/GitDiffGenerator/blob/af01f8c8/src/main/java/pg/gipter/core/ApplicationProperties.java).
