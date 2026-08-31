# Validate and normalize a queried protein change against the OncoKB canonical protein sequence

## What's New

Somatic annotation now checks a queried protein change against the OncoKB canonical protein sequence for the queried gene and `referenceGenome`. It extracts the reference residue(s) and position(s) from the alteration and compares them to that sequence. **A protein change that disagrees describes a variant that cannot exist, so the variant is no longer annotated.** The query is annotated as a gene-level query instead — `geneExist`, `geneSummary`, `query.canonicalTranscript` and the rest of the gene-level content are exactly what a query naming that gene and no alteration returns — while the variant-level fields (`variantExist`, `oncogenic`, `variantSummary`, `tumorTypeSummary`, `treatments`, the implication lists, the highest levels) stay at their defaults. The queried alteration is echoed back untouched in `query`, and the new `errors` list names the reason.

The check lives in somatic annotation itself rather than in one endpoint, so it covers every somatic annotation path: `/api/v1/annotate/mutations/byProteinChange` (GET, POST), `/api/v1/annotate/mutations/byHGVSg`, and `/api/v1/annotate/mutations/byGenomicChange`. On the HGVS and genomic change paths the alteration being checked is the protein change OncoKB selected from the Genome Nexus response, and OncoKB already selects it on its own canonical transcript — the same transcript the validation reads the sequence from. Those queries should therefore pass validation.

The canonical sequence differs between GRCh37 and GRCh38 for some genes, so the query is always checked against the sequence for the genome it was asked about. Frameshift queries are checked too: the reference residue of e.g. `V600fs`, `R123Gfs*45` or `Gly7GlufsTer12` is verified like any other protein change.

`errors` is a list of objects, each with a `type` to branch on (`REFERENCE_ALLELE_MISMATCH`, `POSITION_OUT_OF_RANGE`, `REVERSED_POSITION_RANGE`, `MALFORMED_ALTERATION`) and a `message` saying the same thing in words, for a client with nowhere better to get the wording. The two always agree; the `message` is prose and should not be parsed. Both are documented in [docs/validation-errors.md](../docs/validation-errors.md).

The list is **always present and empty when there is nothing wrong**, so a client never has to null-check it, and it is a list rather than a single object because it is the one place every validation OncoKB reports will land — gene-level checks and others are planned, and will add `type` values here rather than a parallel field. The alteration check described here reports at most one error per query. Read the response fields, not the mere presence of an error, for whether the variant was annotated — every `type` above leaves it unannotated, but a later check need not.

Only the validation half runs here: the queried protein change is never rewritten on the somatic endpoints, so a query that passes validation is annotated exactly as before. Queries that could not be checked (the gene has no cached canonical protein sequence) are annotated as usual and report no error — the check is skipped, not failed.

### Example of an annotation that changes: `BRCA1 T100*`

`GET /api/v1/annotate/mutations/byProteinChange?hugoSymbol=BRCA1&alteration=T100*` is a query that changes. Position 100 of the OncoKB canonical BRCA1 sequence is `E`, so the protein change the caller meant is `E100*`; `T100*` describes a variant that cannot exist.

Nothing checked the reference residue before, and nothing downstream needed it either. `T100*` parsed to consequence `stop_gained`, which OncoKB treats as generally truncating, so the query matched BRCA1's curated `Truncating Mutations` entry purely on the position and the `*` — the wrong `T` was simply never consulted. The response was a full variant annotation:

```json
{
  "query": { "hugoSymbol": "BRCA1", "alteration": "T100*" },
  "variantExist": false,
  "errors": [],
  "oncogenic": "Likely Oncogenic",
  "mutationEffect": { "knownEffect": "Likely Loss-of-function" },
  "variantSummary": "The BRCA1 T100* is a truncating mutation in a tumor suppressor gene, and therefore is likely oncogenic."
}
```

The same query now returns gene-level annotation only, with the reason:

```json
{
  "query": { "hugoSymbol": "BRCA1", "alteration": "T100*" },
  "geneExist": true,
  "geneSummary": "BRCA1, a tumor suppressor involved in the DNA damage response, is mutated in various cancer types.",
  "variantExist": false,
  "oncogenic": "Unknown",
  "mutationEffect": { "knownEffect": "Unknown" },
  "variantSummary": "",
  "errors": [
    {
      "type": "REFERENCE_ALLELE_MISMATCH",
      "message": "BRCA1 T100*: The reference amino acid at position 100 is E instead of T on the OncoKB canonical transcript."
    }
  ]
}
```

Correcting the query to `E100*` returns the Likely Oncogenic / Likely Loss-of-function annotation shown above, unchanged. The point of the check is that a typo in the reference residue no longer silently borrows a real annotation from the position it happens to land on.

### Canonical transcript on every somatic annotation

`query.canonicalTranscript` is now filled in by somatic annotation itself, with the OncoKB canonical transcript of the annotated gene for the queried `referenceGenome`. It was previously set only where the annotation came from Genome Nexus (the genomic change and HGVS endpoints, which report the exact transcript Genome Nexus used, and still do — that more specific value continues to win). Protein change queries returned null and now report the gene's canonical transcript, which is the sequence the validation above ran against. It stays null only when OncoKB does not curate the gene.

### `/api/private/utils/variantAnnotation` (website only)

`/api/private/utils/variantAnnotation` is the private endpoint behind the public website at oncokb.org. API clients do not consume it, so this section is documentation of the website's behaviour rather than a client-facing change.

The same validation runs there, but it is reported differently and it is allowed to rewrite the query. When Genome Nexus does not resolve the query to a valid alternative OncoKB variant, the endpoint calls the OncoKB transcript service's find-canonical-sequence (`sequenceType=PROTEIN`) directly, rather than reading the sequence cache the somatic path uses, so it can also report why a check could not be performed.

The outcome is reported through a single nested `proteinChangeValidation` object with a `status` (severity), a `messageType` (specific reason), a human-readable `message`, and, when the query was rewritten before annotation, the `normalizedProteinChange`.

`status` values:
- **`INVALID`** — the query disagrees with the canonical sequence. `message` carries the detail. Covers reference amino acid mismatch (e.g. `A600E` or `A600fs` where position 600 is `V`), range boundaries (both boundaries of e.g. `A237_G238del` are checked), a position beyond the sequence length, a reversed range, and malformed forms. `messageType` is `INVALID_PROTEIN_CHANGE`; which of those it is, is left to the `message`.
- **`NORMALIZED`** — the query was non-standard but unambiguously rewritten, and annotation ran on the normalized form. HGVS specifies a deletion by position only and does not spell out the deleted residues, so a spelled-out deleted sequence is dropped: `A237_G238delAG` → `A237_G238del`, and `A237_G238delAGinsCT` → `A237_G238delinsCT`. `messageType` is `NORMALIZED_DELETED_SEQUENCE`; `normalizedProteinChange` holds the form actually annotated.
- **`UNCHECKED`** — the check could not be performed. `messageType` distinguishes `NO_PROTEIN_SEQUENCE` (the transcript service returned no canonical protein sequence for the gene), `TRANSCRIPT_SERVICE_DISABLED` (the transcript service is disabled), and `TRANSCRIPT_SERVICE_UNAVAILABLE` (the transcript service failed; the query should be retried).

The message text for the `UNCHECKED` reasons is left to the frontend — only the type is returned. The annotation itself is always returned; a spelled-out deletion is annotated on its normalized form. `proteinChangeValidation` is left null when the query agrees with the canonical sequence, resolves to a valid alternative variant, or is not a reference-bearing protein change.

## Impact

On the public somatic annotation endpoints, a query whose reference residue disagrees with the OncoKB canonical protein sequence now comes back with gene-level annotation only and an entry in `errors` naming why. Clients that previously received a variant annotation for such a query will now see the variant-level fields at their defaults; the new field can be ignored by clients that do not care why, since it is an empty list on every other response. Every valid protein change is unaffected.

The validation affects only alterations with a resolvable protein position and reference residue. `/api/v1/annotate/mutations/byProteinChange` is where a client can send one directly and is the endpoint that will see this in practice; the HGVS and genomic change endpoints are covered by the same check, but since OncoKB picks their protein change off its own canonical transcript, those queries should pass validation. Germline annotation is not affected — it matches on cDNA rather than protein change.

The `query.canonicalTranscript` change is wider, since it lives in somatic annotation rather than in one endpoint: every somatic response for a curated gene now carries it. That is purely additive — the field already existed, no other field changes, and where a Genome Nexus transcript was already reported it is still the value returned.

`/api/private/utils/variantAnnotation` is private and consumed only by oncokb.org — API clients do not need to take any action. Its response is unchanged by the somatic annotation work above: it still reports the human-readable `proteinChangeValidation` object and no enum.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `IndicatorQueryResp.errors` | Added (array, never null, empty when there is nothing wrong) | `byProteinChange`, `byHGVSg`, `byGenomicChange` |
| `IndicatorQueryResp.errors[].type` | Added (enum: `REFERENCE_ALLELE_MISMATCH`, `POSITION_OUT_OF_RANGE`, `REVERSED_POSITION_RANGE`, `MALFORMED_ALTERATION`) | `byProteinChange`, `byHGVSg`, `byGenomicChange` |
| `IndicatorQueryResp.errors[].message` | Added (the same reason in words) | `byProteinChange`, `byHGVSg`, `byGenomicChange` |
| `IndicatorQueryResp.query.canonicalTranscript` | Edit (existing field, previously null unless the annotation came from Genome Nexus, now always populated for a curated gene) | `byProteinChange`, `byHGVSg`, `byGenomicChange`, `/api/private/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation` | Added (nullable object) | `/api/private/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.status` | Added (nullable enum: `NORMALIZED`, `INVALID`, `UNCHECKED`) | `/api/private/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.messageType` | Added (nullable enum: `INVALID_PROTEIN_CHANGE`, `NORMALIZED_DELETED_SEQUENCE`, `NO_PROTEIN_SEQUENCE`, `TRANSCRIPT_SERVICE_DISABLED`, `TRANSCRIPT_SERVICE_UNAVAILABLE`) | `/api/private/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.message` | Added (nullable; detail text) | `/api/private/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.normalizedProteinChange` | Added (nullable; the protein change actually annotated when the query was rewritten) | `/api/private/utils/variantAnnotation` |

## Migration / Action Required

On the somatic annotation endpoints, clients that send protein changes with an incorrect reference residue now get a default, unannotated response with an entry in `errors`, instead of an annotation for a variant that cannot exist. None for `/api/private/utils/variantAnnotation`, which is private.

## Related Links

- PR: #4131
- Issue:
- Docs:
