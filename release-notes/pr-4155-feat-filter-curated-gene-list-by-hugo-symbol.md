# Filter the curated gene list by hugo symbol

`GET /utils/allCuratedGenes` now accepts an optional `hugoSymbol` parameter, returning a single curated gene instead of the full list.

## What's New

Clients that need one gene previously had to download the entire curated gene list — currently 1,137 records across 1,028 genes — and filter it themselves. Passing `hugoSymbol` now returns only the records for that gene.

Gene aliases are accepted and resolve to the curated symbol, so `hugoSymbol=HER2` returns `ERBB2`. Genes curated in both settings return both records; `hugoSymbol=BRCA1` returns its `Somatic` and `Germline` entries.

The symbol is **case-sensitive**. `BRAF` matches, `braf` does not. This matches how gene symbols are keyed internally and keeps symbol and alias lookups consistent with each other.

A symbol that matches no curated gene returns `404` rather than an empty list, so a typo is not reported as a gene without curated content. Omitting the parameter, or sending it empty or blank, returns the full list exactly as before.

Filtering is applied to the cached gene list rather than pushed into the cache lookup, so the cache still holds one entry for the full list rather than one entry per queried gene.

`hugoSymbol` also applies when `version` is supplied, so a historic data version can be filtered to a single gene the same way. Symbols are matched against the archived payload itself, so a gene that existed under a symbol at the time of that release still resolves even if the symbol is no longer current.

The `/utils/allCuratedGenes.txt` endpoint is unchanged and does not accept the parameter.

## Impact

Affects only callers who pass the new parameter. Existing callers see no change in behavior, response shape, or status codes.

Callers adopting the parameter should note two things: matching is case-sensitive, and an unmatched symbol is a `404`, not an empty `200`.

Requests that combine `version` and `hugoSymbol` are filtered against the archived payload for that version, and return `404` when that version contains no matching gene.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `hugoSymbol` (query parameter, optional) | Added | `/utils/allCuratedGenes` |
| `404` response for an unmatched `hugoSymbol` | Added | `/utils/allCuratedGenes` |
| `hugoSymbol` combined with `version` | Added | `/utils/allCuratedGenes` |

No response fields were added, renamed, or removed, and no existing request behavior changed.

## Migration / Action Required

None. The parameter is optional and additive, and the `404` can only be triggered by a request shape that was not previously possible.

No configuration, environment variable, database, migration, Docker, or startup changes, so there is nothing for self-hosted deployments to do.

## Related Links

- PR [#4155](https://github.com/oncokb/oncokb/pull/4155)
