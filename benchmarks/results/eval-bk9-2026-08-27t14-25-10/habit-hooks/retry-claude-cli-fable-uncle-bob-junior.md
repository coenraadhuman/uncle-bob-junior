── oversized-function (1 issue) ──

Functions over 12 lines almost always carry more than one responsibility, and that is the smell to chase — not the line count itself.

Analyse responsibilities first: what distinct concerns does this function handle? Ask: (1) Are these separate responsibilities that belong in different methods? (2) Should this become a class with multiple methods? (3) Can you group cohesive data into objects to reduce local variables?

Avoid mechanical extraction. Pulling out a `helperA` / `helperB` purely to satisfy the threshold often hides the smell behind worse names and leaves the real shape untouched. Find true responsibility boundaries.

If responsibilities are tangled you may need to first *inline* methods to see the whole picture before redistributing. Think of this when reducing line count seems particularly hard — stepping backwards often opens up better possibilities.

A concrete technique: write what the method does in one short sentence. Refactor until the code reads as close to that sentence as possible. If you cannot say what it does in one sentence, it almost certainly has more than one responsibility.

FixedDelayRetry.java:55

── unused-import (1 issue) ──

An import nothing in the module uses is noise the reader has to disprove: it makes the module look like it depends on something it does not, and it hides real signals — a leftover from code you deleted, a symbol you meant to call and forgot, or a re-export that belongs somewhere explicit.

Delete it. Don't comment it out or alias it to silence the warning — that keeps the lie. Two cases need more than deletion: if the name was imported purely for a side effect at import time, that side-effect-on-import is the smell — make the effect an explicit call. If the module is a package's public surface deliberately re-exporting the name, say so where the language makes re-exports explicit (an `__all__` entry, a barrel export) rather than leaning on an unused import to hold it.

Done right, the import list names exactly what the code below it uses — nothing to prove, nothing to explain.

RetryableOperation.java:1
