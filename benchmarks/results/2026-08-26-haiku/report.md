# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-26 · model: haiku · runs per cell: 2 · medians reported.

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
| code LOC | 27.500 | 21.500 |
| longest function (lines) | 11 | 8.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 3 | 3 |
| magic numbers | 1 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 1 | 0.500 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.014 | 0.017 |
| duration (ms) | 6628.500 | 7162 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 42.500 | 45.500 |
| longest function (lines) | 39 | 33 |
| functions > 20 lines | 1 | 1 |
| max nesting depth | 6.500 | 6 |
| magic numbers | 0.500 | 0 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.016 | 0.018 |
| duration (ms) | 8465.500 | 6639 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 55 | 70 |
| longest function (lines) | 14 | 17.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 5 | 5 |
| magic numbers | 2.500 | 4.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 4.500 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.015 | 0.029 |
| duration (ms) | 6768.500 | 18771.500 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 112.500 | 101.500 |
| longest function (lines) | 15 | 11.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3.500 |
| magic numbers | 9 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 3 | 5 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.020 | 0.022 |
| duration (ms) | 12176.500 | 11243.500 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 101 | 102 |
| longest function (lines) | 24.500 | 25 |
| functions > 20 lines | 0.500 | 1 |
| max nesting depth | 4 | 3 |
| magic numbers | 9 | 5.500 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.019 | 0.025 |
| duration (ms) | 10105 | 13386.500 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 55 | 70 |
| longest function (lines) | 15 | 17.500 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 4 | 3.500 |
| magic numbers | 2.500 | 1 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0.500 |
| ships tests (share) | 0 | 0 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.016 | 0.022 |
| duration (ms) | 8465.500 | 11243.500 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
