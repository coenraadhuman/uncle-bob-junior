import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTests {

    @Test
    void executesOperationSuccessfully() throws Exception {
        int result = RetryHelper.executeWithRetry(() -> 42);
        assertEquals(42, result);
    }

    @Test
    void retriesOnFailureThenSucceeds() throws Exception {
        int[] attempts = {0};
        int result = RetryHelper.executeWithRetry(() -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw new RuntimeException("Fail");
            }
            return 100;
        }, 3, 10);

        assertEquals(3, attempts[0]);
        assertEquals(100, result);
    }

    @Test
    void throwsAfterMaxRetriesExceeded() {
        assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(() -> {
                throw new RuntimeException("Always fails");
            }, 2, 10);
        });
    }

    @Test
    void appliesDelayBetweenRetries() throws Exception {
        int[] attempts = {0};
        long startTime = System.currentTimeMillis();

        assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(() -> {
                attempts[0]++;
                throw new RuntimeException("Fail");
            }, 3, 50);
        });

        long elapsedMs = System.currentTimeMillis() - startTime;
        assertEquals(3, attempts[0]);
        assertTrue(elapsedMs >= 80, "Should apply delay between retries");
    }

    @Test
    void rejectsZeroMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> {
            RetryHelper.executeWithRetry(() -> 1, 0, 1000);
        });
    }

    @Test
    void preservesLastExceptionOnFailure() {
        RuntimeException expectedException = new RuntimeException("Specific error");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(() -> {
                throw expectedException;
            }, 2, 10);
        });
        assertSame(expectedException, thrown);
    }

    @Test
    void worksWithCheckedExceptions() throws Exception {
        int[] attempts = {0};
        int result = RetryHelper.executeWithRetry(() -> {
            attempts[0]++;
            if (attempts[0] == 1) {
                throw new InterruptedException("Transient failure");
            }
            return 99;
        }, 3, 10);

        assertEquals(2, attempts[0]);
        assertEquals(99, result);
    }

    @Test
    void returnsNullIfOperationReturnsNull() throws Exception {
        String result = RetryHelper.executeWithRetry(() -> null);
        assertNull(result);
    }
}
