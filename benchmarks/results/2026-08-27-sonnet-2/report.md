# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-27 · model: sonnet · runs per cell: 10 · medians reported.

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
| code LOC | 21 | 18 |
| longest function (lines) | 9 | 9 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 3 | 3 |
| magic numbers | 2 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.046 | 0.075 |
| duration (ms) | 8983.500 | 16379 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 51.500 | 46.500 |
| longest function (lines) | 46 | 12 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 5 | 4 |
| magic numbers | 1 | 2 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.049 | 0.096 |
| duration (ms) | 9620 | 25733 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 61 | 67.500 |
| longest function (lines) | 25.500 | 14 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 5 | 4 |
| magic numbers | 2 | 0 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.055 | 0.152 |
| duration (ms) | 12000.500 | 56041 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 97 | 120 |
| longest function (lines) | 14 | 11.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 5 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 1 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.070 | 0.208 |
| duration (ms) | 19650 | 80860 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 103 | 110.500 |
| longest function (lines) | 19 | 16 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3 |
| magic numbers | 4 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 1 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.073 | 0.175 |
| duration (ms) | 20926.500 | 65550 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 61 | 67.500 |
| longest function (lines) | 19 | 12 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4.500 | 4 |
| magic numbers | 2 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.055 | 0.152 |
| duration (ms) | 12000.500 | 56041 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
