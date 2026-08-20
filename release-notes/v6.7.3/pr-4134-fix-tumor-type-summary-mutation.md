# Stop the tumor type summary logic from overwriting the query's tumor type

## What's New

Generating a tumor type summary no longer mutates the shared `Query` object. Previously, `SummaryUtils.tumorTypeSummary` ran the query's tumor type through `convertTumorTypeNameInSummary` (which lowercases the name and, for names ending in " Tumor", pluralizes it to "...Tumors" for use in summary prose) and wrote the result back onto the query via `query.setTumorType(...)`. Because the same query object is reused downstream and echoed in the response, this corrupted `query.tumorType` (for example, `Gastrointestinal Stromal Tumor` became `gastrointestinal stromal tumors`). The summary text is unchanged: `CplUtils.annotate` already performs this name conversion internally and prefers the matched OncoTree tumor type, so the pre-conversion and write-back were redundant.

## Impact

Affects annotation requests that include a `tumorType` and produce a tumor type summary (any path through `processQuerySomatic` / `processQueryGermline`).

- **Echoed query corrupted:** the returned `query.tumorType` was lowercased for every tumor type, and additionally pluralized for any tumor type whose name ends in " Tumor" (e.g., Gastrointestinal Stromal Tumor, Wilms Tumor, Germ Cell Tumor). It now echoes the original input unchanged.
- **Dropped evidence in variant annotation:** in `/utils/variantAnnotation` and `/utils/variantAnnotation/germline`, the mutated query was reused for a second evidence lookup. The pluralized name matched no OncoTree code, subtype, or main type, so tumor-type resolution failed and tumor-type-specific treatment/diagnostic/prognostic evidence was propagated away or dropped. For a tumor type ending in " Tumor", `tumorTypes[]` could come back empty. For example, KIT V559D in Gastrointestinal Stromal Tumor returned 0 tumor type entries before the fix and now correctly returns the GIST entry with its Level 1 treatments.

The previous behavior was incorrect; this fix restores the original tumor type value and correct evidence resolution.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `Query.tumorType` | Edit (now echoes the original input instead of a lowercased/pluralized value) | `/annotate/mutations/*`, `/utils/variantAnnotation`, `/utils/variantAnnotation/germline` |
| `tumorTypes[]` / `tumorTypes[].evidences` | Edit (value now populated for tumor types ending in " Tumor" that previously returned empty) | `/utils/variantAnnotation`, `/utils/variantAnnotation/germline` |

## Migration / Action Required

None. No configuration, environment, database, or Docker changes. Self-hosted deployments that cached or depended on the previously mutated (lowercased/pluralized) `query.tumorType` value will now receive the original input string.

## Related Links

- PR: #4134
- Issue:
- Docs:
