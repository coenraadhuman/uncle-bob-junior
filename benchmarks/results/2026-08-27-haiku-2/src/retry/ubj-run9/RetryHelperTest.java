import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;

class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 10;
    
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    void retriesUntilSuccess() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attempts = {0};
        Integer result = retry.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 3) throw new IOException("Fail");
            return 99;
        });
        assertEquals(3, attempts[0]);
        assertEquals(99, result);
    }
    
    @Test
    void throwsAfterExhaustingRetries() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attempts = {0};
        assertThrows(IOException.class, () ->
            retry.execute(() -> {
                attempts[0]++;
                throw new IOException("Always fails");
            })
        );
        assertEquals(MAX_ATTEMPTS, attempts[0]);
    }
    
    @Test
    void voidOperationSucceeds() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] calls = {0};
        retry.execute(() -> calls[0]++);
        assertEquals(1, calls[0]);
    }
    
    @Test
    void voidOperationRetries() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attempts = {0};
        assertThrows(IOException.class, () ->
            retry.execute(() -> {
                attempts[0]++;
                throw new IOException("Always fails");
            })
        );
        assertEquals(MAX_ATTEMPTS, attempts[0]);
    }
    
    @Test
    void rejectsZeroAttempts() {
        assertThrows(IllegalArgumentException.class, () ->
            new RetryHelper(0, DELAY_MILLIS)
        );
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () ->
            new RetryHelper(MAX_ATTEMPTS, -1)
        );
    }
}
