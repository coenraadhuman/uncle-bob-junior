---
name: uncle-bob-junior-review
description: >
  Code review focused exclusively on clean-code violations. Finds what makes
  the diff hard to read, understand, or change: vague names, long functions,
  deep nesting, duplicated logic, magic values, mixed responsibilities,
  missing tests. One line per finding: location, the smell, the fix. Use when
  the user says "review for readability", "is this clean", "clean code
  review", "find the smells", or invokes /uncle-bob-junior-review.
  Complements correctness-focused review, this one only hunts readability
  and changeability.
---

Review diffs for clean-code violations. One line per finding: location, the
smell, the fix. The diff's best outcome is code the next reader understands
on the first pass.

## Format

`L<line>: <tag> <what>. <fix>.`, or `<file>:L<line>: ...` for
multi-file diffs.

Tags:

- `name:` a name that hides intent (`data2`, `tmp`, `doStuff`). Give the intent-revealing name.
- `long:` function over 20 lines or doing more than one thing. Name the extraction seams.
- `nest:` nesting deeper than 2 levels. Show the guard clause or extraction that flattens it.
- `dup:` the same logic in two places. Name the shared function to extract; flag near-duplicates only when a third use exists.
- `magic:` a bare literal with meaning. Give the constant name.
- `srp:` a function or class with two jobs (an "and" in its description, a boolean flag parameter). Name the split.
- `dead:` commented-out code, unused branches, unreachable paths. Delete.
- `type:` a runtime check guarding a state a precise type would forbid (a mode string, a nullable that is never null, raw primitives passed as a group). Name the enum, value object, or non-nullable field.
- `mut:` shared mutable state or a side effect buried in core logic. Name where the effect moves (the edge) or what becomes immutable.
- `untested:` new or changed behavior with no test, or tests welded to internals that a pure refactor would break. Name the missing cases (happy path, edges) or the public surface to test through.

## Examples

❌ "This function is a bit long and could perhaps be broken up into smaller
pieces for improved readability and maintainability."

✅ `L12-58: long: 46-line handler parses, validates, and saves. Extract parseOrder(), validateOrder(); handler keeps the orchestration.`

✅ `L23: name: 'flag' controls retry behavior. Split into fetchOnce() / fetchWithRetry(), callers say what they mean.`

✅ `L31: magic: bare 86400 in expiry check. SECONDS_PER_DAY, or better CACHE_TTL_SECONDS.`

✅ `orders.py:L88: nest: 4 levels deep. Guard-clause the empty cart and the missing user; happy path lands at depth 1.`

✅ `L40-55, L92-107: dup: same address normalisation twice. Extract normalizeAddress(), one owner for the rule.`

✅ `L60: untested: new discount branch has no test. Cover percentage, fixed-amount, and zero-total cases.`

## Scoring

End with the readability verdict: `smells: <N> (<tags>).`

If there is nothing to flag, say `Clean already. Ship.` and stop.

## Boundaries

Scope: readability, structure, and changeability only. Correctness bugs,
security holes, and performance are explicitly out of scope. Route them to a
normal review pass, not this one. A deliberate deviation carrying a `ubj:`
comment that names its reason and cleanup trigger is a tracked decision, not
a smell, never flag it. Does not apply the fixes, only lists them.
"stop uncle-bob-junior-review" or "normal mode": revert to verbose review style.
