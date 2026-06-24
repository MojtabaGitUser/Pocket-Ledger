# Summary

-

## Linked Issue

-

## Testing

- [ ] Ran the relevant local tests or documented why they were not run.
- [ ] Updated or added tests for changed behavior.

## Accessibility Checklist

Use `N/A - <brief reason>` for items that do not apply, for example
`N/A - backend-only change` or `N/A - test-only refactor`. UI changes should
complete the relevant checks before merge. See
[`androidApp/docs/accessibility.md`](../androidApp/docs/accessibility.md).

- [ ] Icon-only buttons and meaningful interactive elements have semantic
  labels.
- [ ] Selected, checked, expanded/collapsed, loading, error, disabled,
  synced/offline, and similar states have clear state descriptions where
  applicable.
- [ ] Decorative icons and images are hidden from accessibility services.
- [ ] Changed screens/components were considered or tested at 200% font
  scaling.
- [ ] Important text is not clipped, overlapped, or hidden at large font sizes,
  and primary actions remain reachable.
- [ ] TalkBack/screen-reader navigation order is logical for changed UI.
- [ ] Forms, validation errors, empty states, loading states, and error states
  remain understandable.
- [ ] Color is not the only way important information is communicated, and text
  contrast/readability is preserved.
- [ ] Accessibility strings are user-facing, localized/resource-backed where
  expected, and do not expose internal IDs or raw enum names.
- [ ] Accessibility behavior changes include updated Compose UI, screenshot, or
  other relevant tests.
- [ ] Debug/app-health screens, build/test/benchmark state labels, and
  diagnostics UI remain accessible if touched.
- [ ] Release diagnostics do not expose sensitive data through visible text,
  logs, crash metadata, or accessibility labels.
- [ ] Debug and release diagnostics behavior remains consistent with
  [`androidApp/docs/logging-policy.md`](../androidApp/docs/logging-policy.md).

## Screenshots

- N/A

## Risk

-

## Checklist

- [ ] Documentation was updated when needed.
- [ ] No unrelated changes are included.
