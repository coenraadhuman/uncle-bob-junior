# Benchmark run eval-EHt-2026-08-27T22:53:55

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Generated code and full habit-hooks reports sit next to this file in
`src/` and `habit-hooks/`.

| task | model | arm | score | valid code | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:----------:|:-----------:|--------------|:-----------:|:-------:|
| email | claude-cli:haiku | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| email | claude-cli:sonnet | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| email | claude-cli:fable | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| email | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| email | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.83 | yes | FAIL | 1 oversized-function at SalesSumCalculator.java:5; 1 high-complexity at SalesSumCalculator.java:5; 1 unused-import at SalesSumCalculator.java:2 | NO | yes |
| csv | claude-cli:haiku | uncle-bob-junior | 0.86 | yes | FAIL | 1 oversized-function at SalesAnalyzer.java:14 | NO | yes |
| csv | claude-cli:sonnet | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at SalesSum.java:9 | NO | yes |
| csv | claude-cli:fable | baseline (no ruleset) | 0.84 | yes | FAIL | 1 oversized-function at SalesSum.java:9; 1 high-complexity at SalesSum.java:9 | NO | yes |
| csv | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| csv | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| retry | claude-cli:sonnet | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at RetryHelper.java:20 | NO | yes |
| retry | claude-cli:fable | baseline (no ruleset) | 0.97 | yes | FAIL | 1 oversized-function at Retry.java:46; 1 high-complexity at Retry.java:46 | yes | yes |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| retry | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| retry | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at RateLimitedHttpHandler.java:14 | NO | yes |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.97 | yes | FAIL | 2 unused-import at RateLimitFilter.java:1, RateLimitFilter.java:2 | yes | yes |
| ratelimit | claude-cli:sonnet | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at RateLimitedHandler.java:40 | NO | yes |
| ratelimit | claude-cli:sonnet | uncle-bob-junior | 0.98 | yes | FAIL | 1 too-many-parameters at RateLimitingHandler.java:21 | yes | yes |
| ratelimit | claude-cli:fable | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| ratelimit | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| order | claude-cli:haiku | baseline (no ruleset) | 0.84 | yes | FAIL | 1 oversized-function at Order.java:51; 1 too-many-parameters at Order.java:51 | NO | yes |
| order | claude-cli:haiku | uncle-bob-junior | 0.97 | yes | FAIL | 1 oversized-function at OrderProcessor.java:100; 1 too-many-parameters at OrderProcessor.java:134 | yes | yes |
| order | claude-cli:sonnet | baseline (no ruleset) | 0.83 | yes | FAIL | 1 oversized-function at OrderProcessor.java:64; 1 too-many-parameters at OrderProcessor.java:64; 1 unused-import at OrderProcessor.java:5 | NO | yes |
| order | claude-cli:fable | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at OrderProcessor.java:84 | NO | yes |
| order | claude-cli:fable | uncle-bob-junior | 0.97 | yes | FAIL | 2 unused-import at LineItem.java:2, LineItem.java:3 | yes | yes |
| order | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.856 (n=5)
- **claude-cli:sonnet / baseline (no ruleset)**: 0.856 (n=5)
- **claude-cli:fable / baseline (no ruleset)**: 0.884 (n=5)
- **claude-cli:haiku / uncle-bob-junior**: 0.959 (n=5)
- **claude-cli:sonnet / uncle-bob-junior**: 0.997 (n=5)
- **claude-cli:fable / uncle-bob-junior**: 0.994 (n=5)
