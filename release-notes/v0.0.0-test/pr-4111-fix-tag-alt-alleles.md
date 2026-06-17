# Prioritize curated alterations with no variant allele over tags for tumor type summaries

## What's New

The tumor type summary retrieval logic has been reordered to prioritize curated alterations with no variant allele before falling back to tag-based evidence. This ensures that when alternative alleles are available, more specific curated evidence is returned instead of generic tag-based summaries.

## Impact

OncoKB public website

## API Changes

None

## Migration / Action Required

None

## Related Links

- PR: #4111
- Issue:
- Docs:
