import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ofMillis(1);

    @Test
    void succeedsOnFirstAttempt_returnsResultWithoutRetrying() {
        AtomicInteger callCount = new AtomicInteger(0);
        Callable<String> operation = () -> {
            callCount.incrementAndGet();
            return "ok";
        };

        String result = Retryer.retry(operation, 3, NO_DELAY);

        assertEquals("ok", result);
        assertEquals(1, callCount.get());
    }

    @Test
    void succeedsAfterTransientFailures_returnsResult() {
        AtomicInteger callCount = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        };

        String result = Retryer.retry(operation, 5, NO_DELAY);

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void exhaustsAllAttempts_throwsRetryExhaustedExceptionWithLastCause() {
        AtomicInteger callCount = new AtomicInteger(0);
        RuntimeException lastFailure = new RuntimeException("final failure");
        Callable<String> alwaysFails = () -> {
            callCount.incrementAndGet();
            if (callCount.get() == 3) {
                throw lastFailure;
            }
            throw new RuntimeException("earlier failure");
        };

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> Retryer.retry(alwaysFails, 3, NO_DELAY));

        assertEquals(3, callCount.get());
        assertEquals(lastFailure, thrown.getCause());
    }

    @Test
    void invalidMaxAttempts_throwsIllegalArgumentException() {
        Callable<String> unused = () -> "unreachable";

        assertThrows(IllegalArgumentException.class, () -> Retryer.retry(unused, 0, NO_DELAY));
    }

    @Test
    void interruptedDuringWait_propagatesAsRetryInterruptedException() throws InterruptedException {
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };
        AtomicReference<Throwable> thrown = new AtomicReference<>();

        Thread worker = new Thread(() -> {
            try {
                Retryer.retry(alwaysFails, 3, Duration.ofSeconds(5));
            } catch (Throwable t) {
                thrown.set(t);
            }
        });

        worker.start();
        Thread.sleep(100);
        worker.interrupt();
        worker.join(1000);

        assertInstanceOf(RetryInterruptedException.class, thrown.get());
    }
}
