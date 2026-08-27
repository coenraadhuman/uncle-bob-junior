<h1 align="center">Uncle Bob Junior</h1>

<p align="center">
  <em>Easy to read. Simple to understand. Safe to change.</em>
</p>

---

You know the reviewer. Never raises their voice. Reads your fifty-line handler,
says nothing, and leaves eleven comments that each name a function you should
have extracted. A month later you thank them, because the change request that
would have taken a day took twenty minutes.

Uncle Bob Junior puts that reviewer inside your AI agent.

## Before / after

You ask for "handle the incoming order". Your agent writes one 46-line
function that parses, validates, prices, saves, and emails, with a bare `5`
and a `900000` in the middle and no test.

With uncle-bob-junior:

```python
def handle_order(request):
    order_data = parse_order(request.body)
    validate_order(order_data)
    order = save_order(order_data)
    send_confirmation(order)
    return order
```

...plus the named constants, and tests for the happy path and the edges.
More before/afters in [examples/](examples/).

## How it works

Before code leaves the agent's hands, every checklist item holds:

```
1.  One job each          — SRP: an "and" in the description means split it
2.  Names reveal intent   — remainingRetries, not n
3.  Small functions       — under 20 lines, one thought, one abstraction level
4.  Flat control flow     — guard clauses first, nesting max 2
5.  Logic exists once     — DRY, but duplication beats the wrong abstraction
6.  Simplest design       — KISS + YAGNI, no speculative generality
7.  No magic values       — every meaningful literal gets a named constant
8.  Types over checks     — make invalid states unrepresentable
9.  Immutable by default  — side effects at the edges, the core stays pure
10. Comments say why      — the code says what; doc comments carry contracts
11. House style           — match the project's formatter, linter, idiom
12. Tests prove behavior  — happy-path + edge tests against the public surface
```

The checklist runs *after* the agent understands the problem, not instead of
it: read the code the change touches, trace the real flow, then write.

A **final gate** closes every reply: before sending code, the agent checks its
own answer for tests present in the same reply, functions over 20 lines,
nesting past 2, bare literals, and mutable state a `final` field or precise
type could replace. The gate is what makes the test rule bind on smaller
models (see the benchmark below): principles get dropped, output contracts
don't.

Clean, not reckless: it never refactors code it doesn't understand, never
renames public APIs uninvited, and never removes trust-boundary validation,
data-loss handling, security, or accessibility. Deliberate deviations get a
`ubj:` comment naming the reason and cleanup trigger, harvested later by
`/uncle-bob-junior-debt`.

## Does it work? Measure it

The repo ships its own with/without benchmark: the same tasks through a
headless agent, once bare and once with the ruleset, scored by deterministic
judges — code LOC, longest function, nesting depth, magic numbers, short
names, duplication, mutable fields and setters, whether tests ship, and a
functional correctness gate.
No LLM grading, no hand-picked outputs.

```bash
node benchmarks/run-clean-code.js --runs 4
```

Reports land in `benchmarks/results/`, and `/uncle-bob-junior-gain` renders
the newest one as a scoreboard. Method, caveats, and how to read the numbers:
[benchmarks/](benchmarks/).

### Latest results (27-08-2026, per-generation means)

Java tasks (email validator, CSV parser, retry helper, rate limiter, order
processor). Lower is better everywhere except **ships tests** and **correct**.

| model | arm | longest fn | fns > 20 | nesting | magic | mutable fields | ships tests | correct | cost/gen |
|-------|-----|--:|--:|--:|--:|--:|--:|--:|--:|
| haiku | baseline | 22.7 | 0.56 | 4.5 | 4.2 | 0.70 | 2% | 98% | $0.02 |
| haiku | ruleset | **14.9** | **0.18** | **4.0** | **2.6** | **0.26** | **72%** | 98% | $0.03 |
| sonnet | baseline | 22.4 | 0.50 | 4.3 | 3.3 | 0.08 | 0% | 100% | $0.06 |
| sonnet | ruleset | **12.5** | **0.00** | **3.6** | **1.3** | 0.22 | **98%** | 100% | $0.14 |
| fable | baseline | 24.2 | 0.40 | 4.1 | 5.4 | 0.00 | 20% | 100% | $0.25 |
| fable | ruleset\* | **13.8** | **0.00** | **3.8** | **1.7** | 0.00 | **80%** | 100% | $0.41 |

Haiku and sonnet: n=50 generations per arm (10 runs × 5 tasks) with the
final-gate ruleset. \*Fable: n=10 per arm, measured before the final gate was
added; its ships-tests share is expected to rise like sonnet's did (0% → 98%
with the gate). Nesting counts braces from the file top, so Java's floor is 3
(class + method + one block). Correctness never regresses: the ruleset arm
passes the functional gate exactly as often as baseline.

### What the ruleset arm actually writes

**haiku, rate limiter.** Baseline inlines the policy (`new RateLimiter(10,
60_000)` with comments doing a constant's job) and ships no tests. The
ruleset arm names the policy and proves the behavior:

```java
private static final int MAX_REQUESTS = 10;
private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);
```

...plus `allowsRequestsUnderLimit`, `blocksRequestsOverLimit`,
`isolatesDifferentClients`, and `resetsAfterTimeWindow` tests. This is the
model that shipped tests in 0/10 runs before the final gate.

**sonnet, order processor.** Baseline: one file holding parsing, validation,
pricing, and rendering. The ruleset arm splits along the checklist's seams,
with types over checks (`EmptyOrderException`, `InvalidLineItemException`
instead of boolean returns), value objects (`LineItem`, `OrderTotals`), and
one job per class:

```
LineItem.java  OrderTotals.java  OrderCalculator.java  OrderProcessor.java
ReceiptFormatter.java  EmptyOrderException.java  InvalidLineItemException.java
LineItemTest.java  OrderCalculatorTest.java  OrderProcessorTest.java
```

**fable, retry helper.** Baseline: a 103-line `Retry` monolith with a usage
example instead of tests. The ruleset arm: an 88-line `RetryPolicy` with a
typed `RetryExhaustedException`, `CheckedRunnable`/`CheckedSupplier`
interfaces so callers say what they mean, and an 88-line `RetryPolicyTest`,
test code roughly matching production code line for line.

Every generation behind these numbers is in `benchmarks/results/<run>/src/`,
organised as `<task>/<arm>-run<N>/<File>.java`; nothing is hand-picked.

## Install

The Claude Code and Codex plugins run two tiny Node.js lifecycle hooks, so `node` needs to be on your PATH (note for Nix/nvm users: it must be on the non-interactive shell's PATH). If it isn't, the skills still work, the always-on activation just stays quiet instead of erroring on every prompt.

### Claude Code

```
/plugin marketplace add coenraadhuman/uncle-bob-junior
```
```
/plugin install uncle-bob-junior@uncle-bob-junior
```
(You have to send two separate prompts for the install to work)

Same steps in the Claude Code Desktop app's Code tab: type the two `/plugin` commands above into the prompt box, or click the **+** button next to it, choose **Plugins** → **Add plugin** to browse your configured marketplaces, and manage marketplaces from **Customize** in the sidebar.

### Codex

```bash
codex plugin marketplace add coenraadhuman/uncle-bob-junior
codex plugin add uncle-bob-junior@uncle-bob-junior
```

Run `codex` and open `/hooks`, review and trust its two lifecycle hooks, and start a new thread. The same install covers the Codex desktop app: restart the app after installing.

### GitHub Copilot CLI

```bash
copilot plugin marketplace add coenraadhuman/uncle-bob-junior
copilot plugin install uncle-bob-junior@uncle-bob-junior
```

In an interactive Copilot CLI session, use the slash equivalents of the two commands above. Copilot CLI namespaces plugin commands by plugin name, e.g. `/uncle-bob-junior:uncle-bob-junior ultra`.

### Pi agent harness

```
pi install git:github.com/coenraadhuman/uncle-bob-junior
```

### OpenCode

Add to `opencode.json`:

```json
{ "plugin": ["@coenraadhuman/uncle-bob-junior"] }
```

Or run from a checkout (the plugin reuses `hooks/` and `skills/`):

```json
{ "plugin": ["./.opencode/plugins/uncle-bob-junior.mjs"] }
```

Injects the ruleset every turn at the active level and adds the `/uncle-bob-junior` commands. OpenCode also auto-loads this repo's `AGENTS.md`, so the rules hold even without the plugin; the plugin adds the `lite/full/ultra/off` levels.

### Gemini CLI

```bash
gemini extensions install https://github.com/coenraadhuman/uncle-bob-junior
```

Loads the ruleset as always-on context every session and registers the `/uncle-bob-junior` commands; the `skills/` ship too.

### Qoder

Qoder auto-loads `AGENTS.md` from the repo root, so running from a checkout works with zero setup. For per-project rules, copy [`.qoder/rules/uncle-bob-junior.md`](.qoder/rules/uncle-bob-junior.md) into your project's `.qoder/rules/`. For full plugin-tier support (mode activation + per-prompt injection), add the hooks from [`hooks/qoder-hooks.json`](hooks/qoder-hooks.json) to your `.qoder/settings.json`, replacing `UNCLE_BOB_JUNIOR_DIR` with your checkout path.

### Hermes Agent

```bash
hermes plugins install coenraadhuman/uncle-bob-junior --enable
```

Restart Hermes after installing. In shared gateways, restrict `/uncle-bob-junior` to trusted users with Hermes slash-command access controls.

### Devin CLI

```bash
devin plugins install coenraadhuman/uncle-bob-junior
```

### Grok Build

```bash
grok plugin install coenraadhuman/uncle-bob-junior --trust
```

Enable the plugin (off by default) via `/plugins` or `~/.grok/config.toml`, then start a new session.

### OpenClaw

```bash
clawhub install uncle-bob-junior
```

The review, audit, debt, gain, and help skills install the same way. Without ClawHub, copy [`.openclaw/skills/uncle-bob-junior`](.openclaw/skills/) into `~/.openclaw/skills/`.

### Instruction-only hosts

Cursor, Windsurf, Cline, GitHub Copilot Chat, Kiro, Zed, CodeWhale, Swival, Amp, Jules, JetBrains Junie, VS Code + Codex, Antigravity: copy the matching rules file ([`.cursor/rules/`](.cursor/rules/), [`.windsurf/rules/`](.windsurf/rules/), [`.clinerules/`](.clinerules/), [`.github/copilot-instructions.md`](.github/copilot-instructions.md), [`.kiro/steering/`](.kiro/steering/), [`.qoder/rules/`](.qoder/rules/), or plain [`AGENTS.md`](AGENTS.md)). Which files map to which agent: [Agent portability](docs/agent-portability.md).

Set the level for every new session with the `UNCLE_BOB_JUNIOR_DEFAULT_MODE` env var (`lite`/`full`/`ultra`/`off`), or a `defaultMode` field in `~/.config/uncle-bob-junior/config.json` (`%APPDATA%\uncle-bob-junior\config.json` on Windows). The default is `full`.

While active, the ruleset is also injected into every subagent spawned via the Agent tool. To scope that to specific agent types, set `UNCLE_BOB_JUNIOR_SUBAGENT_MATCHER` to a regex tested against the subagent's `agent_type` (unanchored, case-insensitive; unset injects into every subagent).

### Uninstall

| Host | Command |
|------|---------|
| Claude Code | `/plugin remove uncle-bob-junior` |
| Codex | `codex plugin remove uncle-bob-junior` |
| Devin CLI | `devin plugins remove uncle-bob-junior` |
| Grok Build | `grok plugin uninstall uncle-bob-junior` |
| Pi agent | `pi uninstall uncle-bob-junior` |
| Cursor / Windsurf / Cline / Qoder / etc. | Delete the copied rule file |

These remove the plugin's own files. Run `node scripts/uninstall.js` **before** the host remove command to also clean up the mode flag, `~/.config/uncle-bob-junior/config.json`, and (if you accepted the setup nudge) the statusline entry in `~/.claude/settings.json`.

## Commands

| Command | What it does |
|---------|--------------|
| `/uncle-bob-junior [lite \| full \| ultra \| off]` | Set the intensity, or turn it off. No argument reports the current level. |
| `/uncle-bob-junior-review` | Review the current diff for clean-code violations, one line per smell. |
| `/uncle-bob-junior-audit` | Audit the whole repo, ranked by change friction, hot files first. |
| `/uncle-bob-junior-debt` | Harvest the `ubj:` deviations you've deferred into a ledger. |
| `/uncle-bob-junior-gain` | Render the newest with/without benchmark result as a scoreboard. |
| `/uncle-bob-junior-help` | Quick reference for the commands above. |

Commands need a skill-capable host (Claude Code, Codex, Devin CLI, OpenCode, Gemini, pi, Swival, Hermes Agent, Qoder, Grok Build). In Codex they're skills, invoke with `@`. The instruction-only adapters load the always-on ruleset without the commands.

## Levels

| Level | What changes |
|-------|--------------|
| **lite** | Readability pass only: names, guard clauses, named constants on the code you touch. |
| **full** | The whole checklist enforced on new and changed code, tests included. Default. |
| **ultra** | Hard limits (≤ 20 lines, ≤ 2 nesting), every branch tested, adjacent smells in touched files cleaned too. |

## Development

When changing the compact rule text, keep the agent copies aligned:

```bash
node scripts/check-rule-copies.js
npm test
```

The OpenClaw skill package (`.openclaw/skills/`) is generated from `skills/`; rerun `node scripts/build-openclaw-skills.js` after changing a skill — the test suite fails if it is stale.

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
