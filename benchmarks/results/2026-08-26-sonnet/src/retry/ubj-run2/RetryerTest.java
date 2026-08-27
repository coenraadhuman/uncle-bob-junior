package com.plg.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultWhenFirstAttemptSucceeds() {
        Retryer retryer = new Retryer(3, NO_DELAY);

        String result = retryer.run(() -> "ok");

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilTaskEventuallySucceeds() {
        Retryer retryer = new Retryer(3, NO_DELAY);
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retryer.run(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer(2, NO_DELAY);
        AtomicInteger attempts = new AtomicInteger(0);

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
            retryer.run(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("always fails");
            }));

        assertEquals(2, attempts.get());
        assertEquals("always fails", thrown.getCause().getMessage());
    }

    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, NO_DELAY));
    }
}
