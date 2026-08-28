---
description: Review changes for clean-code violations: names, length, nesting, duplication, magic values, tests
---

Review the current code changes for clean-code violations only, not correctness. One line per finding: L<line>: <tag> <smell>. <fix>. Tags: name (intent-hiding name), long (function over ten statements or doing two jobs), nest (nesting deeper than 2, show the guard clause), dup (same logic twice, name the extraction), magic (bare literal with meaning, give the constant name), srp (function or class with two jobs, name the split), dead (commented-out or unreachable code, delete), untested (new or changed behavior with no test, name the missing cases). End with 'smells: <N> (<tags>).' If nothing to flag: 'Clean already. Ship.'
