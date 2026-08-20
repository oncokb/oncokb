# Add request annotation-count and agent-aware Genome Nexus logging

Adds observability-focused logging for annotation request volume, outbound Genome Nexus payload size (as list item count), and request `User-Agent` in logging context.

## What's New

- Added INFO logs for annotation counts on POST annotation endpoints across somatic and germline controllers.
- Added INFO logs for outbound Genome Nexus annotation calls to report payload size as the number of items in each request list.
- Added `User-Agent` from request headers to MDC logging context (`userAgent`) so logs can attribute traffic sources.
- This supports operational investigation of high-volume clients, including identifying whether AI agents are overwhelming the server with requests.

## Impact

This change affects operators and self-hosted deployments by improving request-level observability. There is no API contract change and no change to endpoint behavior.

## API Changes

None

| Parameter/Field Path | Change (Added/Edit/Removed) | Endpoints |
| --- | --- | --- |
| None | None | None |

## Migration / Action Required

None.

For self-hosted operators, this is optional observability data: if needed, `userAgent` in logs can be used to identify noisy clients (including automated/AI agents). No configuration changes are required to keep current behavior; existing deployments can run without any action.

## Related Links

- PR: https://github.com/oncokb/oncokb/pull/4138
- Issue:
- Docs:
