---
description: Show the measured with/without benchmark scoreboard
---

Show the uncle-bob-junior gain scoreboard. One shot, change nothing: do not switch mode, write flag files, or persist anything. Each run lives in its own benchmarks/results/<date>-<model>/ directory; read the newest one and render its report.md summary table: the same tasks run with and without the ruleset, scored on lint violations, cyclomatic complexity, function length, nesting depth, magic numbers, duplication, and correctness. If no such results file exists, say so and point at benchmarks/README.md for how to produce one. NEVER invent a figure or print a per-repo savings number: for real per-repo figures point to /uncle-bob-junior-debt (counted ledger) and /uncle-bob-junior-audit (counted smells). Report only.
