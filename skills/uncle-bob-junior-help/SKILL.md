---
name: uncle-bob-junior-help
description: >
  Quick-reference card for all uncle-bob-junior modes, skills, and commands.
  One-shot display, not a persistent mode. Trigger: /uncle-bob-junior-help,
  "uncle-bob-junior help", "what clean-code commands", "how do I use
  uncle-bob-junior".
---

# Uncle Bob Junior Help

Display this reference card when invoked. One-shot, do NOT change mode,
write flag files, or persist anything.

## Levels

| Level | Trigger | What change |
|-------|---------|-------------|
| **Lite** | `/uncle-bob-junior lite` | Readability pass only: names, guard clauses, named constants on touched code. |
| **Full** | `/uncle-bob-junior` | The checklist enforced: SRP → names → small functions → flat flow → DRY → no magic values → precise types → immutability → libraries over wheels → tests. Default. |
| **Ultra** | `/uncle-bob-junior ultra` | Hard limits (≤ 10 statements, ≤ 2 nesting), every branch tested, adjacent smells in touched files cleaned too. |

Level sticks until changed or session end.

## Skills

| Skill | Trigger | What it does |
|-------|---------|--------------|
| **uncle-bob-junior** | `/uncle-bob-junior` | Clean-code mode itself. Easy to read, simple to understand, safe to change. |
| **uncle-bob-junior-review** | `/uncle-bob-junior-review` | Smell review of the diff: `L42: magic: bare 86400. CACHE_TTL_SECONDS.` |
| **uncle-bob-junior-audit** | `/uncle-bob-junior-audit` | Whole-repo smell audit: ranked list of the hardest-to-change spots. |
| **uncle-bob-junior-debt** | `/uncle-bob-junior-debt` | Harvest `ubj:` deviation comments into a tracked ledger. |
| **uncle-bob-junior-gain** | `/uncle-bob-junior-gain` | Measured with/without scoreboard from this repo's benchmark results. |
| **uncle-bob-junior-help** | `/uncle-bob-junior-help` | This card. |

Codex uses `@uncle-bob-junior`, `@uncle-bob-junior-review`, and
`@uncle-bob-junior-help`; Claude Code and OpenCode use the slash-command
forms above (OpenCode ships all six as slash commands).

## Deactivate

Say "stop uncle-bob-junior" or "normal mode". Resume anytime with
`/uncle-bob-junior`. `/uncle-bob-junior off` also works.

## Configure Default Mode

Default mode = `full`, auto-active every session. Change it:

**Environment variable** (highest priority):
```bash
export UNCLE_BOB_JUNIOR_DEFAULT_MODE=ultra
```

**Config file** (`~/.config/uncle-bob-junior/config.json`, Windows: `%APPDATA%\uncle-bob-junior\config.json`):
```json
{ "defaultMode": "lite" }
```

Set `"off"` to disable auto-activation on session start, activate manually
with `/uncle-bob-junior` when wanted.

Resolution: env var > config file > `full`.

## Update

Enable auto-update once: open `/plugin`, go to Marketplaces, pick uncle-bob-junior, Enable auto-update. Claude Code then pulls new versions at startup (run `/reload-plugins` when it prompts). Manual refresh: `/plugin marketplace update uncle-bob-junior` then `/reload-plugins`.

If `/plugin` is not recognized, your Claude Code is out of date. Update it (`npm install -g @anthropic-ai/claude-code@latest`, or `brew upgrade claude-code`) and restart. Other hosts use their own update flow.

## More

Full docs + examples: https://github.com/coenraadhuman/uncle-bob-junior
