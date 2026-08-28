---
name: uncle-bob-junior-audit
description: "Audit the whole repo for clean-code violations. A ranked list of the hardest-to-read, hardest-to-change spots, hot files first."
homepage: https://github.com/coenraadhuman/uncle-bob-junior
license: MIT
---

uncle-bob-junior-review, repo-wide. Scan the whole tree instead of a diff.
Rank findings by how much they slow down the next change: the file everyone
edits weekly outranks the script nobody touches.

## Tags

Same as uncle-bob-junior-review:

- `name:` a name that hides intent. Give the intent-revealing name.
- `long:` function over ten statements or doing more than one thing. Name the extraction seams.
- `nest:` nesting deeper than 2 levels. Show what flattens it.
- `dup:` the same logic in two or more places. Name the shared function.
- `magic:` a bare literal with meaning. Give the constant name.
- `srp:` a function, class, or module with two jobs. Name the split.
- `dead:` commented-out code, unused exports, unreachable paths. Delete.
- `type:` a runtime check a precise type would forbid. Name the enum, value object, or non-nullable field.
- `wheel:` hand-rolled code the standard library or an existing dependency already provides, or a third-party type leaking through the codebase unwrapped. Name the replacement call or the boundary seam.
- `mut:` shared mutable state or side effects buried in core logic. Name what moves to the edge.
- `untested:` behavior-bearing code with no test, or tests welded to internals. Name the missing cases.

## Hunt

God functions and god classes, copy-pasted blocks across files, single-letter
and numbered names (`data2`, `handler3`), bare literals in conditions,
boolean flag parameters, empty catch blocks, commented-out code, modules with
no tests next to modules that change often (`git log --format= --name-only |
sort | uniq -c | sort -rn` finds the hot files).

## Output

One line per finding, ranked: `<tag> <what>. <fix>. [path]`.
End with `smells: <N> across <M> files; hottest: <path>.`
Nothing to flag: `Clean already. Ship.`

## Boundaries

Scope: readability, structure, and changeability only. Correctness bugs,
security holes, and performance are explicitly out of scope. Route them to a
normal review pass. `ubj:` comments naming a reason and cleanup trigger are
tracked decisions, not findings (harvest those with /uncle-bob-junior-debt).
Lists findings, applies nothing. One-shot.
"stop uncle-bob-junior-audit" or "normal mode" to revert.
