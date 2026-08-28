# Benchmark run eval-Pvn-2026-08-28T08:24:17

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Generated code and full habit-hooks reports sit next to this file in
`src/` and `habit-hooks/`.

| task | model | arm | score | valid code | habit-hooks | smells found | ships tests | correct |
|------|-------|-----|------:|:----------:|:-----------:|--------------|:-----------:|:-------:|
| email | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | clean | NO | YES |
| email | claude-cli:sonnet | baseline (no ruleset) | 0.88 | YES | PASS | clean | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |
| email | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 oversized-function at SalesSum.java:5; 1 unused-import at SalesSum.java:2 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 0.86 | YES | FAIL | 1 oversized-function at SalesAnalyzer.java:7 | NO | YES |
| csv | claude-cli:sonnet | baseline (no ruleset) | 0.86 | YES | FAIL | 1 oversized-function at SalesSum.java:9 | NO | YES |
| csv | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | clean | NO | YES |
| retry | claude-cli:sonnet | baseline (no ruleset) | 0.84 | YES | FAIL | 1 oversized-function at RetryHelper.java:22; 1 high-complexity at RetryHelper.java:22 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |
| retry | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | clean | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 unused-variable at Example.java:5 | YES | YES |
| ratelimit | claude-cli:sonnet | baseline (no ruleset) | 0.88 | YES | PASS | clean | NO | YES |
| ratelimit | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 oversized-function at Order.java:35; 1 high-complexity at Order.java:17 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 oversized-function at Receipt.java:14; 1 too-many-parameters at OrderCalculation.java:10 | YES | YES |
| order | claude-cli:sonnet | baseline (no ruleset) | 0.80 | YES | FAIL | 3 oversized-function at OrderProcessor.java:24, OrderProcessor.java:55, OrderProcessor.java:81; 1 too-many-parameters at OrderProcessor.java:81; 1 high-complexity at OrderProcessor.java:55 | NO | YES |
| order | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | clean | YES | YES |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.863 (n=5)
- **claude-cli:sonnet / baseline (no ruleset)**: 0.850 (n=5)
- **claude-cli:haiku / uncle-bob-junior**: 0.963 (n=5)
- **claude-cli:sonnet / uncle-bob-junior**: 1.000 (n=5)
