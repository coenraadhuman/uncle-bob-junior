# Refactoring moves, by smell

The gate names the violation; this file names the move. Each smell has a
standard, mechanical refactoring — reach for the named move instead of
improvising.

| Smell                                   | Move                                                           |
|-----------------------------------------|----------------------------------------------------------------|
| Function over ten statements            | **Extract Method** at a responsibility boundary                |
| More than three parameters              | **Introduce Parameter Object** with a domain name              |
| Value object over three fields          | **Extract Class** (nested smaller types) or **Builder**        |
| Nesting past two levels                 | **Guard Clause** / **Extract Method** on the inner block       |
| Same logic twice-plus                   | **Extract Method/Class** once a third use proves the shape     |
| Switch or if-chain on a type code       | **Replace Conditional with Polymorphism** (or a map)           |
| Fields that travel together             | **Extract Class** for the clump (`Money`, not amount+currency) |
| Method living off another object's data | **Move Method** to the envied class                            |
| Mode string or magic literal            | **Replace with Enum / Named Constant**                         |
| File over ~200 lines                    | **Extract Class/Module** along a seam, never at a line count   |

## Extract Method, done right

Find the responsibility boundary, not the line count. Say what the function
does in one sentence; refactor until the code reads like that sentence. If
you cannot say it in one sentence, it has more than one responsibility.

```java
// Before: parse + convert + categorise + report in one method (4 jobs)
double process(String line) { /* 20 statements */ }

// After: the method reads as its sentence —
// "parse the line, convert to EUR, and record it under its category"
void process(String line) {
    Transaction transaction = parse(line);
    Money amount = toEur(transaction.amount());
    ledger.record(categorise(transaction), amount);
}
```

Never extract mechanically to duck the threshold: `helperA`/`helperB` hides
the smell behind worse names. And stop where extraction scatters one
operation across fragments the reader must chase — a call chain you must
follow to understand one step is one function again.

## Introduce Parameter Object / decompose a value object

Values that keep appearing side by side are a missing type, and it is
usually one of the domain's own nouns:

```java
// Before: the fields travel loose (and any record collecting all six
// would itself break the three-field cap)
Booking book(String event, String seat, String tier, int quantity, String customer, String email) { ... }

// After: nested smaller types, three fields max at every level
record Customer(String name, Email email) {}
record SeatRequest(Seat seat, PriceTier tier, int quantity) {}
Booking book(Event event, SeatRequest request, Customer customer) { ... }
```

When one aggregate genuinely needs many parts, assemble it with a builder —
never one long constructor. A `FooProps` bag named after the function that
takes it is not a domain type; you are done when the type carries a domain
name and no call site still passes its fields loose.
