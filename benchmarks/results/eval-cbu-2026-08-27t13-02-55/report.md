# Benchmark run eval-cBU-2026-08-27T13:02:55

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Generated code and full habit-hooks reports sit next to this file in
`src/` and `habit-hooks/`.

| task | model | arm | score | valid code | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:----------:|:-----------:|--------------|:-----------:|:-------:|
| email | claude-cli:haiku | baseline (no ruleset) | 0.67 | n/a | pass | habit-hooks: clean (scan artifacts excluded) | NO | yes |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | n/a | pass | habit-hooks: clean | yes | yes |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.61 | n/a | pass | habit-hooks: 1 smell(s) — oversized-function(1) at SalesSummarizer.java:6 | NO | yes |
| csv | claude-cli:haiku | uncle-bob-junior | 0.61 | n/a | pass | habit-hooks: 1 smell(s) — oversized-function(1) at SalesSummary.java:17 | NO | yes |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.67 | n/a | pass | habit-hooks: clean (scan artifacts excluded) | NO | yes |
| retry | claude-cli:haiku | uncle-bob-junior | 0.94 | n/a | pass | habit-hooks: 1 smell(s) — unused-variable(1) at RetryHelperUsageExample.java:6 | yes | yes |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.67 | n/a | pass | habit-hooks: clean | NO | yes |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.94 | n/a | pass | habit-hooks: 1 smell(s) — unused-import(1) at RateLimiter.java:1 | yes | yes |
| order | claude-cli:haiku | baseline (no ruleset) | 0.56 | n/a | pass | habit-hooks: 2 smell(s) — oversized-function(1) at OrderProcessor.java:27; high-complexity(1) at OrderProcessor.java:27 | NO | yes |
| order | claude-cli:haiku | uncle-bob-junior | 0.61 | n/a | pass | habit-hooks: 1 smell(s) — oversized-function(1) at LineItem.java:86 | NO | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.633 (n=5)
- **claude-cli:haiku / uncle-bob-junior**: 0.822 (n=5)
