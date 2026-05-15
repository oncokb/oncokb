#!/usr/bin/env bash

set -euo pipefail

skip_label="${SKIP_RELEASE_NOTES_LABEL:-skip-release-notes}"
labels="${PR_LABELS:-}"
release_note_files="${RELEASE_NOTE_FILES:-}"

if printf '%s\n' "$labels" | grep -Fxq "$skip_label"; then
  echo "Skipping release note requirement because the PR has the '$skip_label' label."
  exit 0
fi

if [ -n "${release_note_files//[$'\n\r\t ']}" ]; then
  echo "Release note file detected:"
  printf '%s\n' "$release_note_files"
  exit 0
fi

echo "PRs must include a markdown file under release-notes/ unless the '$skip_label' label is applied."
echo "Expected: add a file like release-notes/pr-123-short-summary.md"
exit 1
