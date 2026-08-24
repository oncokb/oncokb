# Title

Enable request ID and user-agent logging in the no-frontend image.

## What's New

Registered `RequestResponseLoggingFilter` in the public API web profile so the no-frontend image now populates MDC `requestId` and `userAgent` values in application logs.

## Impact

Operators using the no-frontend image get improved request traceability and client attribution in logs. No functional API behavior changes.

## API Changes

None.

## Migration / Action Required

None.

## Related Links

- PR: #4142
- Issue:
- Docs:
