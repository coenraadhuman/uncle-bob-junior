# Benchmark: with vs without

Does the ruleset actually change the code an agent writes? Two arms, same
model, same tasks:

- **baseline** — the bare task prompt, nothing else.
- **uncle-bob-junior** — the same prompt with `skills/uncle-bob-junior/SKILL.md`
  appended as system prompt.

Each answer's fenced code is scored by two deterministic judges, no LLM grading:

- [`clean-code-metrics.js`](clean-code-metrics.js) — code LOC, longest function,
  functions over 20 lines, max nesting depth, magic numbers, short names,
  duplicate blocks, mutable (non-final) Java fields, setters, whether tests
  ship with the code. Zero dependencies;
  proven by `tests/clean-code-metrics.test.js`.
- [`correctness.js`](correctness.js) — gates the generated Java: the email and
  csv tasks are compiled and executed with the local JDK (`javac` + `java`)
  against real checks, the open-ended tasks get structural checks, so
  "cleaner" is never quietly "broken". Without a JDK on PATH the executable
  checks report "skipped" instead of failing.

## Run it

Needs an authenticated Claude Code install (`claude` on PATH); no API key
plumbing. Each session runs `--safe-mode`, so your own CLAUDE.md, hooks, and plugins (including
an installed uncle-bob-junior) cannot leak into either arm.

```bash
node benchmarks/run-clean-code.js                     # 5 tasks, 1 run each, haiku
node benchmarks/run-clean-code.js --model sonnet --runs 4
node benchmarks/run-clean-code.js --tasks email,csv   # subset while iterating
```

Each run gets its own directory, `results/<date>-<model>/`, containing:

- `report.md` — medians per task + summary.
- `src/<task>/<arm>-run<N>/` — every generated code block saved as a real
  file, Java files named after their declared type (so a ruleset run reads as
  `EmailValidator.java` + `EmailValidatorTest.java`); alternative
  implementations of the same type get an `alt-` prefix.
- `sources.md` — the same code inline, baseline directly above the ruleset
  arm, for side-by-side reading.
- `raw.json` — per-run scores plus full reply text, persisted after every
  task so an interrupted run stays rescoreable.

`/uncle-bob-junior-gain` renders the newest run's report as a scoreboard.

Tasks live in [`tasks.json`](tasks.json), all Java: email validator, CSV sum,
retry helper, rate limiter, order processor (validation + VAT + discount +
receipt). Single-shot generations, default temperature, so expect run-to-run
variance; use `--runs 4` or more for numbers you want to quote.

## Reading the results

Lower is better on every metric except **ships tests** and **correct**. The
honest expectation: the ruleset arm should cut magic numbers, short names,
nesting, and long functions, and raise the ships-tests share — usually at the
price of somewhat more LOC (tests and named constants are lines too) and cost.
If correctness drops in the ruleset arm, that is a finding, not a formatting
issue: report it.

The metrics are regex-based rulers, not compilers (see the `ubj:` note in
`clean-code-metrics.js`). They are stable enough to compare two arms on the
same task; do not quote them as absolute code-quality scores.
