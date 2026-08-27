// File: RetryerTest.java
package com.plg.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

class RetryerTest {

    private static final Duration DELAY = Duration.ofMillis(50);

    @Test
    void succeedsOnFirstAttempt_returnsResultWithoutWaiting() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(3, DELAY, sleeper);
        ScriptedOperation operation = new ScriptedOperation("ok");

        String result = retryer.run(operation);

        assertEquals("ok", result);
        assertEquals(1, operation.invocationCount());
        assertTrue(sleeper.sleptDurations().isEmpty());
    }

    @Test
    void succeedsAfterTransientFailures_retriesThenReturnsResult() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(3, DELAY, sleeper);
        ScriptedOperation operation = new ScriptedOperation("ok", new IOException("first"), new IOException("second"));

        String result = retryer.run(operation);

        assertEquals("ok", result);
        assertEquals(3, operation.invocationCount());
        assertEquals(List.of(DELAY, DELAY), sleeper.sleptDurations());
    }

    @Test
    void exhaustsAllAttempts_throwsWithLastFailureAsCause() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(3, DELAY, sleeper);
        IOException finalFailure = new IOException("boom");
        AlwaysFailingOperation operation = new AlwaysFailingOperation(new IOException("ignored 1"), new IOException("ignored 2"), finalFailure);

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () -> retryer.run(operation));

        assertEquals(3, operation.invocationCount());
        assertSame(finalFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelay_onlyBetweenAttemptsNotAfterLastOne() {
        FakeSleeper sleeper = new FakeSleeper();
        Retryer retryer = new Retryer(4, DELAY, sleeper);
        AlwaysFailingOperation operation = new AlwaysFailingOperation(new IOException("a"), new IOException("b"), new IOException("c"), new IOException("d"));

        assertThrows(RetryExhaustedException.class, () -> retryer.run(operation));

        assertEquals(List.of(DELAY, DELAY, DELAY), sleeper.sleptDurations());
    }

    @Test
    void invalidMaxAttempts_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, DELAY));
    }

    @Test
    void negativeDelay_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(3, Duration.ofMillis(-1)));
    }

    @Test
    void nullDelay_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Retryer(3, null));
    }

    @Test
    void nullOperation_throwsNullPointerException() {
        Retryer retryer = new Retryer(3, DELAY, new FakeSleeper());

        assertThrows(NullPointerException.class, () -> retryer.run(null));
    }

    @Test
    void interruptedWhileWaiting_restoresInterruptStatusAndThrows() {
        Retryer.Sleeper interruptingSleeper = duration -> {
            throw new RetryInterruptedException(new InterruptedException("test"));
        };
        Retryer retryer = new Retryer(3, DELAY, interruptingSleeper);
        AlwaysFailingOperation operation = new AlwaysFailingOperation(new IOException("a"), new IOException("b"));

        assertInstanceOf(RetryInterruptedException.class,
                assertThrows(RetryInterruptedException.class, () -> retryer.run(operation)));
    }

    /** Records requested waits instead of actually sleeping, keeping tests fast. */
    private static final class FakeSleeper implements Retryer.Sleeper {
        private final List<Duration> sleptDurations = new java.util.ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            sleptDurations.add(duration);
        }

        List<Duration> sleptDurations() {
            return sleptDurations;
        }
    }

    /** Fails with the given exceptions in order, then succeeds with {@code successValue}. */
    private static final class ScriptedOperation implements RetryableOperation<String> {
        private final Queue<Exception> failuresBeforeSuccess;
        private final String successValue;
        private int invocationCount = 0;

        ScriptedOperation(String successValue, Exception... failuresBeforeSuccess) {
            this.successValue = successValue;
            this.failuresBeforeSuccess = new ArrayDeque<>(Arrays.asList(failuresBeforeSuccess));
        }

        @Override
        public String execute() throws Exception {
            invocationCount++;
            Exception nextFailure = failuresBeforeSuccess.poll();
            if (nextFailure != null) {
                throw nextFailure;
            }
            return successValue;
        }

        int invocationCount() {
            return invocationCount;
        }
    }

    /** Throws the given exceptions in order on each invocation, repeating the last one once exhausted. */
    private static final class AlwaysFailingOperation implements RetryableOperation<String> {
        private final List<Exception> failures;
        private int invocationCount = 0;

        AlwaysFailingOperation(Exception... failures) {
            this.failures = Arrays.asList(failures);
        }

        @Override
        public String execute() throws Exception {
            Exception failure = failures.get(Math.min(invocationCount, failures.size() - 1));
            invocationCount++;
            throw failure;
        }

        int invocationCount() {
            return invocationCount;
        }
    }
}
