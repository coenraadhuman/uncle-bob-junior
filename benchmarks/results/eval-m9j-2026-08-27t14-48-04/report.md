# Benchmark run eval-M9J-2026-08-27T14:48:04

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Generated code and full habit-hooks reports sit next to this file in
`src/` and `habit-hooks/`.

| task | model | arm | score | valid code | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:----------:|:-----------:|--------------|:-----------:|:-------:|
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.875 (n=1)
- **claude-cli:haiku / uncle-bob-junior**: 1.000 (n=1)
