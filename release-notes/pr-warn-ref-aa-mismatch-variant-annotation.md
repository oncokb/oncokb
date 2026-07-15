# Warn when a queried protein change disagrees with the OncoKB canonical protein sequence

## What's New

The private `/utils/variantAnnotation` endpoint now validates protein change queries against the OncoKB canonical protein sequence. It extracts the reference residue(s) and position(s) from the alteration, calls the OncoKB transcript service's find-canonical-sequence (`sequenceType=PROTEIN`) for the queried gene, and checks them against the canonical sequence. When something disagrees, the response includes a `message` concisely describing each problem.

Checks performed:
- **Reference amino acid mismatch** — the queried reference residue differs from the canonical residue at that position (e.g. `A600E` on a transcript with `V` at position 600).
- **Range boundaries** — both boundary residues of a range are validated (e.g. `A237_G238del` checks the residue at 237 and at 238).
- **Position beyond sequence** — a position greater than the length of the canonical protein sequence is reported.
- **Spelled-out deletions** — when a deletion lists its deleted residues (e.g. `A237_G238delAG`) that sequence is validated over the whole range for both length and content.

The annotation itself is still returned unchanged — this is informational only. If everything agrees, the transcript service is unavailable, the entrez id is unknown, or the alteration is not a reference-bearing protein change, no `message` is added (fail open).

## Impact

Affects only the somatic `/utils/variantAnnotation` endpoint, and only for protein change queries (queries with a resolvable protein position and reference residue). HGVSg and genomic change queries are unaffected.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `SomaticVariantAnnotation.message` | Added (nullable; set when the queried protein change disagrees with the canonical protein sequence) | `/utils/variantAnnotation` |

## Migration / Action Required

None

## Related Links

- PR:
- Issue:
- Docs:
