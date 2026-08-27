# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-27 · model: haiku · runs per cell: 2 · medians reported.

Method: same task, same model, headless `claude -p --safe-mode` (no CLAUDE.md, hooks, or plugins).
Baseline arm gets the bare prompt; the uncle-bob-junior arm gets the SKILL.md
ruleset appended as system prompt. Production code (fenced blocks minus test
and non-code blocks) scored by `clean-code-metrics.js`, gated by `correctness.js`;
test code counts only toward "ships tests". Lower is better for every row
except "ships tests" and "correct".

## email

> Write a Java method that validates email addresses.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 23 | 41 |
| longest function (lines) | 10 | 15 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 2.500 | 3 |
| magic numbers | 1 | 4.500 |
| short names | 0 | 0 |
| duplicate blocks | 0.500 | 1.500 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.032 | 0.037 |
| duration (ms) | 6180.500 | 8626.500 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 39.500 | 50 |
| longest function (lines) | 34.500 | 22 |
| functions > 20 lines | 1 | 0.500 |
| max nesting depth | 7 | 5 |
| magic numbers | 1 | 0.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.018 | 0.018 |
| duration (ms) | 9527.500 | 7079 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 56 | 58.500 |
| longest function (lines) | 16 | 14.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 5 | 5 |
| magic numbers | 3 | 2.500 |
| short names | 0 | 0 |
| duplicate blocks | 3 | 3 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.018 | 0.032 |
| duration (ms) | 9555.500 | 19889 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 84 | 95 |
| longest function (lines) | 16 | 18 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3.500 |
| magic numbers | 5 | 5.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.018 | 0.035 |
| duration (ms) | 10292 | 25071 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 82 | 105 |
| longest function (lines) | 26.500 | 18 |
| functions > 20 lines | 1 | 0.500 |
| max nesting depth | 3.500 | 3.500 |
| magic numbers | 11.500 | 5 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.019 | 0.026 |
| duration (ms) | 9920 | 14346.500 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 56 | 58.500 |
| longest function (lines) | 16 | 18 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3.500 |
| magic numbers | 3 | 4.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.018 | 0.032 |
| duration (ms) | 9555.500 | 14346.500 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
