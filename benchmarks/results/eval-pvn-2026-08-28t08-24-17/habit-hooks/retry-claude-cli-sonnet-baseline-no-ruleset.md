── oversized-function (1 issue) ──

Functions over 12 lines almost always carry more than one responsibility, and that is the smell to chase — not the line count itself.

Analyse responsibilities first: what distinct concerns does this function handle? Ask: (1) Are these separate responsibilities that belong in different methods? (2) Should this become a class with multiple methods? (3) Can you group cohesive data into objects to reduce local variables?

Avoid mechanical extraction. Pulling out a `helperA` / `helperB` purely to satisfy the threshold often hides the smell behind worse names and leaves the real shape untouched. Find true responsibility boundaries.

If responsibilities are tangled you may need to first *inline* methods to see the whole picture before redistributing. Think of this when reducing line count seems particularly hard — stepping backwards often opens up better possibilities.

A concrete technique: write what the method does in one short sentence. Refactor until the code reads as close to that sentence as possible. If you cannot say what it does in one sentence, it almost certainly has more than one responsibility.

RetryHelper.java:22

── high-complexity (1 issue) ──

High cyclomatic complexity means one function makes too many decisions at once. The count is the symptom; tangled responsibilities are the cause.

**Untangle the decisions:**
1. Lift guards out first — turn precondition checks into early returns so the happy path stays flat. Much of the count is preconditions wrapped around the real work.
2. Change the shape of what remains: an `if`/`else` chain switching on one value is often a lookup table or polymorphism in disguise; a nested loop is often a filter/map pipeline.
3. If the branches are genuinely separate jobs, extract one function per branch, each named for the responsibility it handles.

Useful tip: describe each branch in one sentence. Two branches with the same sentence belong together; a branch you cannot name cleanly wants its own function.

**AVOID**: merging conditions with and/or, or rewriting branches as ternaries, just to lower the score — the decisions remain, only the counter moves. You are done when a first-time reader can hold the whole function in their head.

RetryHelper.java:22
