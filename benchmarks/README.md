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
  the ruleset is vetted by a ruler this repo did not write. The verdict
  mirrors habit-hooks' own
  [catch list](https://github.com/habit-hooks/habit-hooks#what-it-catches):
  **enforced** smells (oversized-function, too-many-parameters,
  high-complexity, unused-variable, unused-import, …) fail the answer, while
  **suggested** smells (swallowed-exception, duplicated-code, …) are advisory.
  The 0..1 score stays granular beyond the verdict, the reason carries the
  per-rule breakdown with `File.java:line` locations, and sensor artifacts
  like `incomplete-run` never count. Skips cleanly when the CLI is not
  installed. `tests/promptfoo.test.js` proves each Java-relevant rule from
  the catch list actually fires.
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

- `report.md` — scoreboard per task, model, and arm: weighted score, the
  habit-hooks pass/FAIL verdict with the smell breakdown, ships-tests and
  correctness, plus mean score per model and arm.
- `src/<task>/<model>/<arm>/main/` — the production code as real source
  files, one file per top-level Java type (imports attributed to the types
  that use them; a genuinely unused import survives once, so the
  unused-import rule still sees it). Shipped tests land in `test/`, and
  `reply.md` holds the full verbatim answer.
- `habit-hooks/<task>-<model>-<arm>.md` — habit-hooks run **directly on that
  answer's exported `main/` files**, so every `File.java:line` in the report
  points at a file in the run directory. The plugin set matches the languages
  the answer used (java, python, typescript, php + generic), and the scan
  config is removed afterwards.

## Reading the results

The honest expectation: the ruleset arm should hold a higher habit-hooks
score and pass the gates (especially ships-tests) — usually at the price of
somewhat more output and cost. If correctness drops in the ruleset arm, that
is a finding, not a formatting issue: report it.

habit-hooks draws its own thresholds (functions over 12 lines, files over
200), stricter than the ruleset's, which is why it scores as a penalty rather
than a gate: the score compares arms, it does not define compliance.
