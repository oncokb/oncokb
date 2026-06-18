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
  echo "Release note file(s) detected:"
  printf '%s\n' "$release_note_files"

  # Validate each filename matches pr-<number>-<feat|fix|chore>-<short-kebab-summary>.md
  while IFS= read -r file; do
    [ -z "$file" ] && continue
    filename=$(basename "$file")
    if ! echo "$filename" | grep -Eq '^pr-[0-9]+-(feat|fix|chore)-[a-z0-9-]+\.md$'; then
      echo "ERROR: '${filename}' does not match the required format."
      echo "Expected: pr-<number>-<feat|fix|chore>-<short-kebab-summary>.md"
      echo "Example:  pr-123-feat-add-dark-mode.md"
      exit 1
    fi
    echo "✓ ${filename}"
  done <<< "$release_note_files"

  exit 0
fi

echo "PRs must include a markdown file under release-notes/ unless the '$skip_label' label is applied."
echo "Expected: add a file like release-notes/pr-123-feat-add-dark-mode.md"
exit 1