# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-26 · model: sonnet · runs per cell: 2 · medians reported.

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
| code LOC | 22 | 18 |
| longest function (lines) | 10 | 9 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 3 | 3 |
| magic numbers | 4.500 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.047 | 0.071 |
| duration (ms) | 11056.500 | 14855.500 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 51.500 | 46 |
| longest function (lines) | 45.500 | 15 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 6 | 4.500 |
| magic numbers | 1 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.049 | 0.072 |
| duration (ms) | 9347 | 15742 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 57 | 62 |
| longest function (lines) | 15 | 13.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 5 | 4.500 |
| magic numbers | 3 | 0 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.054 | 0.104 |
| duration (ms) | 12116 | 32527 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 106 | 91.500 |
| longest function (lines) | 15 | 10.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 5 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0.500 | 2 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 0.500 |
| cost (USD) | 0.075 | 0.192 |
| duration (ms) | 23606 | 63150.500 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 105 | 101 |
| longest function (lines) | 19.500 | 16 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 4 | 3 |
| magic numbers | 2 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0.500 | 1 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.074 | 0.123 |
| duration (ms) | 20352 | 39594.500 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 57 | 62 |
| longest function (lines) | 15 | 13.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 3 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.054 | 0.104 |
| duration (ms) | 12116 | 32527 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
