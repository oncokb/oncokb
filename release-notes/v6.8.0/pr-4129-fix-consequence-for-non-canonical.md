# Stop propagating a consequence downstream for variants without an OncoKB canonical transcript

## What's New

When a variant does not map to any OncoKB canonical transcript, the Genome Nexus fallback no longer copies that transcript's consequence into the resolved query. It still surfaces the gene (`hugoSymbol` / `entrezGeneId`) for context, but leaves `consequence` empty, so consequence-level rules (such as the gene-level "Truncating Mutations" annotation) no longer fire on it.

## Impact

Affects annotation by genomic change and HGVSg (any path that reaches this fallback). Previously such variants could return an incorrect `mutationEffect` and therapeutic level; they now return the gene with no consequence and the message "This variant does not occur within a gene or transcript annotated in OncoKB." Variants that resolve to an OncoKB canonical transcript are unaffected.

This is a behavioral change: clients that relied on receiving a consequence or an annotation for variants that do not map to an OncoKB transcript will now see an empty `consequence` and an `Unknown` mutation effect for those variants. The previous behavior was incorrect.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `Query.consequence` | Edit (value now empty for variants not on an OncoKB canonical transcript) | `/annotate/mutations/byGenomicChange`, `/annotate/mutations/byHGVSg` |

## Migration / Action Required

None

## Related Links

- PR: #4129
- Issue:
- Docs:
