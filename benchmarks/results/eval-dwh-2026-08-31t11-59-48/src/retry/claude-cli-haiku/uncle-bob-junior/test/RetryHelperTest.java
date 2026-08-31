import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    private static final long DELAY_MS = 50;
    
    @Test
    public void shouldExecuteOperationSuccessfullyOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, DELAY_MS);
        
        AtomicInteger attempts = new AtomicInteger(0);
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(1, attempts.get());
    }
    
    @Test
    public void shouldRetryAndSucceedAfterFailures() throws Exception {
        RetryHelper retry = new RetryHelper(3, DELAY_MS);
        
        AtomicInteger attempts = new AtomicInteger(0);
        int result = retry.execute(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("Transient failure");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void shouldExhaustRetriesAndThrowLastException() throws Exception {
        RetryHelper retry = new RetryHelper(3, DELAY_MS);
        
        AtomicInteger attempts = new AtomicInteger(0);
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Persistent failure");
            });
        });
        
        assertEquals("Persistent failure", thrown.getMessage());
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void shouldRejectInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, DELAY_MS));
    }
    
    @Test
    public void shouldRejectNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
    
    @Test
    public void shouldProvideDefaultsFactory() throws Exception {
        RetryHelper retry = RetryHelper.withDefaults();
        
        int result = retry.execute(() -> 99);
        assertEquals(99, result);
    }
}
