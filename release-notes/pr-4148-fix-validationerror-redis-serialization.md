# Fix Redis cache serialization failure for validation errors.

## What's New

`ValidationError` now implements `java.io.Serializable`, so response objects that include validation errors can be serialized by the Redis cache layer.

## Impact

Deployments with Redis caching enabled no longer fail cache writes with `java.io.NotSerializableException: org.mskcc.cbio.oncokb.apiModels.ValidationError` when annotation responses include validation errors.

## API Changes

None

## Migration / Action Required

No API migration is required. If your environment previously cached incompatible entries, restart the service and clear Redis keys as needed.

## Related Links

- PR: https://github.com/oncokb/oncokb/pull/4148
- Issue: https://github.com/oncokb/oncokb-pipeline/issues/1266
- Docs: None
