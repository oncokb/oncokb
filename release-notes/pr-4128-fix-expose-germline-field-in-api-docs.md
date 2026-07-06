# Expose the germline query field in API documentation

## What's New

Removed `@ApiModelProperty(hidden = true)` from the `Query.isGermline()` getter so the `germline` boolean is no longer hidden from the generated Swagger/OpenAPI documentation.

## Impact

API documentation and generated clients now show the `germline` field on the `Query` object. Runtime API behavior is unchanged: the field was already serialized in responses (as `germline`), it was only hidden from the docs.

## API Changes

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| `Query.germline` | Added (to docs) | All endpoints returning a `Query` object (e.g. `/annotate/*` responses) |

## Migration / Action Required

None

## Related Links

- PR: #4128
- Issue:
- Docs:
