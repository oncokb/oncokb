# Annotate cancer hotspots with their type, citations, and curated variants

Annotations of positional cancer hotspot variants now describe the hotspot by its type and cite the studies that identified it.

## What's New

### Public API

The variant summary for a positional hotspot query now names the hotspot type: single residue, in-frame indel, or splice site. For example, `BRAF V600` now returns:

> BRAF V600 is a statistically significant hotspot and single residue mutations at this position are considered likely oncogenic unless functional evidence suggests otherwise.

When a positional hotspot variant has no curated mutation effect, `mutationEffect.description` now explains how the hotspot was identified. `mutationEffect.citations.pmids` now lists the studies that identified the hotspot and the hotspot detection methods. `mutationEffect.knownEffect` remains `Unknown`.

The bundled hotspot data file now includes the PMIDs for each hotspot. The set of hotspots is unchanged, so `hotspot` flags in annotation responses are the same as before.

### Private API

A new `GET /api/private/utils/hotspotAnnotation` endpoint returns the annotation of a single cancer hotspot. The hotspot can be looked up by `residue` (for example `V600`, `X307`, or `27-42`) or by an `alteration` on it (for example `V600E`, `K459del`, or `X307_splice`). The response includes a new `cancerHotspot` object with the hotspot's type, protein range, tumor count, PMIDs, and the OncoKB-curated variants on the hotspot.

Biological variants returned by `/api/private/search/variants/biological` now include a `hotspot` object with `isHotspot`, `type`, `residue`, and `name`.

## Impact

Clients that call the public annotation endpoints with positional hotspot variants see the new `variantSummary` wording and, when the mutation effect is unknown, a populated `mutationEffect.description` and `mutationEffect.citations`. Clients that store or compare these text fields will see differences. No response fields were added, renamed, or removed from public endpoints.

The private endpoint changes support the OncoKB website and do not affect API users.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `variantSummary` (text for positional hotspot variants) | Edit | `/annotate/mutations/byProteinChange` (GET and POST) |
| `mutationEffect.description`, `mutationEffect.citations` (positional hotspot variants with an unknown mutation effect) | Edit | `/annotate/mutations/byProteinChange` (GET and POST) |
| `cancerHotspot` | Added | `/api/private/utils/hotspotAnnotation` (new) |
| `hotspot` | Added | `/api/private/search/variants/biological` |

## Migration / Action Required

None

## Related Links

- PR: https://github.com/oncokb/oncokb/pull/4141
- Issue: https://github.com/oncokb/oncokb-pipeline/issues/1158
- Docs: None
