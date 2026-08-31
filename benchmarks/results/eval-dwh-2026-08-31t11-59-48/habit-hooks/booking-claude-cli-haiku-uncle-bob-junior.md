── too-many-parameters (6 issues) ──

High parameter count is a sign of coupling.
Parameters that travel together across several calls are a missing abstraction.

**Find the missing abstraction:**
1. Look at the call sites and nearby functions — is there an existing class a group of these parameters belongs to? Search wider than the file that fired: values that keep appearing side by side are the entity, and it is usually one of the domain's own nouns — where that name already exists, it is the answer.
2. If not, create it — then move behaviour that uses those fields onto it.
3. If one object owns most of the parameters, it may be the natural home for this function.
4. Use it at every site it fits, not only the one that fired — a call passing three of its fields is the same concept sitting under the threshold.

Useful tip: rewrite each call site with the signature that feels natural there, and let that shape the final method. 

**AVOID**: A `{ ...everything }` bag that merely renames the list hides the coupling instead of removing it. A `FooProps` or options object named after the function that takes it is the same bag: organised by method rather than by abstraction, so the next function invents another one and the concept stays unnamed. You are done when the entity carries a domain name and no call site still passes its fields loose.

Booking.java:14
Event.java:12
EventSeatBookingEngine.java:22
EventSeatBookingEngine.java:29
EventSeatBookingEngine.java:91
Hold.java:12

── oversized-function (1 issue) ──

Functions over 12 lines almost always carry more than one responsibility, and that is the smell to chase — not the line count itself.

Analyse responsibilities first: what distinct concerns does this function handle? Ask: (1) Are these separate responsibilities that belong in different methods? (2) Should this become a class with multiple methods? (3) Can you group cohesive data into objects to reduce local variables?

Avoid mechanical extraction. Pulling out a `helperA` / `helperB` purely to satisfy the threshold often hides the smell behind worse names and leaves the real shape untouched. Find true responsibility boundaries.

If responsibilities are tangled you may need to first *inline* methods to see the whole picture before redistributing. Think of this when reducing line count seems particularly hard — stepping backwards often opens up better possibilities.

A concrete technique: write what the method does in one short sentence. Refactor until the code reads as close to that sentence as possible. If you cannot say what it does in one sentence, it almost certainly has more than one responsibility.

EventSeatBookingEngine.java:44

── unused-variable (1 issue) ──

An unused local variable is dead weight: the reader has to prove to themselves it does not matter. It usually signals one of three things — a computation whose result is never consumed (delete the computation, not just the assignment), a leftover from a refactor that moved logic elsewhere, or a value you meant to use and forgot to wire in (the real bug).

Decide which it is before deleting. If the right-hand side has side effects you still need, keep the call but drop the binding. If it was meant to be returned or passed on, finish that thread rather than silencing the warning. Suppressing it with a throwaway name hides the question instead of answering it.

EventSeatBookingEngine.java:131
