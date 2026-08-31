---
title: Claude Code Plugin
sidebar_position: 5
---

*Embedded from the repository README on every site build.*

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

Agents working on a repo can also read the compact ruleset straight from a committed [`AGENTS.md`](https://github.com/coenraadhuman/uncle-bob-junior/blob/main/AGENTS.md) — this repo keeps one for exactly that.

### Plugin Update

```
/plugin marketplace update uncle-bob-junior
```
```
/plugin update uncle-bob-junior@uncle-bob-junior
```

```
/reload-plugins 
```

### Uninstall

```
/plugin remove uncle-bob-junior
```

This removes the plugin's own files. Run `node plugins/uncle-bob-junior/scripts/uninstall.js` **before** it to also clean up the mode flag, `~/.config/uncle-bob-junior/config.json`, and (if you accepted the setup nudge) the statusline entry in `~/.claude/settings.json`.
