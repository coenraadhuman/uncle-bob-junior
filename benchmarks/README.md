# Benchmark: with vs without

Does the ruleset actually change the code an agent writes? Two arms, same
model, same tasks:

- **baseline** — the bare task prompt, nothing else.
- **uncle-bob-junior** — the same prompt with `skills/uncle-bob-junior/SKILL.md`
  as system prompt.

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

The asserts come in three kinds, so the arm columns read directly (higher
score = cleaner, pass rate = checklist compliance):

- **Gates** fail on the ruleset's own rules: no function over 20 lines, no
  nesting past 2 levels inside a method, tests ship, and the answer is
  functionally correct.
- **Penalties** score smell density from 1 (clean) down to 0: magic numbers,
  mutable fields, setters. They never fail — the rulers have known false
  positives, so counts inform without gating.
- **Raw measurements** (LOC, longest function, max nesting) carry weight 0:
  visible in the UI, excluded from the score.

The tasks live in [`promptfooconfig.yaml`](promptfooconfig.yaml), all Java:
email validator, CSV sum, retry helper, rate limiter, order processor
(validation + VAT + discount + receipt). Three providers are configured
(haiku, sonnet, fable); trim a run with `--filter-providers haiku` and/or
`--filter-pattern email` while iterating. Single-shot generations, so expect
run-to-run variance; repeat runs before quoting numbers. See
[`examples/`](../examples/) for how the generated comparisons are used, and
`/uncle-bob-junior-gain` renders the newest eval as a scoreboard.

## Reading the results

The honest expectation: the ruleset arm should pass more gates (especially
ships-tests) and hold higher penalty scores — usually at the price of somewhat
more LOC (tests and named constants are lines too) and cost. If correctness
drops in the ruleset arm, that is a finding, not a formatting issue: report it.

The metrics are regex-based rulers, not compilers (see the `ubj:` note in
`clean-code-metrics.js`). They are stable enough to compare two arms on the
same task; do not quote them as absolute code-quality scores.
