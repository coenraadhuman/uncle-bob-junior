package com.example.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryPolicyTest {

    private static final int THREE_ATTEMPTS = 3;
    private static final Duration NO_DELAY = Duration.ZERO;

    private final RetryPolicy policy = RetryPolicy.of(THREE_ATTEMPTS, NO_DELAY);

    @Test
    void returnsResultOnFirstSuccessWithoutRetrying() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = policy.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, attempts.get());
    }

    @Test
    void retriesUntilTheOperationSucceeds() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = policy.execute(() -> {
            if (attempts.incrementAndGet() < THREE_ATTEMPTS) {
                throw new IOException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(THREE_ATTEMPTS, attempts.get());
    }

    @Test
    void throwsLastFailureAfterExhaustingAttempts() {
        AtomicInteger attempts = new AtomicInteger();
        IOException lastFailure = new IOException("attempt 3");

        IOException thrown = assertThrows(IOException.class, () -> policy.execute(() -> {
            if (attempts.incrementAndGet() == THREE_ATTEMPTS) {
                throw lastFailure;
            }
            throw new IOException("attempt " + attempts.get());
        }));

        assertSame(lastFailure, thrown);
        assertEquals(THREE_ATTEMPTS, attempts.get());
        assertEquals(1, thrown.getSuppressed().length);
    }

    @Test
    void runnableOverloadRetriesLikeCallable() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        policy.execute((RetryPolicy.ThrowingRunnable) () -> {
            if (attempts.incrementAndGet() < 2) {
                throw new IllegalStateException("not ready");
            }
        });

        assertEquals(2, attempts.get());
    }

    @Test
    void doesNotRetryAnInterruptedOperation() {
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(InterruptedException.class, () -> policy.execute(() -> {
            attempts.incrementAndGet();
            throw new InterruptedException("cancelled");
        }));

        assertEquals(1, attempts.get());
        assertEquals(true, Thread.interrupted()); // also clears the flag so other tests are unaffected
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, NO_DELAY));
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(1, Duration.ofMillis(-1)));
        assertThrows(NullPointerException.class, () -> RetryPolicy.of(1, null));
        assertThrows(NullPointerException.class, () -> policy.execute((java.util.concurrent.Callable<String>) null));
    }
}
