# Benchmark run eval-MpG-2026-08-30T19:54:33

Judges: one habit-hooks metric per code smell (0 occurrences = pass;
suggested smells carry half the weight of enforced ones), plus the
valid_code, ships_tests, and correct gates. Higher score = cleaner.
Each smell column holds the occurrence count (enforced smells first,
then suggested). The first table keeps only the smells with at least
one hit across the run; the second carries the full catch list. File
and line locations live in the `habit-hooks/` reports next to this
file; the generated code sits in `src/`.

## Smells with hits

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | oversized-file | unused-import | swallowed-exception | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: | :---: |
| email | claude-cli:haiku | baseline (no ruleset) | 0.75 | NO | PASS | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.85 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 1 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 0 | 0 | 0 | 1 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 0 | 0 | 0 | 0 | 1 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 0 | 0 | 0 | 1 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 0 | 0 | 0 | 0 | 2 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 0 | 1 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.73 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | NO | NO |
| order | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| statement | claude-cli:haiku | baseline (no ruleset) | 0.83 | YES | FAIL | 2 | 1 | 0 | 0 | 0 | 0 | NO | YES |
| statement | claude-cli:haiku | uncle-bob-junior | 0.95 | YES | FAIL | 1 | 1 | 0 | 1 | 0 | 0 | YES | YES |
| statement | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | NO | YES |
| statement | claude-cli:haiku | uncle-bob-junior | 0.95 | YES | FAIL | 0 | 3 | 0 | 0 | 0 | 0 | YES | YES |
| statement | claude-cli:haiku | baseline (no ruleset) | 0.70 | YES | FAIL | 2 | 1 | 0 | 0 | 0 | 0 | NO | NO |
| statement | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | 0 | 0 | 0 | YES | YES |
| booking | claude-cli:haiku | baseline (no ruleset) | 0.78 | YES | FAIL | 3 | 2 | 0 | 1 | 0 | 0 | NO | YES |
| booking | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | YES | YES |
| booking | claude-cli:haiku | baseline (no ruleset) | 0.73 | YES | FAIL | 6 | 6 | 0 | 1 | 0 | 0 | NO | YES |
| booking | claude-cli:haiku | uncle-bob-junior | 0.94 | YES | FAIL | 0 | 2 | 0 | 1 | 1 | 0 | YES | YES |
| booking | claude-cli:haiku | baseline (no ruleset) | 0.75 | YES | FAIL | 4 | 1 | 1 | 1 | 1 | 0 | NO | YES |
| booking | claude-cli:haiku | uncle-bob-junior | 0.89 | YES | FAIL | 2 | 5 | 0 | 1 | 0 | 0 | YES | YES |
| config | claude-cli:haiku | baseline (no ruleset) | 0.77 | YES | FAIL | 4 | 0 | 2 | 1 | 0 | 0 | NO | YES |
| config | claude-cli:haiku | uncle-bob-junior | 0.89 | YES | FAIL | 2 | 4 | 1 | 0 | 0 | 0 | YES | YES |
| config | claude-cli:haiku | baseline (no ruleset) | 0.78 | YES | FAIL | 3 | 0 | 2 | 1 | 0 | 0 | NO | YES |
| config | claude-cli:haiku | uncle-bob-junior | 0.89 | YES | FAIL | 4 | 2 | 1 | 0 | 0 | 0 | YES | YES |
| config | claude-cli:haiku | baseline (no ruleset) | 0.69 | YES | FAIL | 3 | 0 | 1 | 0 | 0 | 0 | NO | NO |
| config | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | YES | YES |

## Full smell breakdown

| task | model | arm | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | deep-nesting | oversized-file | unused-variable | unused-import | loose-equality | var-declaration | non-const-binding | duplicate-import | redundant-type-annotation | unused-class-member | unused-file | unused-export | unused-dependency | test-only-dead-code | parse-error | warning-comment | explicit-any | non-null-assertion | non-essential-comment | duplicated-code | swallowed-exception | ships tests | correct |
| --- | --- | --- | ---: | :---: | :---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :---: | :---: |
| email | claude-cli:haiku | baseline (no ruleset) | 0.75 | NO | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| email | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| email | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| csv | claude-cli:haiku | baseline (no ruleset) | 0.85 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 1 | NO | YES |
| csv | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| retry | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| retry | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.88 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| ratelimit | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| ratelimit | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 0 | 0 | 0 | 0 | 0 | 0 | 2 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.86 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| order | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| order | claude-cli:haiku | baseline (no ruleset) | 0.73 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | NO |
| order | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| statement | claude-cli:haiku | baseline (no ruleset) | 0.83 | YES | FAIL | 2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| statement | claude-cli:haiku | uncle-bob-junior | 0.95 | YES | FAIL | 1 | 1 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| statement | claude-cli:haiku | baseline (no ruleset) | 0.84 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| statement | claude-cli:haiku | uncle-bob-junior | 0.95 | YES | FAIL | 0 | 3 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| statement | claude-cli:haiku | baseline (no ruleset) | 0.70 | YES | FAIL | 2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | NO |
| statement | claude-cli:haiku | uncle-bob-junior | 0.98 | YES | FAIL | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| booking | claude-cli:haiku | baseline (no ruleset) | 0.78 | YES | FAIL | 3 | 2 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| booking | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| booking | claude-cli:haiku | baseline (no ruleset) | 0.73 | YES | FAIL | 6 | 6 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| booking | claude-cli:haiku | uncle-bob-junior | 0.94 | YES | FAIL | 0 | 2 | 0 | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| booking | claude-cli:haiku | baseline (no ruleset) | 0.75 | YES | FAIL | 4 | 1 | 1 | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| booking | claude-cli:haiku | uncle-bob-junior | 0.89 | YES | FAIL | 2 | 5 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| config | claude-cli:haiku | baseline (no ruleset) | 0.77 | YES | FAIL | 4 | 0 | 2 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| config | claude-cli:haiku | uncle-bob-junior | 0.89 | YES | FAIL | 2 | 4 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| config | claude-cli:haiku | baseline (no ruleset) | 0.78 | YES | FAIL | 3 | 0 | 2 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | YES |
| config | claude-cli:haiku | uncle-bob-junior | 0.89 | YES | FAIL | 4 | 2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |
| config | claude-cli:haiku | baseline (no ruleset) | 0.69 | YES | FAIL | 3 | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | NO | NO |
| config | claude-cli:haiku | uncle-bob-junior | 0.97 | YES | FAIL | 1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | YES | YES |

## Mean score per model and arm

- **claude-cli:haiku / baseline (no ruleset)**: 0.815 (n=24)
- **claude-cli:haiku / uncle-bob-junior**: 0.971 (n=24)

```mermaid
xychart-beta
    title "Mean score per model and arm"
    x-axis ["claude-cli:haiku / baseline (no ruleset)", "claude-cli:haiku / uncle-bob-junior"]
    y-axis "mean score" 0 --> 1
    bar [0.815, 0.971]
```
