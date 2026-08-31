# Benchmark: with vs without

Does the ruleset actually change the code an agent writes? Two arms, same
model, same tasks:

- **baseline** — the bare task prompt, nothing else.
- **uncle-bob-junior** — the same prompt with `plugins/uncle-bob-junior/skills/uncle-bob-junior/SKILL.md`
  as system prompt.

Each answer is scored by deterministic judges, no LLM grading:

- [`habit-hooks-assert.js`](habit-hooks-assert.js) — the smell judge:
  [habit-hooks](https://github.com/habit-hooks/habit-hooks), an independent
  third-party detector, scans the extracted code so the ruleset is vetted by
  a ruler this repo did not write. **Each smell from its
  [catch list](https://github.com/habit-hooks/habit-hooks#what-it-catches) is
  its own promptfoo metric** (`hh:oversized-function`,
  `hh:too-many-parameters`, `hh:unused-import`, …): zero occurrences passes,
  any occurrence fails that smell's metric, with `File:line` locations in the
  reason. Enforced smells weigh 1; suggested smells (swallowed-exception,
  duplicated-code, …) weigh 0.5. All rule metrics share one memoized scan per
  answer, and the CLI missing means a clean skip.
  `tests/promptfoo.test.js` proves each Java-relevant rule actually fires.
- [`habit-hooks-assert.js:validCode`](habit-hooks-assert.js) — gate: the
  answer must contain at least one valid compilation unit. Java blocks with
  no top-level type declaration — usage examples, shell output pasted into a
  java fence, bare statements — are snippets and are excluded from judging
  entirely; the benchmark wants valid code.
- [`promptfoo-metrics.js:shipsTests`](promptfoo-metrics.js) — gate: new
  behavior ships with tests, the ruleset's headline rule.
- [`correctness.js`](correctness.js) — gate: the email and csv tasks are
  compiled and executed with the local JDK (`javac` + `java`) against real
  checks, the open-ended tasks get structural checks, so "cleaner" is never
  quietly "broken". Without a JDK on PATH the executable checks report
  "skipped" instead of failing.

A cell passes on its **aggregate weighted score against a 0.9 threshold**
(`defaultTest.threshold`), not on every assert passing: one smell hit
degrades the score (the cell keeps its 0.98) instead of zeroing the cell in
the pass-rate view. The bar sits so that failing any gate (each weighs 2/16,
capping the score at 0.875) still fails the cell, while smell hits alone do
not.

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
installed uncle-bob-junior) cannot leak into either arm. The session also
gets no tools (`--tools ""`): generations are single-shot text, so a model
can never hang the eval by running its own answer (the Game of Life task
produces a program that never exits). Both arms are
prompt functions in [`arms/`](arms/) — baseline sends the bare task, the
ruleset arm loads `plugins/uncle-bob-junior/skills/uncle-bob-junior/SKILL.md` as system prompt — and
the asserts in [`promptfoo-metrics.js`](promptfoo-metrics.js) wrap the
judges above, plus [`correctness.js`](correctness.js), which promptfoo calls
directly.

The tasks live in [`promptfooconfig.yaml`](promptfooconfig.yaml). In Java:
email validator, CSV sum, retry helper, rate limiter, order processor
(validation + VAT + discount + receipt), plus three deliberately
multi-responsibility ones — bank statement analyser, seat booking engine,
and config-language parser — that mix parsing, branching business rules,
and reporting in one ask, so a baseline answer that keeps everything in one
method trips the structural smells rather than only the ships-tests gate.
Two more prove the ruleset is language-neutral: an access-log analyser in
Python (fully judged — the habit-hooks python plugin runs in the bare
extraction dirs) and an expense-claim processor in C# (judged by the gates
plus the generic plugin only: habit-hooks has no C# plugin). Providers are configured one per
model × arm (e.g. `claude-cli:haiku · uncle-bob-junior`), each bound to its
arm's prompt: the promptfoo web UI names its graph series by provider only,
so carrying the arm in the provider label is what makes baseline and ruleset
distinguishable on the graphs. Trim a run with `--filter-providers haiku`
and/or `--filter-pattern email` while iterating. Single-shot generations, so expect
run-to-run variance: for numbers you plan to quote, run with `--repeat 3` —
the exporter writes one report row per repetition and the means average all
of them (the exported source files and the site's task pages keep the last
repetition per arm). TypeScript/JavaScript tasks stay out: habit-hooks'
typescript plugin needs eslint, knip, and ts-morph installed in the scanned
project, which the benchmark's bare extraction dirs cannot provide, so a
TS/JS task would judge as `incomplete-run`. See
the site's Game of Life section for the standalone showcase, and
`/uncle-bob-junior-gain` renders the newest eval as a scoreboard.

## Run outcomes in `results/`

Every `eval` exports its outcomes automatically to
`benchmarks/results/<eval-id>/` — the directory is committed, so the full
run history stays in the repo — then regenerates the site content and
renders the static site into `/docs` (the `extensions` entry in the config
does all three). Runs covering the full task set become the site's featured
scoreboard; anything else files under the site's subset-runs section, so a
cheap `--filter-pattern` iteration run never displaces the headline numbers.
Keep verification runs to a subset; full runs cost real Claude usage.

The raw `reply.md` files are the ground truth; everything else in a run
directory is derived. When the judges or the extraction improve, re-judge
the whole history in place (a run directory with no reply.md at all is
removed):

```bash
node benchmarks/reprocess-results.js
```

To re-export a single past eval from promptfoo's own store:

```bash
node benchmarks/export-results.js            # newest eval
node benchmarks/export-results.js <eval-id>  # a specific one
```

Each run directory under `benchmarks/results/<eval-id>/` contains:

- `report.json` — the same run data machine-readable (rows with gates and
  per-smell counts, means); `build-site.js` generates the Docusaurus site
  content in `website/docs/` from it, automatically after every eval
  (`npm --prefix website run build` renders the site into `/docs`).
- `report.md` — scoreboard per task, model, and arm: weighted score, the
  habit-hooks pass/FAIL verdict, one occurrence-count column per smell,
  ships-tests and correctness, plus mean score per model and arm (as a
  list and as a mermaid bar chart, so the graded gap between arms is
  visible even where pass/fail views flatten it). Two tables carry the
  same rows: a compact one keeping only the smells with at least one hit
  across the run, then the full habit-hooks catch list (enforced smells
  first, then suggested). File and line locations for each hit live in
  the `habit-hooks/` reports.
- `src/<task>/<model>/<arm>/main/` — the production code as real source
  files, one file per top-level Java type (imports attributed to the types
  that use them; a genuinely unused import survives once, so the
  unused-import rule still sees it). Snippet blocks — no top-level type —
  are excluded. Shipped tests land in `test/`, and `reply.md` holds the full
  verbatim answer.
- `habit-hooks/<task>-<model>-<arm>.md` — habit-hooks run **directly on that
  answer's exported `main/` files**, so every `File.java:line` in the report
  points at a file in the run directory. The plugin set matches the languages
  the answer used (java, python, typescript, php + generic), and the scan
  config is removed afterwards.

## Game of Life examples (standalone)

[`promptfooconfig.gameoflife.yaml`](promptfooconfig.gameoflife.yaml) is a
separate showcase config, deliberately not part of the main suite: it runs
only when invoked explicitly:

```bash
npx promptfoo@latest eval -c benchmarks/promptfooconfig.gameoflife.yaml
```

One task (a terminal-only Conway's Game of Life with a Maven `pom.xml`, the
grid redrawn in place with no new lines, each generation printed to stdout),
both arms, all three models. There are no eval-time judges: each run is
stored as a full run directory under `benchmarks/game-of-life-results/`
(same structure as `results/` — report, extracted sources, habit-hooks
findings, produced by the shared exporter), gets its own page in the site's
Game of Life section, and is re-judged by `reprocess-results.js` alongside
the regular runs. Re-export a past run with
`node benchmarks/gameoflife-examples.js <eval-id>`.

## Reading the results

The honest expectation: the ruleset arm should hold a higher habit-hooks
score and pass the gates (especially ships-tests) — usually at the price of
somewhat more output and cost. If correctness drops in the ruleset arm, that
is a finding, not a formatting issue: report it.

habit-hooks draws its own thresholds (functions over 12 lines, files over
200), stricter than the ruleset's, which is why it scores as a penalty rather
than a gate: the score compares arms, it does not define compliance.
