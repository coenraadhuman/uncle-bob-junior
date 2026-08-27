package com.example.retry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultWhenFirstAttemptSucceeds() {
        AtomicInteger calls = new AtomicInteger();

        String result = Retry.of(3, NO_DELAY).execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        AtomicInteger calls = new AtomicInteger();

        String result = Retry.of(3, NO_DELAY).execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IOException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsRetryExceptionWithLastFailureWhenAllAttemptsFail() {
        AtomicInteger calls = new AtomicInteger();
        IOException lastFailure = new IOException("still down");

        RetryException thrown = assertThrows(RetryException.class, () ->
                Retry.of(2, NO_DELAY).execute(() -> {
                    calls.incrementAndGet();
                    throw lastFailure;
                }));

        assertSame(lastFailure, thrown.getCause());
        assertEquals(2, calls.get());
    }

    @Test
    void runsVoidOperationsWithRetries() {
        AtomicInteger calls = new AtomicInteger();

        Retry.of(2, NO_DELAY).execute(() -> {
            if (calls.incrementAndGet() < 2) {
                throw new IllegalStateException("first attempt fails");
            }
        });

        assertEquals(2, calls.get());
    }

    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> Retry.of(0, NO_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> Retry.of(3, Duration.ofMillis(-1)));
    }

    @Test
    void restoresInterruptFlagWhenWaitIsInterrupted() throws Exception {
        Thread worker = new Thread(() ->
                assertThrows(RetryException.class, () ->
                        Retry.of(2, Duration.ofSeconds(10)).execute(() -> {
                            throw new IOException("forces a wait");
                        })));

        worker.start();
        Thread.sleep(100);
        worker.interrupt();
        worker.join(Duration.ofSeconds(5).toMillis());

        assertEquals(Thread.State.TERMINATED, worker.getState());
    }
}
