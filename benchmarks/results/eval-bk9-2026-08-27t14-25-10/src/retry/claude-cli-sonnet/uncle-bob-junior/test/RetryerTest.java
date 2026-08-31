package com.postcodeloterij.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration SHORT_DELAY = Duration.ofMillis(5);

    @Test
    void returnsResultOnFirstSuccess() {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, SHORT_DELAY);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.run(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, SHORT_DELAY);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.run(() -> {
            if (calls.incrementAndGet() < MAX_ATTEMPTS) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(MAX_ATTEMPTS, calls.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, SHORT_DELAY);
        AtomicInteger calls = new AtomicInteger();
        RuntimeException failure = new RuntimeException("always fails");

        Callable<String> alwaysFails = () -> {
            calls.incrementAndGet();
            throw failure;
        };

        RetryExhaustedException thrown =
                assertThrows(RetryExhaustedException.class, () -> retryer.run(alwaysFails));

        assertEquals(MAX_ATTEMPTS, calls.get());
        assertEquals(failure, thrown.getCause());
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, SHORT_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class,
                () -> new Retryer(MAX_ATTEMPTS, Duration.ofMillis(-1)));
    }

    @Test
    void stopsAndRestoresInterruptFlagWhenInterruptedDuringWait() throws InterruptedException {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, Duration.ofSeconds(1));
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicReference<Boolean> interruptFlagInsideThread = new AtomicReference<>();

        Thread worker = new Thread(() -> {
            try {
                retryer.run(() -> {
                    throw new RuntimeException("always fails");
                });
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                interruptFlagInsideThread.set(Thread.currentThread().isInterrupted());
            }
        });

        worker.start();
        Thread.sleep(20); // let the worker reach its wait
        worker.interrupt();
        worker.join();

        assertInstanceOf(RetryInterruptedException.class, thrown.get());
        assertTrue(interruptFlagInsideThread.get());
        assertFalse(worker.isAlive());
    }
}
