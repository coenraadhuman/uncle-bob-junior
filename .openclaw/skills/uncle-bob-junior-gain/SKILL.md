---
name: uncle-bob-junior-gain
description: "Show the measured with/without scoreboard from the newest promptfoo eval: checklist gates, smell penalties, tests. One-shot display."
homepage: https://github.com/coenraadhuman/uncle-bob-junior
license: MIT
---

# Uncle Bob Junior Gain

Display the benchmark scoreboard when invoked. One-shot: do NOT change mode,
write flag files, or persist anything.

## Source

The figures come from this repo's own with/without benchmark, run through
promptfoo: the same tasks answered by the same model, once bare and once with
the ruleset as system prompt, scored by deterministic judges
(`benchmarks/promptfoo-metrics.js` + `correctness.js`). Read the newest eval
and render a per-arm scoreboard — gates passed (functions ≤ 20 lines, flat
control flow, ships tests, correct), smell penalty scores, and the weighted
score per arm:

```bash
npx promptfoo@latest list evals -n 1          # newest eval id
npx promptfoo@latest export eval <id> -o -    # full results as JSON on stdout
```

In the export, `results.results[]` carries one entry per task × arm:
`prompt.label` is the arm, `testCase.description` the task,
`gradingResult.score` the weighted score, and
`gradingResult.componentResults[]` the per-metric pass/score/reason.

If no eval exists yet, say so and point at how to produce one:

```
  No benchmark results yet.

  Run the with/without comparison (needs a logged-in `claude` CLI, no API key):
    npx promptfoo@latest eval -c benchmarks/promptfooconfig.yaml
  Then re-run /uncle-bob-junior-gain, or open the UI: npx promptfoo@latest view
```

## Honesty boundary

Render only measured numbers from promptfoo evals. NEVER invent a figure,
extrapolate one, or print a per-repo savings claim for the current working
repo: the dirty version of code written clean was never written, so there is
no baseline to subtract from. The only real per-repo figures come from
`/uncle-bob-junior-debt` (a counted ledger) and `/uncle-bob-junior-audit`
(a counted smell list); point there instead.

## Boundaries

One-shot display. Edits nothing, changes no mode.
"stop uncle-bob-junior" or "normal mode": revert.
