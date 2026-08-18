# Validation errors

Somatic annotation responses carry an `errors` list: everything OncoKB found wrong with the query,
each entry a `type` to branch on and a `message` saying the same thing in words. The list is **empty
whenever there is nothing wrong, and is never null**, so it is present on every somatic annotation
response.

Today every entry comes from one check. Before annotating, OncoKB checks the alteration a query names
against the OncoKB canonical sequence for the queried gene and reference genome. Some queries describe
a variant that **cannot exist** on that sequence — a reference residue that is not there, a position
past the end of the protein. There is no variant to annotate for such a query, so OncoKB does not
annotate one, and reports why through `errors`.

The list is deliberately not tied to that one check: it is where further validation OncoKB adds later
— gene-level and otherwise — will be reported, using the same `type`/`message` shape rather than a
parallel field. See [Adding checks](#adding-checks) for what that means for a client.

What OncoKB knows about the **gene** still holds, so a query rejected this way is annotated as a
gene-level query: `geneExist`, `geneSummary` and the rest of the gene-level content come back exactly
as they would for a query naming that gene and no alteration. Only the variant-level fields are left
at their defaults.

The check is part of somatic annotation rather than of one endpoint, so these errors can appear on any
somatic annotation response: `/annotate/mutations/byProteinChange` (GET and POST),
`/annotate/mutations/byHGVSg`, `/annotate/mutations/byHGVSc`, `/annotate/mutations/byGenomicChange`,
`/annotate/samples` and `/search`.

What is checked is the protein change the query resolves to. On `byProteinChange` that is the alteration
as sent; on the HGVS and genomic change endpoints it is the protein change Genome Nexus resolved the
query to, which OncoKB takes only from its own canonical transcript. A query on those endpoints is
therefore flagged only when OncoKB's canonical sequence and the transcript the annotation came from have
drifted apart. Alterations that name no reference residue — copy number, structural variants, `Fusion`,
`Amplification`, `Oncogenic Mutations` — are never flagged. Germline annotation does not carry `errors`,
as it matches on cDNA rather than protein change.

The private `/utils/variantAnnotation` runs the same check but reports it as a `proteinChangeValidation`
object with a `status` and a human-readable `message` rather than through `errors`, and annotates the
query rather than rejecting it.

## Reading the response

`errors` is **empty in every annotation of a variant**. A client that does not care about the
distinction can keep ignoring the field: a rejected query still comes back as a well-formed response,
with the queried alteration echoed in `query`, the gene-level content populated, and the variant-level
fields (`variantExist`, `oncogenic`, `variantSummary`, `tumorTypeSummary`, `treatments`, the
implication lists and the highest levels) at their defaults.

```json
{
  "query": {
    "hugoSymbol": "BRAF",
    "alteration": "A600E",
    "referenceGenome": "GRCh37",
    "canonicalTranscript": "ENST00000646891"
  },
  "errors": [
    {
      "type": "REFERENCE_ALLELE_MISMATCH",
      "message": "BRAF A600E: The reference amino acid at position 600 is V instead of A on the OncoKB canonical transcript."
    }
  ],
  "geneExist": true,
  "geneSummary": "BRAF, an intracellular kinase, is frequently mutated in melanoma, ...",
  "variantExist": false,
  "oncogenic": "Unknown",
  "variantSummary": "",
  "treatments": []
}
```

A valid query has `"errors": []` and is annotated as it always was.

`query.canonicalTranscript` names the transcript the alteration was checked against, so the reason can
be shown with the transcript it applies to. It is populated for every query naming a gene OncoKB
curates, rejected or not.

`type` and `message` always agree, so use whichever suits: branch on `type`, display `message`, or
both. `message` is OncoKB's wording of the same reason, naming what was queried and what OncoKB has
instead — useful for a client with nowhere better to get the text, but it is prose and its exact
phrasing may change, so never parse it.

The alteration check reports **at most one** error per query: when a query has more than one thing
wrong with it, the most specific reason is the one reported. Do not rely on the list holding exactly
one entry, though — it is a list precisely so that later checks can report alongside this one.

## `type` values

| Value | Meaning | Example |
| --- | --- | --- |
| `REFERENCE_ALLELE_MISMATCH` | The reference allele the query names is not what the OncoKB canonical sequence has at that position. Usually a wrong reference residue, a position off by a few, or a query written against a different transcript than the OncoKB canonical one. | `BRAF A600E` — position 600 is `V` on the OncoKB canonical transcript. |
| `POSITION_OUT_OF_RANGE` | The position the query names is past the end of the OncoKB canonical sequence. Also typical of a query written against a longer, non-canonical isoform. | `BRAF V9999E` — the canonical protein is 766 residues. |
| `REVERSED_POSITION_RANGE` | The query names a range whose start position comes after its end position. | `BRAF V600_G596del`. |
| `MALFORMED_ALTERATION` | The query is not a well-formed alteration of its kind, independent of the canonical sequence — most often a single position carrying more than one reference residue. | `BRAF AL600L`, `BRAF VVV600_W604del`. |

All four mean the queried alteration cannot exist, so all four leave the variant unannotated.

## Adding checks

`errors` is one flat vocabulary for every kind of validation OncoKB reports, not one field or one enum
per check. A `type` added later need not concern the alteration at all, so **do not infer from the
presence of an error that the variant was left unannotated**. That is true of every value listed above,
but a later check may report something worth telling the user about alongside a complete annotation.
Read the response fields for what was annotated, and `errors` for what was wrong.

## What is *not* reported here

- **A query that could not be checked.** If the gene has no canonical protein sequence, or the
  transcript service is unavailable, the query is annotated as usual and `errors` stays empty. The
  check is skipped, not failed.
- **An alteration with no reference allele to check** — `Amplification`, `Fusion`, `Truncating
  Mutations`, an insertion's inserted residues, a frameshift that names no single reference residue.
  These are annotated as usual.
- **An unknown gene.** A gene OncoKB does not curate is reported through `geneExist`, as before.
- **The private `/utils/variantAnnotation` endpoint**, which reports its own `proteinChangeValidation`
  object rather than this field — it also covers normalized and unchecked queries, and carries the same
  `message` without this enum.
