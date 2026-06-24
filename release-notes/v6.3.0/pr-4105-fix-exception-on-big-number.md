# Fixed an exception when parsing very large protein change numbers.

## What's New

Updated the protein change parser to handle very large numeric values without throwing an exception. Added test coverage for large-number parsing behavior.

## Impact

Users and downstream systems that submit or process alterations with very large protein-position numbers may be affected. This change improves parser stability for those edge cases.

## API Changes

None

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| None | None | None |

## Migration / Action Required

None

## Related Links

- PR: #4105
- Issue:
- Docs:
