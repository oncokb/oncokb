# Add All Genomic Indicators Export Endpoints

## What's New

Added two new public utility export endpoints for genomic indicators:

- `GET /utils/allGenomicIndicators` (JSON)
- `GET /utils/allGenomicIndicators.txt` (text)

Historical versions can be fetched by passing the `version` query parameter on
these endpoints.

For local/self-hosted development, `version` downloads will not work without access
to the OncoKB historical data archive on GitHub. Historical data can still be
downloaded from the [OncoKB website](https://www.oncokb.org/data-download).

## Impact

This affects API consumers who need bulk genomic indicator exports in JSON or
tab-delimited text formats. Consumers can now retrieve current data or
historical release snapshots using `version`.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints                             |
| -------------------- | --------------------------- | ------------------------------------- |
| Endpoint             | Added                       | `GET /utils/allGenomicIndicators`     |
| Endpoint             | Added                       | `GET /utils/allGenomicIndicators.txt` |

## Migration / Action Required

None.

## Related Links

- PR [#4126](https://github.com/oncokb/oncokb/pull/4126)
