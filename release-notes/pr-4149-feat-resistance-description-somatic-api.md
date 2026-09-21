# Add resistance description to somatic annotation responses

## What's New

Added a new somatic response field, `resistanceDescription`, to provide concise resistance context (`Known Resistance Mutation`, `Potential Resistance Implications`, or `Limited Resistance Evidence`) when applicable.

The resistance description is derived from resistance evidence and oncogenicity, and resistance-focused variant summary text is updated to use more specific language.

## Impact

API consumers of somatic annotation responses receive additional resistance context and updated resistance-summary phrasing for resistance-classified alterations.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `SomaticIndicatorQueryResp.resistanceDescription` | Added | Somatic annotation responses (including `SomaticVariantAnnotation` outputs) |
| Somatic variant summary text for resistance oncogenicity cases | Edit | Somatic annotation responses that include variant summaries |

## Migration / Action Required

None.

## Related Links

- PR: https://github.com/oncokb/oncokb/pull/4149
- Issue: https://github.com/oncokb/oncokb-pipeline/issues/1267
- Docs: None
