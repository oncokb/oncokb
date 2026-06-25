# Fix: Propagate "Other Tumor Types" Summaries to Tags and Select Most Specific Tag

## What's New

Two bug fixes in tag evidence resolution:

- **`SummaryUtils`**: When multiple tag evidences exist, the most specific tag (narrowest genomic range by start/end position) is now selected before resolving the tumor type summary.
- **`TagsController`**: When no tumor-type-specific summary is found for a tag, the system now falls back to the "Other Tumor Types" summary.

## Impact

API consumers querying tag-level tumor type summaries for variants that match multiple tags or lack a specific tumor type summary. Previously, summaries may have been missing or incorrectly resolved. Affected responses may now contain populated `description` fields where they were previously empty or null.

## API Changes

None.

## Migration / Action Required

None.

## Related Links

- PR: https://github.com/oncokb/oncokb/pull/4121
- Branch: `fix/tag-other-tumor-types`