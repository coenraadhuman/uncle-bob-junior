---
name: uncle-bob-junior-debt
description: "Harvest every ubj: deviation comment into one cleanup ledger, so deferrals get tracked instead of forgotten. One-shot report."
homepage: https://github.com/coenraadhuman/uncle-bob-junior
license: MIT
---

Every deliberate deviation from the clean-code checklist is marked with a
`ubj:` comment naming its reason and cleanup trigger. This collects them into
one ledger so a deferral can't quietly become permanent.

## Scan

Grep the repo for comment markers, skipping `node_modules`, `.git`, and build
output:

`grep -rnE '(#|//) ?ubj:' .`  (add other comment prefixes if your stack uses them)

Each hit is one ledger row. The comment prefix keeps prose that merely mentions
the convention out of the ledger.

## Output

One row per marker, grouped by file:

`<file>:<line>, <what was deferred>. reason: <why it was kept dirty>. trigger: <when to clean it>.`

The convention is `ubj: <reason>, <cleanup trigger>`, so pull the reason
and the trigger straight from the comment. Want an owner per row too? add
`git blame -L<line>,<line>`.

Flag the rot risk: any `ubj:` comment that names no cleanup trigger gets a
`no-trigger` tag, those are the ones that silently rot.

End with `<N> markers, <M> with no trigger.` Nothing found: `No ubj: debt. Clean ledger.`

## Boundaries

Reads and reports only, changes nothing. To persist it, ask and it writes the
ledger to a file (e.g. `CLEANUP-DEBT.md`). One-shot. "stop uncle-bob-junior-debt"
or "normal mode" to revert.
