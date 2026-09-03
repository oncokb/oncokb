# Fusions are named with the HGVS `::` separator

OncoKB now curates and reports fusions with the HGVS `::` separator (`BCR::ABL1 Fusion`); a fusion queried with the legacy hyphen is still annotated, and is normalized to the `::` form.

## What's New

Fusion names in the OncoKB alteration data use the HGVS `::` separator instead of a hyphen. Annotation accepts both spellings:

- A fusion queried with a single hyphen (`BCR-ABL1 Fusion`) is rewritten to `BCR::ABL1 Fusion` before anything is looked up, and is annotated exactly as the `::` spelling is. The normalized name is what comes back in `query.alteration` and in the generated summaries.
- A fusion queried with more than one hyphen (`H1-4-H2BC5 Fusion`) is not interpreted: HUGO symbols may themselves contain hyphens, so which hyphen separates the partners could only be found by trying every candidate pair against the gene table, and OncoKB asks for the `::` form rather than searching. Such a query is reported as a new `AMBIGUOUS_FUSION_SEPARATOR` validation error and is annotated at the gene level only. Written with the HGVS separator, `H1-4::H2BC5 Fusion`, the same query is annotated normally.
- A name that is a single gene whose HUGO symbol itself contains a hyphen (`COX10-AS1 Fusion`, `HLA-DRB1 Fusion`) has nothing to separate and is left as queried. This is checked against every gene OncoKB knows of, including genes it does not curate: fusion partners are often uncurated symbols such as lncRNAs, IG loci and most HLA genes, and checking only the curated gene table would split them into partners that do not exist.
- The private `/utils/variantAnnotation` endpoint reports an ambiguous name through its existing `proteinChangeValidation` object, as `INVALID` with message type `AMBIGUOUS_FUSION_SEPARATOR`. A rewritten hyphen is not reported: the annotation is complete, and the normalized name is echoed in the response.

Typeahead and annotation search accept both spellings: a keyword typed with a single hyphen is searched for under both.

## Impact

Clients sending fusions with a single hyphen are unaffected in what they get back, except that the fusion name echoed in `query.alteration` and rendered in summary text now uses `::`. Any client that compares those strings against its own hyphenated names needs updating.

Clients sending a fusion whose name contains more than one hyphen — which is only possible when a partner's HUGO symbol contains one — now receive a validation error instead of an annotation, and must send the `::` form. In the current data this affects a small number of curated fusions, including `TRB-NKX2-1`, `IGH-NKX2-1`, `TRA-NKX2-1`, `HLA-DRB1-MET`, `NKX2-5-BCL11B`, `COX10-AS1-NRG1`, `PIK3CA-SOX2-OT` and `PIK3CA-KCNMB2-AS1`.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `errors[].type` value `AMBIGUOUS_FUSION_SEPARATOR` | Added | `/annotate/mutations/byProteinChange` (GET, POST), `/annotate/mutations/byHGVSg`, `/annotate/mutations/byHGVSc`, `/annotate/mutations/byGenomicChange`, `/annotate/samples`, `/search` |
| `proteinChangeValidation.messageType` value `AMBIGUOUS_FUSION_SEPARATOR` | Added | `/utils/variantAnnotation` |
| `query.alteration` for a hyphenated fusion query | Edit | Every somatic annotation endpoint |

## Migration / Action Required

This code depends on the fusion rename in the OncoKB data and must be deployed with or after the data release that contains it. Against data that still uses hyphenated fusion names, fusion queries will not match.

Self-hosted deployments must import the data release containing the renamed fusions along with this version.

## Related Links

- PR:
- Issue:
- Docs: `docs/validation-errors.md`
