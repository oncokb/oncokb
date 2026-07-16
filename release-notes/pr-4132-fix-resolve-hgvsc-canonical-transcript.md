# Resolve germline HGVSc queries against the OncoKB canonical transcript before annotating

## What's New

The germline HGVSc endpoint now resolves the hugo symbol into our canonical Ensembl transcript before annotating with Genome Nexus. A cDNA change can exist on multiple transcripts of the same gene, and previously sending only the hugo symbol let Genome Nexus resolve the change on an arbitrary transcript. We now swap the hugo symbol out for the OncoKB canonical transcript id so Genome Nexus resolves the alteration on the transcript we actually annotate on.

When the gene is unknown or has no OncoKB canonical transcript for the reference genome, the endpoint returns an empty, un-annotatable result directly instead of calling Genome Nexus, since there is no transcript to resolve against.

## Impact

Affects only the germline HGVSc endpoint (`/annotate/germline/mutations/byHGVSc`). The somatic annotation endpoints are unchanged.

For a cDNA change that exists on multiple transcripts of a gene, the resolved alteration returned in the query object can change: it now reflects the OncoKB canonical transcript rather than whichever transcript Genome Nexus happened to pick. The previous behavior was incorrect.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `Query` resolved alteration | Edit (now resolved on the OncoKB canonical transcript when a cDNA change exists on multiple transcripts) | `/annotate/germline/mutations/byHGVSc` |

## Migration / Action Required

None

## Related Links

- PR: #4132
- Issue:
- Docs:
