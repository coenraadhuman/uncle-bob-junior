# Benchmark run eval-E2x-2026-08-27T13:39:31

Judges: habit-hooks (independent smell detector — enforced smells fail,
suggested smells are advisory), ships_tests and correct (gates). Higher
score = cleaner. Generated code and full habit-hooks reports sit next to
this file in `src/` and `habit-hooks/`.

| task | model | arm | score | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:-----------:|--------------|:-----------:|:-------:|
| email | claude-cli:haiku | baseline (no ruleset) | 0.67 | pass | habit-hooks passed: clean | NO | yes |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | pass | habit-hooks passed: clean (scan artifacts excluded) | yes | yes |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.61 | FAIL | habit-hooks FAILED: 1 smell(s) — oversized-function(1) at SalesSum.java:4 | NO | yes |
| csv | claude-cli:haiku | uncle-bob-junior | 0.61 | FAIL | habit-hooks FAILED: 1 smell(s) — oversized-function(1) at SalesAnalyzer.java:16 | NO | yes |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.67 | pass | habit-hooks passed: clean (scan artifacts excluded) | NO | yes |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | pass | habit-hooks passed: clean | yes | yes |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.67 | pass | habit-hooks passed: clean | NO | yes |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.83 | FAIL | habit-hooks FAILED: 3 smell(s) — unused-import(3) at ClientRateLimiter.java:1, ClientRateLimiter.java:2, RateLimitingFilter.java:1 | yes | yes |
| order | claude-cli:haiku | baseline (no ruleset) | 0.67 | pass | habit-hooks passed: clean (scan artifacts excluded) | NO | yes |
| order | claude-cli:haiku | uncle-bob-junior | 0.94 | FAIL | habit-hooks FAILED: 1 smell(s) — oversized-function(1) at LineItem.java:83 | yes | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.656 (n=5)
- **claude-cli:haiku / uncle-bob-junior**: 0.878 (n=5)
