# Benchmark run eval-TRm-2026-08-27T12:29:22

Judges: habit-hooks (independent smell report, penalty), ships_tests and
correct (gates). Higher score = cleaner. Generated code and full
habit-hooks reports sit next to this file in `src/` and `habit-hooks/`.

| task | arm | score | habit-hooks | ships tests | correct |
|------|-----|------:|-------------|:-----------:|:-------:|
| email | baseline (no ruleset) | 0.61 | habit-hooks: 1 smell(s) — incomplete-run(1) | NO | yes |
| email | uncle-bob-junior | 1.00 | habit-hooks: clean | yes | yes |
| retry | baseline (no ruleset) | 0.61 | habit-hooks: 1 smell(s) — incomplete-run(1) | NO | yes |
| retry | uncle-bob-junior | 0.94 | habit-hooks: 1 smell(s) — incomplete-run(1) | yes | yes |

## Mean score per arm

- **baseline (no ruleset)**: 0.611 (n=2)
- **uncle-bob-junior**: 0.972 (n=2)
