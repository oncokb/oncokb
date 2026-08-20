# Update cancer hotspots to v3

Cancer hotspot annotation now uses the v3 hotspot set, adding 164 newly designated single-residue hotspots.

## What's New

The bundled hotspot dataset has been replaced. We previously shipped `cancer-hotspots-gn.json`; we now ship
`hotspots_v2_and_3d.txt`, the tab-delimited export from
[genome-nexus-importer](https://github.com/genome-nexus/genome-nexus-importer/blob/master/data/grch38_ensembl95/export/hotspots_v2_and_3d.txt).
`HotspotUtils` reads the new tab-delimited format instead of parsing JSON.

The new dataset adds **164 v3 single-residue hotspots** and removes none. All previously recognized v2 hotspots —
single residue, in-frame indel, splice site, and 3d — are still recognized. Hotspot matching logic itself is unchanged.

## Impact

Affects anyone annotating mutations that fall on one of the 164 newly added residues.

For variants at those residues that are **not curated** by OncoKB (`variantExist: false`), the response now returns
`hotspot: true` and an oncogenic effect of `Likely Oncogenic`, where previously the variant would have received
whatever call it inherited from alternate alleles or no call at all. The variant summary changes accordingly to
"... has been identified as a statistically significant hotspot and is likely to be oncogenic."

For variants at those residues that **are curated**, the curated oncogenic effect continues to take precedence and is
unchanged. This includes curated non-oncogenic calls such as `Resistance`, `Likely Neutral`, and `Inconclusive`.

One related summary change: when an uncurated alternate allele sits on a residue that is now a hotspot, the hotspot
summary takes precedence over the alternate-allele-derived summary. For example, `RET C634F` previously read
"However, RET C634R is known to be oncogenic and RET C634S/W/Y are likely oncogenic; therefore RET C634F is
considered likely oncogenic." and now reads "However, it has been identified as a statistically significant hotspot
and is likely to be oncogenic." The assigned oncogenic effect is `Likely Oncogenic` in both cases.

## API Changes

No request parameters, response fields, or endpoints were added, renamed, or removed. Response *values* change for
the affected variants.

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `hotspot` | Edit (now `true` for the 164 newly added residues) | `/annotate/mutations/*` |
| `oncogenic` | Edit (now `Likely Oncogenic` for uncurated variants at those residues) | `/annotate/mutations/*` |
| `variantSummary` | Edit (hotspot summary text for uncurated variants at those residues) | `/annotate/mutations/*` |

## Migration / Action Required

None. The hotspot dataset is bundled in the application, so there are no configuration, environment variable,
database, or Docker changes, and no migration step for self-hosted deployments.

Consumers who cache or store OncoKB annotations may want to re-annotate to pick up the new hotspot calls.

## Related Links

- PR [#4136](https://github.com/oncokb/oncokb/pull/4136)
- Data source: [genome-nexus-importer hotspots_v2_and_3d.txt](https://github.com/genome-nexus/genome-nexus-importer/blob/master/data/grch38_ensembl95/export/hotspots_v2_and_3d.txt)
