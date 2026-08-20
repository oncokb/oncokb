# Filter germline genomic indicators by inheritance mechanism and fetch them directly by gene and variant

## What's New

**Public API**

The germline HGVSc annotation endpoints accept a new optional `inheritanceMechanisms` filter. When provided, only the genomic indicators matching one of the requested inheritance mechanisms are returned; when omitted, the response is unchanged. The special value `CARRIER` is not a real inheritance mechanism, so it is matched against the genomic indicator name instead of the indicator's inheritance mechanism.

- `GET /annotate/germline/mutations/byHGVSc` — new `inheritanceMechanisms` query parameter, e.g. `inheritanceMechanisms=AUTOSOMAL_DOMINANT,CARRIER`.
- `POST /annotate/germline/mutations/byHGVSc` — new `inheritanceMechanisms` field on each query in the request body.

**Private API (internal use only)**

New endpoint `POST /utils/genomicIndicators` returns the germline genomic indicators for a list of gene/variant queries without running a full annotation. Each query takes `hugoSymbol`, `variant`, and an optional `inheritanceMechanisms` filter with the same semantics as above; each response item echoes the query alongside the matching genomic indicators. `hugoSymbol` and `variant` are required and a missing value returns `400`.

A `Pathogenic Variants` query on this endpoint also matches curated germline alterations that carry an exclusion clause, such as `Pathogenic Variants {excluding A, B, C}`, so those indicators are no longer missed.

## Impact

Additive for all consumers. Existing requests that do not send `inheritanceMechanisms` behave exactly as before, on both the public premium germline endpoints and everywhere else.

Clients of the premium germline HGVSc endpoints can now narrow genomic indicators server-side instead of filtering the full list themselves. The new private endpoint is for internal consumers only and is not part of the public API surface.

The public and private API versions are bumped from `v1.6.0` to `v1.7.0`, and `.version-level` is set to `minor` so the next application release is cut as a minor version.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `inheritanceMechanisms` (query parameter) | Added (optional) | Public: `GET /annotate/germline/mutations/byHGVSc` |
| `inheritanceMechanisms` (request body field) | Added (optional) | Public: `POST /annotate/germline/mutations/byHGVSc` |
| `POST /utils/genomicIndicators` | Added | Private: `/utils/genomicIndicators` |
| `hugoSymbol`, `variant`, `inheritanceMechanisms` (request body fields) | Added | Private: `POST /utils/genomicIndicators` |
| `query`, `genomicIndicators` (response fields) | Added | Private: `POST /utils/genomicIndicators` |

## Migration / Action Required

None. No configuration, environment variable, database, or Docker changes are required for self-hosted deployments.

## Related Links

- PR: #4133
- Issue:
- Docs:
