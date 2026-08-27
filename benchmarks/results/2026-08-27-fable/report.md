# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-27 · model: fable · runs per cell: 2 · medians reported.

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
| code LOC | 36.500 | 52.500 |
| longest function (lines) | 16 | 13.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 3 | 3.500 |
| magic numbers | 7.500 | 5.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.357 | 0.459 |
| duration (ms) | 19778.500 | 28914 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 56.500 | 52 |
| longest function (lines) | 44 | 12 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 0 | 0 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.174 | 0.233 |
| duration (ms) | 18885 | 18967 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 88 | 77.500 |
| longest function (lines) | 18.500 | 13.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 5 | 4.500 |
| magic numbers | 3 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 1 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.217 | 0.364 |
| duration (ms) | 24944 | 44187.500 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 113 | 120 |
| longest function (lines) | 22.500 | 14 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 7 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 1 | 2.500 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.255 | 0.584 |
| duration (ms) | 29181.500 | 83139.500 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 88 | 83 |
| longest function (lines) | 20 | 16 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 3.500 | 3 |
| magic numbers | 9.500 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.238 | 0.406 |
| duration (ms) | 26612 | 46850.500 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 88 | 77.500 |
| longest function (lines) | 20 | 13.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 7 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.238 | 0.406 |
| duration (ms) | 24944 | 44187.500 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
