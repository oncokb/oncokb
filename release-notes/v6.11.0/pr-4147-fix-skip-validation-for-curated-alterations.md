# Skip protein change validation for alterations OncoKB curates

## What's New

An alteration OncoKB itself curates is no longer checked against the canonical protein sequence. A query naming a curated alteration verbatim — by its alteration or by its name, matched case-insensitively — annotates from that curated alteration, so a check against the sequence could only contradict it.

This fixes a regression introduced with the reference allele check: a curated name can be shaped like a protein change while meaning something else. `AR V7` names the AR-V7 splice isoform, not valine at codon 7. Residue 7 of AR is `L`, so the check read the name as a variant that cannot exist and returned gene-level annotation with a `REFERENCE_ALLELE_MISMATCH` error, leaving the curated annotation unreachable. `AR V7`, `EGFR vIII` and `EGFR EGFRvIII` are annotated as before.

The exemption is curation and nothing else, so it is not a blanket one. An uncurated protein change on the same gene, at the same position, with the same reference residue is still checked and still rejected — `AR V7E` continues to report `REFERENCE_ALLELE_MISMATCH`. The cost is that a wrongly curated alteration goes unreported here: curation is the source of truth for annotation, and validating the curation set belongs in a job over that set rather than in the annotation of a user's query.

The same exemption applies on `/api/private/utils/variantAnnotation`, which leaves `proteinChangeValidation` null for a curated alteration. Germline annotation is unaffected; it does not report these errors.

## Impact

Callers of the somatic annotation endpoints (`/api/v1/annotate/mutations/byProteinChange`, `byHGVSg`, `byHGVSc`, `byGenomicChange`, `/api/v1/annotate/samples`, `/api/v1/search`) that query a curated alteration whose name reads like a protein change get their annotation back instead of a gene-level response with an error. No other query changes: a valid protein change was already annotated, and an uncurated invalid one is still rejected with the same error type.

No self-hosting impact — no configuration, environment variable, database, migration, or Docker/runtime change.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `IndicatorQueryResp.errors` | Edit (existing field, unchanged in shape; a curated alteration no longer produces an entry, so those queries return an empty list and full variant annotation) | `byProteinChange`, `byHGVSg`, `byHGVSc`, `byGenomicChange`, `/api/v1/annotate/samples`, `/api/v1/search` |
| `SomaticVariantAnnotation.proteinChangeValidation` | Edit (existing field, unchanged in shape; now left null for a curated alteration) | `/api/private/utils/variantAnnotation` |

## Migration / Action Required

None. Clients that worked around the rejection of a curated alteration such as `AR V7` can drop the workaround.

## Related Links

- PR: #4147
- Issue:
- Docs:
