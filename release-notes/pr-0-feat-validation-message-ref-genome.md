# Protein change validation messages now name the reference genome

## What's New

Validation messages for a queried protein change now say which reference genome's canonical transcript the query was checked against. A reference amino acid mismatch reads, for example, `BRAF A600E: The reference amino acid at position 600 is V instead of A on the OncoKB GRCh37 canonical transcript.` A position past the end of the sequence reads `BRAF V9999E: position 9999 exceeds the BRAF GRCh37 canonical protein length of 766.`

The reference genome named is the one the query was validated against: the queried `referenceGenome`, or GRCh37 when none was given.

## Impact

Some genes have different canonical transcripts on GRCh37 and GRCh38 (for example WT1, where the GRCh37 transcript is five amino acids shorter at the start), so a protein change flagged on one build can be valid on the other. Users and API clients can now see which build a message refers to. Which queries are flagged, and the `errors[].type` values, are unchanged.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `IndicatorQueryResp.errors[].message` | Edit (wording now names the reference genome) | `byProteinChange`, `byHGVSg`, `byGenomicChange` |
| `SomaticVariantAnnotation.proteinChangeValidation.message` | Edit (wording now names the reference genome) | `/api/private/utils/variantAnnotation` |

## Migration / Action Required

None. Clients that string-match `message` should branch on `errors[].type` instead; `message` is prose and is not meant to be parsed.

## Related Links

- PR:
- Issue: https://github.com/oncokb/oncokb/issues/4268
- Docs: [docs/validation-errors.md](../docs/validation-errors.md)
