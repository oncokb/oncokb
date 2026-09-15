# Fusions are named with the HGVS `::` separator

OncoKB now curates and reports fusions as `BCR::ABL1 Fusion`; hyphenated queries are still accepted and normalized to `::`.

## What's New

- A fusion queried with a hyphen (`BCR-ABL1 Fusion`) is rewritten to `BCR::ABL1 Fusion` and annotated as usual.
- Gene symbols can contain hyphens, so a name with several hyphens (`H1-4-H2BC5 Fusion`) is rewritten only when exactly one split gives two known genes. Otherwise it gets an `AMBIGUOUS_FUSION_SEPARATOR` validation error and only the gene is annotated.
- A single hyphenated gene (`COX10-AS1 Fusion`) is left as queried, whether or not OncoKB curates it.
- Typeahead and search accept both spellings.
- On the private `/utils/variantAnnotation` endpoint, `proteinChangeValidation` is renamed to `variantValidation`, since it now covers fusions too.

## Impact

Fusion names in `query.alteration` and summaries now use `::`. Clients that compare them against hyphenated names need updating. A fusion name whose partners can't be told apart now returns a validation error instead of an annotation.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `errors[].type` value `AMBIGUOUS_FUSION_SEPARATOR` | Added | `/annotate/mutations/byProteinChange` (GET, POST), `/annotate/mutations/byHGVSg`, `/annotate/mutations/byHGVSc`, `/annotate/mutations/byGenomicChange`, `/annotate/samples`, `/search` |
| `query.alteration` for a hyphenated fusion query | Edit | Every somatic annotation endpoint |
| `proteinChangeValidation` renamed to `variantValidation` | Edit | `/utils/variantAnnotation` |
| `variantValidation.messageType` value `AMBIGUOUS_FUSION_SEPARATOR` | Added | `/utils/variantAnnotation` |

## Migration / Action Required

Deploy with or after the data release that renames fusions to `::`; against older data, fusion queries will not match. Self-hosted deployments must import that data release alongside this version.

`/utils/variantAnnotation` is used only by oncokb.org, so its rename needs no action from API clients.

## Related Links

- PR: https://github.com/oncokb/oncokb/pull/4145
- Issue:
- Docs: `docs/validation-errors.md`
