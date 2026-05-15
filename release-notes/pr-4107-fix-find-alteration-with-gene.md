# Title

Fix gene-specific alteration matching for HGVS annotation queries.

## What's New

Updated somatic and germline annotation lookup to attach the resolved gene when matching curated alterations from HGVSg and HGVSc inputs.
This fixes cases where annotation queries could miss the intended curated alteration or match an alteration from the wrong gene when the alteration string was not unique by itself.

## Impact

API clients and users of the annotation endpoints may see corrected germline annotation results for affected HGVS-based queries.

## API Changes

None.

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| None | None | None |

## Migration / Action Required

None.

## Related Links

- PR: #4107
- Issue:
- Docs:
