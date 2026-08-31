---
title: Commands & Levels
sidebar_position: 6
---

*Embedded from the repository README on every site build.*

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
