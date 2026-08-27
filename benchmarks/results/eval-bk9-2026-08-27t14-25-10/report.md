# Benchmark run eval-bK9-2026-08-27T14:25:10

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
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at SalesSummarizer.java:6 | NO | yes |
| csv | claude-cli:haiku | uncle-bob-junior | 0.86 | yes | FAIL | 1 oversized-function at SalesSum.java:6 | NO | yes |
| csv | claude-cli:sonnet | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at SalesSum.java:9 | NO | yes |
| csv | claude-cli:fable | baseline (no ruleset) | 0.84 | yes | FAIL | 1 oversized-function at SalesSum.java:9; 1 high-complexity at SalesSum.java:9 | NO | yes |
| csv | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| csv | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| retry | claude-cli:sonnet | baseline (no ruleset) | 0.83 | yes | FAIL | 1 oversized-function at RetryHelper.java:33; 2 too-many-parameters at RetryHelper.java:33, RetryHelper.java:71 | NO | yes |
| retry | claude-cli:fable | baseline (no ruleset) | 0.97 | yes | FAIL | 1 oversized-function at Retry.java:45; 1 high-complexity at Retry.java:45 | yes | yes |
| retry | claude-cli:fable | uncle-bob-junior | 0.97 | yes | FAIL | 1 oversized-function at FixedDelayRetry.java:55; 1 unused-import at RetryableOperation.java:1 | yes | yes |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| retry | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.84 | yes | FAIL | 2 unused-import at RateLimiter.java:1, RateLimitingFilter.java:1 | NO | yes |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.98 | yes | FAIL | 1 unused-import at RateLimitedHandler.java:4 | yes | yes |
| ratelimit | claude-cli:sonnet | baseline (no ruleset) | 0.88 | yes | pass | clean | NO | yes |
| ratelimit | claude-cli:fable | baseline (no ruleset) | 0.84 | yes | FAIL | 2 oversized-function at RateLimitingHandler.java:27, RateLimitingHandler.java:54 | NO | yes |
| ratelimit | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| ratelimit | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| order | claude-cli:haiku | baseline (no ruleset) | 0.86 | yes | FAIL | 1 oversized-function at Order.java:24 | NO | yes |
| order | claude-cli:haiku | uncle-bob-junior | 0.84 | yes | FAIL | 1 oversized-function at OrderProcessor.java:44; 1 too-many-parameters at OrderProcessor.java:44 | NO | yes |
| order | claude-cli:sonnet | baseline (no ruleset) | 0.83 | yes | FAIL | 1 oversized-function at OrderProcessor.java:69; 1 too-many-parameters at OrderProcessor.java:69; 1 high-complexity at OrderProcessor.java:50 | NO | yes |
| order | claude-cli:fable | baseline (no ruleset) | 0.81 | yes | FAIL | 2 oversized-function at OrderProcessor.java:36, OrderProcessor.java:58; 1 too-many-parameters at OrderProcessor.java:58; 1 high-complexity at OrderProcessor.java:36 | NO | yes |
| order | claude-cli:sonnet | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |
| order | claude-cli:fable | uncle-bob-junior | 1.00 | yes | pass | clean | yes | yes |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.863 (n=5)
- **claude-cli:sonnet / baseline (no ruleset)**: 0.853 (n=5)
- **claude-cli:fable / baseline (no ruleset)**: 0.869 (n=5)
- **claude-cli:haiku / uncle-bob-junior**: 0.938 (n=5)
- **claude-cli:sonnet / uncle-bob-junior**: 1.000 (n=5)
- **claude-cli:fable / uncle-bob-junior**: 0.994 (n=5)
