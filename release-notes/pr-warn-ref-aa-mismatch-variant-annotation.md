# Warn when a queried protein change disagrees with the OncoKB canonical protein sequence

## What's New

The private `/utils/variantAnnotation` endpoint now validates protein change queries against the OncoKB canonical protein sequence. When Genome Nexus does not resolve the query to a valid alternative OncoKB variant, it extracts the reference residue(s) and position(s) from the alteration, calls the OncoKB transcript service's find-canonical-sequence (`sequenceType=PROTEIN`) for the queried gene, and checks them against the canonical sequence. The response reports the outcome through a `messageType` enum (and, only for a genuine reference mismatch, a detailed `message`).

`messageType` values:
- **`INVALID_PROTEIN_CHANGE`** — the query is not a valid protein change against the canonical sequence. `message` carries the detail. Covers reference amino acid mismatch (e.g. `A600E` where position 600 is `V`), range boundaries (both boundaries of e.g. `A237_G238del` are checked), a position beyond the sequence length, malformed forms, and spelled-out deletions (e.g. `A237_G238delAG`, validated over the whole range for length and content).
- **`NO_PROTEIN_SEQUENCE`** — the transcript service returned no canonical protein sequence for the gene, so the query is treated as invalid.
- **`TRANSCRIPT_SERVICE_DISABLED`** — the transcript service is disabled, so the required validation step could not run.
- **`TRANSCRIPT_SERVICE_UNAVAILABLE`** — the transcript service failed (likely intermittent); the query should be retried.

The message text for the non-mismatch types is left to the frontend — only the type is returned. The annotation itself is still returned unchanged. No `messageType` is set when everything agrees, when the query resolves to a valid alternative variant, or when the alteration is not a reference-bearing protein change.

## Impact

Affects only the somatic `/utils/variantAnnotation` endpoint, and only for protein change queries (queries with a resolvable protein position and reference residue). HGVSg and genomic change queries are unaffected.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `SomaticVariantAnnotation.message` | Added (nullable; detail text, set only for an `INVALID_PROTEIN_CHANGE`) | `/utils/variantAnnotation` |
| `SomaticVariantAnnotation.messageType` | Added (nullable enum: `INVALID_PROTEIN_CHANGE`, `NO_PROTEIN_SEQUENCE`, `TRANSCRIPT_SERVICE_DISABLED`, `TRANSCRIPT_SERVICE_UNAVAILABLE`) | `/utils/variantAnnotation` |

## Migration / Action Required

None

## Related Links

- PR:
- Issue:
- Docs:
