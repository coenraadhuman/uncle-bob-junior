# Tests that prove behavior

The gate forces tests to exist; this file is what good ones look like.

## Before writing code

Answer three questions first:

1. What behavior am I promising? (One sentence per behavior.)
2. What test will I write first — and what edge cases exist? (empty,
   boundary, invalid input, duplicate, too large)
3. Am I solving the real problem or a hypothetical one? (YAGNI)

## Shape: Arrange-Act-Assert

One behavior per test, three visible phases:

```java
@Test
void ordersOverOneHundredEurosGetTheTenPercentDiscount() {
    Order order = orderWorth(euros(120));          // Arrange

    Receipt receipt = processor.process(order);    // Act

    assertEquals(euros(108), receipt.preVatTotal()); // Assert
}
```

## Naming: concrete examples, not abstractions

The name states the behavior with real values, so a failure reads as a
specification breach — not "can process orders" but what, exactly, holds:

- Bad: `testProcess`, `canAddNumbers`, `worksCorrectly`
- Good: `emptyOrderIsRejected`, `addingTwoAndThreeReturnsFive`,
  `holdExpiresAfterFifteenMinutes`

## What to test

- The public behavior, never internals: a pure refactor breaks no test.
- The happy path and every edge the code branches on. A branch with no
  test is a promise nobody checked.
- Failure modes explicitly: the exception type and message the caller
  will actually act on.

## What not to do

- Mocks are a last resort for boundaries you don't own (clock, network,
  filesystem); prefer real objects and pure functions that need no setup.
- No test-only dead code, no assertion-free "coverage" tests, no sleeping
  in tests — inject the clock instead.
- Don't ship a usage-example class as a stand-in: the tests are the usage
  example.
