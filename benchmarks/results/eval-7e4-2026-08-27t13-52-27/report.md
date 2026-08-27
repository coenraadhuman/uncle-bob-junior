# Benchmark run eval-7E4-2026-08-27T13:52:27

Judges: habit-hooks (independent smell detector — enforced smells fail,
suggested smells are advisory), ships_tests and correct (gates). Higher
score = cleaner. Generated code and full habit-hooks reports sit next to
this file in `src/` and `habit-hooks/`.

| task | model | arm | score | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:-----------:|--------------|:-----------:|:-------:|
| order | claude-cli:haiku | baseline (no ruleset) | 0.67 | pass | habit-hooks passed: clean (scan artifacts excluded) | NO | yes |
| order | claude-cli:haiku | uncle-bob-junior | 1.00 | pass | habit-hooks passed: clean (scan artifacts excluded) | yes | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.667 (n=1)
- **claude-cli:haiku / uncle-bob-junior**: 1.000 (n=1)
