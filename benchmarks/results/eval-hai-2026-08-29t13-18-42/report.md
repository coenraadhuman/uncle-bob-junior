# Benchmark run eval-Hai-2026-08-29T13:18:42

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Each smell column holds the occurrence count (enforced smells first,
then suggested). The first table keeps only the smells with at least
one hit across the run; the second carries the full catch list. File
and line locations live in the `habit-hooks/` reports next to this
file; the generated code sits in `src/`.

## Smells with hits

| task | model | arm | score | valid code | habit-hooks | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | :---: | :---: |
| email | claude-cli:haiku | baseline (no ruleset) | 0.75 | YES | PASS | NO | NO |
| email | claude-cli:haiku | baseline (no ruleset) | 0.75 | NO | PASS | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | YES | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | YES | YES |

## Full smell breakdown

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | deep-nesting | oversized-file | unused-variable | unused-import | loose-equality | var-declaration | non-const-binding | duplicate-import | redundant-type-annotation | unused-class-member | unused-file | unused-export | unused-dependency | test-only-dead-code | parse-error | warning-comment | explicit-any | non-null-assertion | non-essential-comment | duplicated-code | swallowed-exception | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: | :---: |
| email | claude-cli:haiku | baseline (no ruleset) | 0.75 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | NO |
| email | claude-cli:haiku | baseline (no ruleset) | 0.75 | NO | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.750 (n=2)
- **claude-cli:haiku / uncle-bob-junior**: 1.000 (n=2)
