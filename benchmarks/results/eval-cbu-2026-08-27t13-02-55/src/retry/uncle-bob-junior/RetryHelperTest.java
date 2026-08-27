import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 10;

    @Test
    void executeSucceedsOnFirstAttempt() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        
        String result = helper.execute(() -> "success");
        
        assertEquals("success", result);
    }

    @Test
    void executeRetriesUntilSuccess() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attemptCount = {0};
        
        String result = helper.execute(() -> {
            attemptCount[0]++;
            if (attemptCount[0] < 3) {
                throw new IOException("Temporary failure");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attemptCount[0]);
    }

    @Test
    void executeThrowsRetryExhaustedWhenAllAttemptsFail() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        
        RetryHelper.RetryExhaustedException exception = assertThrows(
            RetryHelper.RetryExhaustedException.class,
            () -> helper.execute(() -> {
                throw new IOException("Persistent failure");
            })
        );
        
        assertTrue(exception.getMessage().contains("Failed after 3 attempts"));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void executeRespectMaxAttempts() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attemptCount = {0};
        
        assertThrows(
            RetryHelper.RetryExhaustedException.class,
            () -> helper.execute(() -> {
                attemptCount[0]++;
                throw new IOException("Persistent failure");
            })
        );
        
        assertEquals(MAX_ATTEMPTS, attemptCount[0]);
    }

    @Test
    void invalidMaxAttemptsThrowsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RetryHelper(0, DELAY_MILLIS)
        );
    }

    @Test
    void negativeDelayThrowsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RetryHelper(MAX_ATTEMPTS, -1)
        );
    }

    @Test
    void interruptedSleepThrowsRetryInterruptedException() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, 1000);
        int[] attemptCount = {0};
        
        RetryHelper.RetryInterruptedException exception = assertThrows(
            RetryHelper.RetryInterruptedException.class,
            () -> helper.execute(() -> {
                if (++attemptCount[0] == 1) {
                    Thread.currentThread().interrupt();
                    throw new IOException("First attempt fails");
                }
                return "success";
            })
        );
        
        assertTrue(exception.getMessage().contains("Retry sleep interrupted"));
    }
}
