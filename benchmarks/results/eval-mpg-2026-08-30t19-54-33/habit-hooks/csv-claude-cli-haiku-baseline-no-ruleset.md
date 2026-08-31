── oversized-function (1 issue) ──

Functions over 12 lines almost always carry more than one responsibility, and that is the smell to chase — not the line count itself.

Analyse responsibilities first: what distinct concerns does this function handle? Ask: (1) Are these separate responsibilities that belong in different methods? (2) Should this become a class with multiple methods? (3) Can you group cohesive data into objects to reduce local variables?

Avoid mechanical extraction. Pulling out a `helperA` / `helperB` purely to satisfy the threshold often hides the smell behind worse names and leaves the real shape untouched. Find true responsibility boundaries.

If responsibilities are tangled you may need to first *inline* methods to see the whole picture before redistributing. Think of this when reducing line count seems particularly hard — stepping backwards often opens up better possibilities.

A concrete technique: write what the method does in one short sentence. Refactor until the code reads as close to that sentence as possible. If you cannot say what it does in one sentence, it almost certainly has more than one responsibility.

SalesSumCalculator.java:4

── swallowed-exception (1 issue) ──

A catch-all that discards the error is hiding a failure you have not understood, not handling one you planned for. Name the specific error you expect here and why — that sentence is usually the fix.

**Make the failure visible:**
1. Catch only what you can name: the specific error type the call can really raise. If you cannot name it, you are guessing — let everything else surface where it can be seen.
2. Make the decision explicit: recover, or add context and rethrow, or — at a boundary that must stay alive (request handler, worker loop, CLI entry point) — log the full stack trace and continue. Logging and returning a default is a real option when it is a decision, not a reflex.

Useful tip: ask "if this fires at 3am, will anyone know it happened, and know why?" If not, it is still a swallow, however it is dressed up. If you are unsure whether a catch is a decision or a reflex, check with a human.

**AVOID**: narrowing the type or adding a suppression comment just to quiet the checker while the error is still discarded.

SalesSumCalculator.java:27
