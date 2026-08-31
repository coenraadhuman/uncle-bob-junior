# Benchmark run 2026-08-27

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Each smell column holds the occurrence count (enforced smells first,
then suggested). The first table keeps only the smells with at least
one hit across the run; the second carries the full catch list. File
and line locations live in the `habit-hooks/` reports next to this
file; the generated code sits in `src/`.

## Smells with hits

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | :---: | :---: |
| gameoflife | claude-cli:fable | baseline (no ruleset) | 0.83 | YES | FAIL | 2 | 0 | 1 | NO | YES |
| gameoflife | claude-cli:fable | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | YES | YES |
| gameoflife | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | NO | YES |
| gameoflife | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | YES | YES |
| gameoflife | claude-cli:sonnet | baseline (no ruleset) | 0.84 | YES | FAIL | 2 | 0 | 0 | NO | YES |
| gameoflife | claude-cli:sonnet | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | YES | YES |

## Full smell breakdown

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | deep-nesting | oversized-file | unused-variable | unused-import | loose-equality | var-declaration | non-const-binding | duplicate-import | redundant-type-annotation | unused-class-member | unused-file | unused-export | unused-dependency | test-only-dead-code | parse-error | warning-comment | explicit-any | non-null-assertion | non-essential-comment | duplicated-code | swallowed-exception | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: | :---: |
| gameoflife | claude-cli:fable | baseline (no ruleset) | 0.83 | YES | FAIL | 2 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| gameoflife | claude-cli:fable | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| gameoflife | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| gameoflife | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| gameoflife | claude-cli:sonnet | baseline (no ruleset) | 0.84 | YES | FAIL | 2 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| gameoflife | claude-cli:sonnet | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |

## Mean score per model and arm

- **claude-cli:fable / baseline (no ruleset)**: 0.828 (n=1)
- **claude-cli:fable / uncle-bob-junior**: 0.984 (n=1)
- **claude-cli:haiku / baseline (no ruleset)**: 0.859 (n=1)
- **claude-cli:haiku / uncle-bob-junior**: 1.000 (n=1)
- **claude-cli:sonnet / baseline (no ruleset)**: 0.844 (n=1)
- **claude-cli:sonnet / uncle-bob-junior**: 0.984 (n=1)

```mermaid
xychart-beta
    title "Mean score per model and arm"
    x-axis ["claude-cli:fable / baseline (no ruleset)", "claude-cli:fable / uncle-bob-junior", "claude-cli:haiku / baseline (no ruleset)", "claude-cli:haiku / uncle-bob-junior", "claude-cli:sonnet / baseline (no ruleset)", "claude-cli:sonnet / uncle-bob-junior"]
    y-axis "mean score" 0 --> 1
    bar [0.828, 0.984, 0.859, 1.000, 0.844, 0.984]
```
