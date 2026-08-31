# Benchmark run eval-zg5-2026-08-28T09:22:44

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Each smell column holds the occurrence count (enforced smells first,
then suggested). The first table keeps only the smells with at least
one hit across the run; the second carries the full catch list. File
and line locations live in the `habit-hooks/` reports next to this
file; the generated code sits in `src/`.

## Smells with hits

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | unused-import | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | ---: | :---: | :---: |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:sonnet | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:sonnet | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | YES | YES |
| order | claude-cli:sonnet | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 0 | 0 | 0 | 1 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:sonnet | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:sonnet | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 0 | 1 | 0 | NO | YES |
| retry | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | YES | YES |

## Full smell breakdown

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | deep-nesting | oversized-file | unused-variable | unused-import | loose-equality | var-declaration | non-const-binding | duplicate-import | redundant-type-annotation | unused-class-member | unused-file | unused-export | unused-dependency | test-only-dead-code | parse-error | warning-comment | explicit-any | non-null-assertion | non-essential-comment | duplicated-code | swallowed-exception | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: | :---: |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:sonnet | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:sonnet | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:sonnet | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:sonnet | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:sonnet | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:sonnet | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.866 (n=5)
- **claude-cli:haiku / uncle-bob-junior**: 0.991 (n=5)
- **claude-cli:sonnet / baseline (no ruleset)**: 0.863 (n=5)
- **claude-cli:sonnet / uncle-bob-junior**: 1.000 (n=5)

```mermaid
xychart-beta
    title "Mean score per model and arm"
    x-axis ["claude-cli:haiku / baseline (no ruleset)", "claude-cli:haiku / uncle-bob-junior", "claude-cli:sonnet / baseline (no ruleset)", "claude-cli:sonnet / uncle-bob-junior"]
    y-axis "mean score" 0 --> 1
    bar [0.866, 0.991, 0.863, 1.000]
```
