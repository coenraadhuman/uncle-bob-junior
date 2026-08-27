# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-27 · model: haiku · runs per cell: 10 · medians reported.

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
| code LOC | 30 | 18.500 |
| longest function (lines) | 17.500 | 8.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 3 | 3 |
| magic numbers | 3.500 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0.500 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.014 | 0.019 |
| duration (ms) | 6776 | 8408 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 40.500 | 52.500 |
| longest function (lines) | 36 | 22 |
| functions > 20 lines | 1 | 1 |
| max nesting depth | 7 | 5 |
| magic numbers | 0 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.017 | 0.019 |
| duration (ms) | 7883 | 7215 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 58 | 47.500 |
| longest function (lines) | 15 | 14 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 5 | 5 |
| magic numbers | 3 | 1.500 |
| short names | 0 | 0 |
| duplicate blocks | 6 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.016 | 0.032 |
| duration (ms) | 7615.500 | 20433 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 98.500 | 94 |
| longest function (lines) | 15.500 | 12.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3.500 |
| magic numbers | 5 | 2 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0.500 |
| mutable fields | 0.500 | 1 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.019 | 0.025 |
| duration (ms) | 11043 | 13934 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 97 | 92.500 |
| longest function (lines) | 25.500 | 16 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 4 | 3 |
| magic numbers | 8.500 | 4 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0.500 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.019 | 0.032 |
| duration (ms) | 9876 | 19668 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 58 | 52.500 |
| longest function (lines) | 17.500 | 14 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 4 | 3.500 |
| magic numbers | 3.500 | 1.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| mutable fields | 0 | 0 |
| setters | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.017 | 0.025 |
| duration (ms) | 7883 | 13934 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
