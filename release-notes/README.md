# Release Notes

Every pull request should include one markdown file in this directory unless the PR is labeled `skip-release-notes`.

Recommended file naming:

- `release-notes/pr-<PR_NUMBER>-<short-kebab-summary>.md`

If the PR number is not available yet, use a temporary descriptive name and rename it later if needed.

Use [TEMPLATE.md](./TEMPLATE.md) as the starting point. The final tone should be concise, factual, and similar in structure to a Stripe changelog entry.

## Authoring Instructions

Assume a release note markdown file is required unless the developer explicitly says it is not needed. If the developer says a release note is not needed, the PR must be labeled `skip-release-notes`.

Before drafting the file:

1. Inspect the git diff and changed files.
2. Look for API contract changes, removed or renamed fields, endpoint changes, response shape changes, request validation changes, schema or database changes, and anything that could break clients or downstream systems.
3. Form an initial view of whether the PR may contain breaking API or database changes.
4. Ask the developer to confirm or correct that view before writing the release note.

Ask the developer this questionnaire before generating the markdown:

1. What is the release note title?
2. What is new in this change?
3. Who is affected and what is the impact?
4. Are there API changes?
5. For each API change, list:
   - Parameter or field path
   - Change type: `Added`, `Edit`, or `Removed`
   - Affected endpoints
6. Based on the diff, I think these may be breaking API or database changes: `<list your findings>`. Is that correct?
7. If there are breaking changes, what migration or action is required?
8. Are there related PRs, issues, docs, dashboards, or rollout links that should be included?

If the developer confirms that no API changes exist, keep the `## API Changes` section and write `None`.

If the developer confirms that no migration or action is required, keep the `## Migration / Action Required` section and write `None`.

The markdown file must contain these exact sections:

- `# Title`
- `## What's New`
- `## Impact`
- `## API Changes`
- `## Migration / Action Required`
- `## Related Links`
