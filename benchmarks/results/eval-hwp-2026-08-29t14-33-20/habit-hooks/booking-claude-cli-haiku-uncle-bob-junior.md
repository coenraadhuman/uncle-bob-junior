── oversized-function (1 issue) ──

Functions over 12 lines almost always carry more than one responsibility, and that is the smell to chase — not the line count itself.

Analyse responsibilities first: what distinct concerns does this function handle? Ask: (1) Are these separate responsibilities that belong in different methods? (2) Should this become a class with multiple methods? (3) Can you group cohesive data into objects to reduce local variables?

Avoid mechanical extraction. Pulling out a `helperA` / `helperB` purely to satisfy the threshold often hides the smell behind worse names and leaves the real shape untouched. Find true responsibility boundaries.

If responsibilities are tangled you may need to first *inline* methods to see the whole picture before redistributing. Think of this when reducing line count seems particularly hard — stepping backwards often opens up better possibilities.

A concrete technique: write what the method does in one short sentence. Refactor until the code reads as close to that sentence as possible. If you cannot say what it does in one sentence, it almost certainly has more than one responsibility.

BookingEngine.java:90

── unused-variable (1 issue) ──

An unused local variable is dead weight: the reader has to prove to themselves it does not matter. It usually signals one of three things — a computation whose result is never consumed (delete the computation, not just the assignment), a leftover from a refactor that moved logic elsewhere, or a value you meant to use and forgot to wire in (the real bug).

Decide which it is before deleting. If the right-hand side has side effects you still need, keep the call but drop the binding. If it was meant to be returned or passed on, finish that thread rather than silencing the warning. Suppressing it with a throwaway name hides the question instead of answering it.

BookingEngineTest.java:186

── oversized-file (1 issue) ──

Files over 200 lines accumulate unrelated concerns. The smell is poor cohesion — a file that asks the reader to hold too many ideas at once — not the raw line count.

First identify the seams: which exports, types, or helper clusters actually belong together? A long file usually splits cleanly along one of: a data type and its operations, a feature pipeline, or one concern per file.

Avoid mechanical splits. Carving the file at line 200 into `foo-1.ts` and `foo-2.ts`, or moving every private helper into a `utils.ts`, satisfies the threshold without making anything clearer — the cohesion problem just hops to a new place.

If the file's structure resists splitting, that is itself the signal: responsibilities are tangled. Look for a missing abstraction (a class, a small module with a focused interface) that would let related pieces move together as a unit.

A concrete technique: write a one-sentence description of what each emerging seam *would* be responsible for. If you cannot, you have not found the seam yet — do not split.

BookingEngineTest.java
