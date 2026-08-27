# Clean-code benchmark: with vs without uncle-bob-junior

Date: 2026-08-26 · model: fable · runs per cell: 2 · medians reported.

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
| code LOC | 32 | 50.500 |
| longest function (lines) | 14 | 15 |
| functions > 20 lines | 0 | 0 |
| max nesting depth | 3 | 4 |
| magic numbers | 5.500 | 5 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.189 | 0.447 |
| duration (ms) | 19328.500 | 25913.500 |

## csv

> Write a Java program that reads sales.csv and prints the sum of the 'amount' column.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 73 | 58.500 |
| longest function (lines) | 48.500 | 12.500 |
| functions > 20 lines | 1.500 | 0 |
| max nesting depth | 5 | 4 |
| magic numbers | 0 | 0 |
| short names | 0.500 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 0.500 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.184 | 0.254 |
| duration (ms) | 16343 | 21773 |

## retry

> Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 71.500 | 63.500 |
| longest function (lines) | 21.500 | 16 |
| functions > 20 lines | 1 | 0 |
| max nesting depth | 5 | 4.500 |
| magic numbers | 2 | 0 |
| short names | 0 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0.500 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.205 | 0.399 |
| duration (ms) | 20231 | 48213.500 |

## ratelimit

> Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 111 | 130 |
| longest function (lines) | 20 | 15.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 5 | 4.500 |
| magic numbers | 7.500 | 1 |
| short names | 0.500 | 0 |
| duplicate blocks | 1 | 0.500 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.271 | 0.518 |
| duration (ms) | 34328.500 | 70715 |

## order

> Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 95.500 | 78.500 |
| longest function (lines) | 21 | 15.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 4 | 3 |
| magic numbers | 5.500 | 1 |
| short names | 1 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.240 | 0.328 |
| duration (ms) | 26402 | 34369 |

## Summary (median of task medians)

| metric | baseline | uncle-bob-junior |
|---|--:|--:|
| code LOC | 73 | 63.500 |
| longest function (lines) | 21 | 15.500 |
| functions > 20 lines | 0.500 | 0 |
| max nesting depth | 5 | 4 |
| magic numbers | 5.500 | 1 |
| short names | 0.500 | 0 |
| duplicate blocks | 0 | 0 |
| ships tests (share) | 0 | 1 |
| correct (share) | 1 | 1 |
| cost (USD) | 0.205 | 0.399 |
| duration (ms) | 20231 | 34369 |

Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).
Caveats and how to read these numbers: ../README.md.
