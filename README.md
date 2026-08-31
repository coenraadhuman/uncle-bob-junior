<h1 align="center">Uncle Bob Junior</h1>

<p align="center">
  <em>Easy to read. Simple to understand. Safe to change.</em>
</p>

---

You know the reviewer. Never raises their voice. Reads your fifty-line handler,
says nothing, and leaves eleven comments that each name a function you should
have extracted. A month later you thank them, because the change request that
would have taken a day took twenty minutes.

Uncle Bob Junior puts that reviewer inside Claude Code.

## How it works

Before code leaves the agent's hands, every checklist item holds:

```
1.  One job each          — SRP: an "and" in the description means split it
2.  Names reveal intent   — remainingRetries, not n
3.  Small functions       — ten statements max, one thought, one abstraction level
4.  Flat control flow     — guard clauses first, nesting max 2
5.  Logic exists once     — DRY, but duplication beats the wrong abstraction
6.  Simplest design       — KISS + YAGNI, no speculative generality
7.  No magic values       — every meaningful literal gets a named constant
8.  Types over checks     — make invalid states unrepresentable
9.  Immutable by default  — side effects at the edges, the core stays pure
10. Comments say why      — the code says what; doc comments carry contracts
11. House style           — match the project's formatter, linter, idiom
12. Tests prove behavior  — happy-path + edge tests against the public surface
13. Libraries over wheels — SDK first, well-maintained deps, wrapped at a seam
```

The checklist runs *after* the agent understands the problem, not instead of
it: read the code the change touches, trace the real flow, then write.

A **final gate** closes every reply: before sending code, the agent checks its
own answer for tests present in the same reply, functions over ten statements,
nesting past 2, bare literals, and mutable state a `final` field or precise
type could replace. The gate is what makes the test rule bind on smaller
models (see the benchmark below): principles get dropped, output contracts
don't.

Clean, not reckless: it never refactors code it doesn't understand, never
renames public APIs uninvited, and never removes trust-boundary validation,
data-loss handling, security, or accessibility. Deliberate deviations get a
`ubj:` comment naming the reason and cleanup trigger, harvested later by
`/uncle-bob-junior-debt`.

**Trust, but verify.** In Claude Code, a Stop hook closes the loop
mechanically: when the agent finishes a turn, it runs
[habit-hooks](https://github.com/habit-hooks/habit-hooks) over the branch's
changed files, and any findings block the finish once with the report as the
fix-it prompt. Opt-in per project — it only fires where a
`.habit-hooks/config.toml` exists (`habit-hooks init`) and the CLI is on
PATH; it stays silent when uncle-bob-junior is off, and deliberate
deviations are managed with habit-hooks' own snooze.

## Does it work? Measure it

The repo ships its own with/without benchmark: the same tasks, once bare and
once with the ruleset as system prompt, scored by deterministic
judges — code LOC, longest function, nesting depth, magic numbers, short
names, duplication, mutable fields and setters, whether tests ship, and a
functional correctness gate.
No LLM grading, no hand-picked outputs. It runs through
[promptfoo](https://promptfoo.dev), with a web UI showing both arms side by
side; the provider drives your logged-in Claude Code CLI, so no API key is
needed:

```bash
npx promptfoo@latest eval -c benchmarks/promptfooconfig.yaml
npx promptfoo@latest view
```

`/uncle-bob-junior-gain` renders the newest eval as a scoreboard. Method,
caveats, and how to read the numbers: [benchmarks/](benchmarks/).

## Install

Uncle Bob Junior is a Claude Code plugin. Its lifecycle hooks run tiny
Node.js scripts, so `node` needs to be on your PATH (note for Nix/nvm users:
it must be on the non-interactive shell's PATH). If it isn't, the skills
still work, the always-on activation just stays quiet instead of erroring
on every prompt.

```
/plugin marketplace add coenraadhuman/uncle-bob-junior
```
```
/plugin install uncle-bob-junior@uncle-bob-junior
```
(You have to send two separate prompts for the install to work)

Same steps in the Claude Code Desktop app's Code tab: type the two `/plugin` commands above into the prompt box, or click the **+** button next to it, choose **Plugins** → **Add plugin** to browse your configured marketplaces, and manage marketplaces from **Customize** in the sidebar.

Set the level for every new session with the `UNCLE_BOB_JUNIOR_DEFAULT_MODE` env var (`lite`/`full`/`ultra`/`off`), or a `defaultMode` field in `~/.config/uncle-bob-junior/config.json` (`%APPDATA%\uncle-bob-junior\config.json` on Windows). The default is `full`.

While active, the ruleset is also injected into every subagent spawned via the Agent tool. To scope that to specific agent types, set `UNCLE_BOB_JUNIOR_SUBAGENT_MATCHER` to a regex tested against the subagent's `agent_type` (unanchored, case-insensitive; unset injects into every subagent).

Agents working on a repo can also read the compact ruleset straight from a committed [`AGENTS.md`](AGENTS.md) — this repo keeps one for exactly that.

### Uninstall

```
/plugin remove uncle-bob-junior
```

This removes the plugin's own files. Run `node plugins/uncle-bob-junior/scripts/uninstall.js` **before** it to also clean up the mode flag, `~/.config/uncle-bob-junior/config.json`, and (if you accepted the setup nudge) the statusline entry in `~/.claude/settings.json`.

## Commands

| Command                                            | What it does                                                              |
|----------------------------------------------------|---------------------------------------------------------------------------|
| `/uncle-bob-junior [lite \| full \| ultra \| off]` | Set the intensity, or turn it off. No argument reports the current level. |
| `/uncle-bob-junior-review`                         | Review the current diff for clean-code violations, one line per smell.    |
| `/uncle-bob-junior-audit`                          | Audit the whole repo, ranked by change friction, hot files first.         |
| `/uncle-bob-junior-debt`                           | Harvest the `ubj:` deviations you've deferred into a ledger.              |
| `/uncle-bob-junior-gain`                           | Render the newest with/without promptfoo eval as a scoreboard.            |
| `/uncle-bob-junior-help`                           | Quick reference for the commands above.                                   |

Commands ship both as skills and as file-based commands inside the plugin (`plugins/uncle-bob-junior/{skills,commands}`).

## Levels

| Level     | What changes                                                                                                   |
|-----------|----------------------------------------------------------------------------------------------------------------|
| **lite**  | Readability pass only: names, guard clauses, named constants on the code you touch.                            |
| **full**  | The whole checklist enforced on new and changed code, tests included. Default.                                 |
| **ultra** | Hard limits (≤ 10 statements, ≤ 2 nesting), every branch tested, adjacent smells in touched files cleaned too. |

## Showcase site

The showcase site is a [Docusaurus](https://docusaurus.io/) project in
`website/` — libraries over wheels, rule 13 applies to this repo too. It
carries a landing page with the checklist, the benchmark scoreboard with a
mean-score chart, and per-task pages showing the baseline and ruleset code
in tabs with the habit-hooks findings annotated. The benchmark pages are
generated content, never hand-edited:

```bash
node benchmarks/build-site.js            # MDX content from the newest exported run
node benchmarks/build-site.js <eval-id>  # or a specific one
npm --prefix website install             # once
npm --prefix website run build           # render the static site into /docs
npm --prefix website start               # or preview locally with live reload
```

Every eval regenerates the MDX content automatically (the same extension
hook that exports run outcomes); rendering and committing `docs/` is the
deliberate publish step. To serve it, enable Pages once in the repo
settings: Settings → Pages → Deploy from a branch → `main` and `/docs`.
All benchmark code on the site is model-generated output, labelled as such.

## Development

### Repository structure

The plugin payload lives under `plugins/`, cleanly separated from the repo's
tooling — installing the plugin ships only the plugin:

```
uncle-bob-junior/
├── .claude-plugin/
│   └── marketplace.json            # marketplace → ./plugins/uncle-bob-junior
├── plugins/
│   └── uncle-bob-junior/
│       ├── .claude-plugin/
│       │   └── plugin.json         # plugin manifest (hooks entry point)
│       ├── skills/                 # SKILL.md per skill + references/
│       ├── commands/               # file-based /uncle-bob-junior commands
│       ├── hooks/                  # lifecycle hooks incl. habit-hooks verify
│       └── scripts/uninstall.js    # state cleanup
├── benchmarks/                     # with/without eval, judges, site content generator
├── website/                        # Docusaurus showcase site (builds into /docs)
├── tests/                          # repo test suite
└── AGENTS.md                       # compact ruleset for agents working here
```

When changing a rule, keep `plugins/uncle-bob-junior/skills/uncle-bob-junior/SKILL.md` (the runtime
source of truth) and `AGENTS.md` (the compact repo-local version) aligned:

```bash
node scripts/check-rule-copies.js
npm test
```

The benchmark tasks are Java: the correctness gate compiles and runs the generated email and CSV code with the local JDK (`javac` + `java` on PATH); without a JDK those checks report "skipped" and the tests skip cleanly.

## FAQ

**Doesn't clean code mean more code?**
Often, yes: named constants, extracted functions, and tests are lines. The benchmark reports it honestly — the ruleset buys readability and changeability, not brevity. If you want fewer lines above all, this is the wrong plugin.

**What if I really need the quick hack?**
Say so. It builds the hack, marks it with a `ubj:` comment naming the reason and the cleanup trigger, and `/uncle-bob-junior-debt` makes sure "later" stays on the books.

**Why "Uncle Bob Junior"?**
A junior disciple of the clean-code school — the checklist is the community's distilled folklore, not an endorsement by, or affiliation with, any actual person.

## License

[MIT](LICENSE).
