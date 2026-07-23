# Validate and normalize a queried protein change against the OncoKB canonical protein sequence

## What's New

The private `/utils/variantAnnotation` endpoint now validates protein change queries against the OncoKB canonical protein sequence. When Genome Nexus does not resolve the query to a valid alternative OncoKB variant, it extracts the reference residue(s) and position(s) from the alteration, calls the OncoKB transcript service's find-canonical-sequence (`sequenceType=PROTEIN`) for the queried gene, and checks them against the canonical sequence.

The outcome is reported through a single nested `proteinChangeValidation` object with a `status` (severity), a `messageType` (specific reason), a human-readable `message`, and, when the query was rewritten before annotation, the `normalizedProteinChange`.

`status` values:
- **`INVALID`** — the query disagrees with the canonical sequence. `message` carries the detail. Covers reference amino acid mismatch (e.g. `A600E` where position 600 is `V`), range boundaries (both boundaries of e.g. `A237_G238del` are checked), a position beyond the sequence length, a reversed range, and malformed forms. `messageType` is `INVALID_PROTEIN_CHANGE`.
- **`NORMALIZED`** — the query was non-standard but unambiguously rewritten, and annotation ran on the normalized form. HGVS specifies a deletion by position only and does not spell out the deleted residues, so a spelled-out deleted sequence is dropped: `A237_G238delAG` → `A237_G238del`, and `A237_G238delAGinsCT` → `A237_G238delinsCT`. `messageType` is `NORMALIZED_DELETED_SEQUENCE`; `normalizedProteinChange` holds the form actually annotated.
- **`UNCHECKED`** — the check could not be performed. `messageType` distinguishes `NO_PROTEIN_SEQUENCE` (the transcript service returned no canonical protein sequence for the gene), `TRANSCRIPT_SERVICE_DISABLED` (the transcript service is disabled), and `TRANSCRIPT_SERVICE_UNAVAILABLE` (the transcript service failed; the query should be retried).

The message text for the `UNCHECKED` reasons is left to the frontend — only the type is returned. The annotation itself is always returned; a spelled-out deletion is annotated on its normalized form. `proteinChangeValidation` is left null when the query agrees with the canonical sequence, resolves to a valid alternative variant, or is not a reference-bearing protein change.

## Impact

**This does not affect any of the public API.** `/utils/variantAnnotation` is a private endpoint used by the public website at oncokb.org — API clients do not consume it and do not need to take any action.

Within that private endpoint, the change affects only protein change queries (queries with a resolvable protein position and reference residue). HGVSg and genomic change queries are unaffected.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `SomaticVariantAnnotation.proteinChangeValidation` | Added (nullable object) | `/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.status` | Added (nullable enum: `NORMALIZED`, `INVALID`, `UNCHECKED`) | `/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.messageType` | Added (nullable enum: `INVALID_PROTEIN_CHANGE`, `NORMALIZED_DELETED_SEQUENCE`, `NO_PROTEIN_SEQUENCE`, `TRANSCRIPT_SERVICE_DISABLED`, `TRANSCRIPT_SERVICE_UNAVAILABLE`) | `/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.message` | Added (nullable; detail text) | `/utils/variantAnnotation` |
| `SomaticVariantAnnotation.proteinChangeValidation.normalizedProteinChange` | Added (nullable; the protein change actually annotated when the query was rewritten) | `/utils/variantAnnotation` |

## Migration / Action Required

None

## Related Links

- PR: #4131
- Issue:
- Docs:
