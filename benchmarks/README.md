# Benchmark: with vs without

Does the ruleset actually change the code an agent writes? Two arms, same
model, same tasks:

- **baseline** — the bare task prompt, nothing else.
- **uncle-bob-junior** — the same prompt with `skills/uncle-bob-junior/SKILL.md`
  as system prompt.

Each answer is scored by three deterministic judges, no LLM grading:

- [`habit-hooks-assert.js`](habit-hooks-assert.js) — the smell judge:
  [habit-hooks](https://github.com/habit-hooks/habit-hooks), an independent
  third-party detector, scans the generated Java (java + generic plugins) so
  the ruleset is vetted by a ruler this repo did not write. Scored as a
  penalty from 1 (clean) down to 0, with the per-rule breakdown and
  `File.java:line` locations in the reason; sensor artifacts like
  `incomplete-run` never cost score. Skips cleanly when the CLI is not
  installed.
- [`promptfoo-metrics.js:shipsTests`](promptfoo-metrics.js) — gate: new
  behavior ships with tests, the ruleset's headline rule.
- [`correctness.js`](correctness.js) — gate: the email and csv tasks are
  compiled and executed with the local JDK (`javac` + `java`) against real
  checks, the open-ended tasks get structural checks, so "cleaner" is never
  quietly "broken". Without a JDK on PATH the executable checks report
  "skipped" instead of failing.

## Run it

The benchmark runs through [promptfoo](https://promptfoo.dev), with an
interactive web UI for reading the two arms next to each other:

```bash
npx promptfoo@latest eval -c benchmarks/promptfooconfig.yaml
npx promptfoo@latest view
```

No API key needed: the provider in [`providers/claude-cli.js`](providers/claude-cli.js)
drives the authenticated Claude Code CLI (`claude` on PATH, logged in) with
`--safe-mode`, so your own CLAUDE.md, hooks, and plugins (including an
installed uncle-bob-junior) cannot leak into either arm. Both arms are
prompt functions in [`arms/`](arms/) — baseline sends the bare task, the
ruleset arm loads `skills/uncle-bob-junior/SKILL.md` as system prompt — and
the asserts in [`promptfoo-metrics.js`](promptfoo-metrics.js) wrap the
judges above, plus [`correctness.js`](correctness.js), which promptfoo calls
directly.

The tasks live in [`promptfooconfig.yaml`](promptfooconfig.yaml), all Java:
email validator, CSV sum, retry helper, rate limiter, order processor
(validation + VAT + discount + receipt). Three providers are configured
(haiku, sonnet, fable); trim a run with `--filter-providers haiku` and/or
`--filter-pattern email` while iterating. Single-shot generations, so expect
run-to-run variance; repeat runs before quoting numbers. See
[`examples/`](../examples/) for how the generated comparisons are used, and
`/uncle-bob-junior-gain` renders the newest eval as a scoreboard.

## Run outcomes in `results/`

Every `eval` exports its outcomes automatically to
`benchmarks/results/<eval-id>/` (the directory is gitignored) — the
`extensions` entry in the config runs the exporter after each run. To
re-export a past eval:

```bash
node benchmarks/export-results.js            # newest eval
node benchmarks/export-results.js <eval-id>  # a specific one
```

Each run directory under `benchmarks/results/<eval-id>/` contains:

- `report.md` — scoreboard per task and arm: weighted score, the habit-hooks
  smell breakdown, ships-tests and correctness, plus mean score per arm.
- `src/<task>/<arm>/` — every generated code block as a real Java file, named
  after its declared type, next to `reply.md` with the full verbatim answer.
- `habit-hooks/<task>-<arm>.md` — the complete habit-hooks report for each
  answer, verbatim.

## Reading the results

The honest expectation: the ruleset arm should hold a higher habit-hooks
score and pass the gates (especially ships-tests) — usually at the price of
somewhat more output and cost. If correctness drops in the ruleset arm, that
is a finding, not a formatting issue: report it.

habit-hooks draws its own thresholds (functions over 12 lines, files over
200), stricter than the ruleset's, which is why it scores as a penalty rather
than a gate: the score compares arms, it does not define compliance.
