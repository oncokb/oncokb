# Fix Gene-Specific Alteration Matching Bug For Hgvs Annotation Queries

## What's New

Fixed a bug in somatic and germline annotation lookup where the resolved gene was not always attached when matching curated alterations from HGVSg and HGVSc inputs.
This could cause annotation queries to miss the intended curated alteration or match an alteration from the wrong gene when the alteration string was not unique by itself.

The following germline annotation endpoints were impacted by this bug:

- `/annotate/germline/mutations/byHGVSg` (GET and POST)
- `/annotate/germline/mutations/byGenomicChange` (GET and POST)

## Impact

API clients and users of the annotation endpoints may see corrected germline annotation results for affected HGVS-based queries.

## API Changes

None.

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| None | None | `/annotate/germline/mutations/byHGVSg` (GET, POST), `/annotate/germline/mutations/byGenomicChange` (GET, POST) |

## Migration / Action Required

None.

## Related Links

- PR: #4107
- Issue:
- Docs:
