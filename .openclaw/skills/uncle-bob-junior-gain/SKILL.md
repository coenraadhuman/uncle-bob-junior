---
name: uncle-bob-junior-gain
description: "Show the measured with/without benchmark scoreboard: lint, complexity, function length, nesting, tests. One-shot display."
homepage: https://github.com/coenraadhuman/uncle-bob-junior
license: MIT
---

# Uncle Bob Junior Gain

Display the benchmark scoreboard when invoked. One-shot: do NOT change mode,
write flag files, or persist anything.

## Source

The figures come from this repo's own with/without benchmark: the same tasks
run by the same agent with and without the ruleset, scored by automated
metrics (ESLint violations, cyclomatic complexity, function length, nesting
depth, magic numbers, duplication, correctness). Each run lives in its own
`benchmarks/results/<date>-<model>/` directory; read the newest one's
`report.md` and render its summary table as the scoreboard (the generated
code itself sits next to it in `src/` and `sources.md`).

If no such file exists yet, say so and point at how to produce one:

```
  No benchmark results yet.

  Run the with/without comparison:
    cd benchmarks && node run-clean-code.js     (single-shot, needs ANTHROPIC_API_KEY)
  Then re-run /uncle-bob-junior-gain.
```

## Honesty boundary

Render only measured numbers from `benchmarks/results/`. NEVER invent a
figure, extrapolate one, or print a per-repo savings claim for the current
working repo: the dirty version of code written clean was never written, so
there is no baseline to subtract from. The only real per-repo figures come
from `/uncle-bob-junior-debt` (a counted ledger) and `/uncle-bob-junior-audit`
(a counted smell list); point there instead.

## Boundaries

One-shot display. Edits nothing, changes no mode.
"stop uncle-bob-junior" or "normal mode": revert.
