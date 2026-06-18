# Fix Genome Nexus-backed germline variant resolution for annotation queries.

## What's New

Updated the germline annotation flow so Genome Nexus-backed HGVSg and genomic location requests preserve the resolved `hugoSymbol` and `alteration` on the query object even when the queried variant does not exist in the curated germline dataset.
Somatic and germline HGVSg/HGVSc inputs now trim surrounding whitespace before validation and lookup.
Germline HGVSc and somatic protein change annotation now correctly returns `geneExist=true` for `CDKN2A (p14)` queries and supported aliases.

## Impact

API clients and users of the germline annotation endpoints may see corrected results for HGVSg and genomic location queries that require Genome Nexus resolution, including cases where the resolved gene and cDNA alteration should still be returned even when no curated germline variant match exists.
Clients using somatic and germline HGVS inputs may also see improved handling of leading or trailing whitespace.
Germline HGVSc and somatic protein change queries for `CDKN2A (p14)` and supported aliases may now return the correct `geneExist=true` value instead of incorrectly reporting `geneExist=false`.
There is no deployment or self-hosting impact in this change.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `hgvsg` | Edit | Somatic and germline annotation endpoints that accept HGVSg now trim surrounding whitespace before validation and lookup. |
| `hgvsc` | Edit | Somatic and germline annotation endpoints that accept HGVSc now trim surrounding whitespace before validation and lookup. |
| Resolved query `hugoSymbol` and `alteration` | Edit | Germline annotation endpoints for HGVSg and genomic location inputs now preserve the Genome Nexus-resolved gene and cDNA alteration on the query object, even when no curated germline variant match exists. |
| `geneExist` for `CDKN2A (p14)` | Edit | Germline HGVSc and somatic protein change annotation now return the correct `geneExist` value for `CDKN2A (p14)` queries and supported aliases. |

## Migration / Action Required

None.

## Related Links

- PR: #4108
- Issue:
- Docs:
