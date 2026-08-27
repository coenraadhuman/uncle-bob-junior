# Benchmark run eval-hLN-2026-08-27T13:09:39

Judges: habit-hooks (independent smell report, penalty), ships_tests and
correct (gates). Higher score = cleaner. Generated code and full
habit-hooks reports sit next to this file in `src/` and `habit-hooks/`.

| task | arm | score | habit-hooks | ships tests | correct |
|------|-----|------:|-------------|:-----------:|:-------:|
| email | baseline (no ruleset) | 0.67 | habit-hooks: clean | NO | yes |
| email | uncle-bob-junior | 1.00 | habit-hooks: clean | yes | yes |

## Mean score per arm

- **baseline (no ruleset)**: 0.667 (n=1)
- **uncle-bob-junior**: 1.000 (n=1)
