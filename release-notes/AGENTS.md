# Release Notes

If the user wants to create, update, or review release notes, read `release-notes/README.md` first for the directory conventions and template, then follow the instructions below.

Assume a release note markdown file is required unless the developer explicitly says it is not needed. If the developer says a release note is not needed, the PR must be labeled `skip-release-notes`.

Keep in mind that some clients run this application as a Docker image on their own servers. Any change that could affect self-hosted deployments should be treated as release-note-worthy. This includes configuration changes, environment variable changes, infrastructure assumptions, dependency changes that affect runtime behavior, database or schema changes, startup or migration changes, and API changes.

Before drafting the file:

1. Inspect the git diff and changed files.
2. Look for API contract changes, removed or renamed fields, endpoint changes, response shape changes, request validation changes, schema or database changes, configuration changes, deployment changes, Docker-related changes, and anything that could break clients, downstream systems, or self-hosted deployments.
3. Form an initial view of whether the PR may contain breaking API or database changes.
4. Prefill the questionnaire below using what you can determine from the diff and surrounding context.
5. Mark anything uncertain, incomplete, or ambiguous so the developer can review it.
6. Ask the developer to confirm or correct your initial view before writing the release note.

Ask the developer this questionnaire before generating the markdown. Prefill each answer with your best draft based on the change, and ask the developer to confirm, modify, or fill in blanks where needed:

1. What is the release note title?
2. What is new in this change?
3. Who is affected and what is the impact?
4. Are there API changes?
5. For each API change, list:
   - Parameter or field path
   - Change type: `Added`, `Edit`, or `Removed`
   - Affected endpoints
6. Are there self-hosting impacts such as configuration changes, environment variable changes, database changes, migration changes, Docker/runtime changes, or operational changes?
7. Based on the diff, I think these may be breaking API, database, or self-hosting changes: `<list your findings>`. Is that correct?
8. If there are breaking changes or self-hosting changes, what migration or action is required?
9. Are there related PRs, issues, docs, dashboards, or rollout links that should be included?

If something cannot be determined from the diff, say that explicitly and leave a clear placeholder for the developer to answer.

If the developer confirms that no API changes exist, keep the `## API Changes` section and write `None`.

If the developer confirms that no migration or action is required, keep the `## Migration / Action Required` section and write `None`.

The markdown file must contain these exact sections:

- `# Title`
- `## What's New`
- `## Impact`
- `## API Changes`
- `## Migration / Action Required`
- `## Related Links`
