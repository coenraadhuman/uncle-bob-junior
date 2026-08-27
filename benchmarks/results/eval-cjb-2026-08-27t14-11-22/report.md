# Benchmark run eval-CjB-2026-08-27T14:11:22

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Generated code and full habit-hooks reports sit next to this file in
`src/` and `habit-hooks/`.

| task | model | arm | score | valid code | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:----------:|:-----------:|--------------|:-----------:|:-------:|
| order | claude-cli:haiku | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at Order.java:53 | NO | yes |
| order | claude-cli:haiku | uncle-bob-junior | 0.92 | yes | FAIL | 1 oversized-function at Order.java:50; 1 too-many-parameters at Order.java:50; 3 unused-import at LineItem.java:2, LineItem.java:3, LineItem.java:4 | yes | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.859 (n=1)
- **claude-cli:haiku / uncle-bob-junior**: 0.922 (n=1)
