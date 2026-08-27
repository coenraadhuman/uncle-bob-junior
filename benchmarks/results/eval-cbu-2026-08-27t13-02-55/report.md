# Benchmark run eval-cBU-2026-08-27T13:02:55

Judges: habit-hooks (independent smell report, penalty), ships_tests and
correct (gates). Higher score = cleaner. Generated code and full
habit-hooks reports sit next to this file in `src/` and `habit-hooks/`.

| task | arm | score | habit-hooks | ships tests | correct |
|------|-----|------:|-------------|:-----------:|:-------:|
| email | baseline (no ruleset) | 0.67 | habit-hooks: clean (scan artifacts excluded) | NO | yes |
| email | uncle-bob-junior | 1.00 | habit-hooks: clean | yes | yes |
| csv | baseline (no ruleset) | 0.61 | habit-hooks: 1 smell(s) — oversized-function(1) at SalesSummarizer.java:6 | NO | yes |
| csv | uncle-bob-junior | 0.61 | habit-hooks: 1 smell(s) — oversized-function(1) at SalesSummary.java:17 | NO | yes |
| retry | baseline (no ruleset) | 0.67 | habit-hooks: clean (scan artifacts excluded) | NO | yes |
| retry | uncle-bob-junior | 0.94 | habit-hooks: 1 smell(s) — unused-variable(1) at RetryHelperUsageExample.java:6 | yes | yes |
| ratelimit | baseline (no ruleset) | 0.67 | habit-hooks: clean | NO | yes |
| ratelimit | uncle-bob-junior | 0.94 | habit-hooks: 1 smell(s) — unused-import(1) at RateLimiter.java:1 | yes | yes |
| order | baseline (no ruleset) | 0.56 | habit-hooks: 2 smell(s) — oversized-function(1) at OrderProcessor.java:27; high-complexity(1) at OrderProcessor.java:27 | NO | yes |
| order | uncle-bob-junior | 0.61 | habit-hooks: 1 smell(s) — oversized-function(1) at LineItem.java:86 | NO | yes |

## Mean score per arm

- **baseline (no ruleset)**: 0.633 (n=5)
- **uncle-bob-junior**: 0.822 (n=5)
