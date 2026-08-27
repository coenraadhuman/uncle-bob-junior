# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-27 · model: sonnet · runs per cell: 2 · medians reported.

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
| code LOC | 27 | 18 |
| longest function (lines) | 11.500 | 9 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 3 | 3 |
| magic numbers | 8 | 2 |
| short names | 0.500 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.121 | 0.145 |
| duration (ms) | 11090.500 | 15204.500 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 51.500 | 43.500 |
| longest function (lines) | 45.500 | 15 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 4.500 | 4.500 |
| magic numbers | 0.500 | 1.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.050 | 0.072 |
| duration (ms) | 9744 | 13561.500 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 64 | 48 |
| longest function (lines) | 18 | 11.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 6 | 5 |
| magic numbers | 1 | 0 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.058 | 0.106 |
| duration (ms) | 14712.500 | 30527.500 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 122.500 | 110.500 |
| longest function (lines) | 14 | 9.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 5 | 3.500 |
| magic numbers | 7 | 0.500 |
| short names | 0 | 0 |
| duplicate blocks | 19 | 0.500 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.106 | 0.151 |
| duration (ms) | 31433 | 56419.500 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 85.500 | 116 |
| longest function (lines) | 18 | 15 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3 |
| magic numbers | 4 | 1.500 |
| short names | 0 | 0 |
| duplicate blocks | 1 | 1 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.068 | 0.180 |
| duration (ms) | 17827 | 65477.500 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 64 | 48 |
| longest function (lines) | 18 | 11.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4.500 | 3.500 |
| magic numbers | 4 | 1.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.068 | 0.145 |
| duration (ms) | 14712.500 | 30527.500 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
