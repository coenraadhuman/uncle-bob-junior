import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class RetryTest {
    
    @Test
    public void successOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 10);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    public void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Retry retry = new Retry(3, 10);
        
        int result = retry.execute(() -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new RuntimeException("Attempt " + current + " failed");
            }
            return current;
        });
        
        assertEquals(3, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throwsAfterMaxAttempts() throws Exception {
        Retry retry = new Retry(2, 10);
        
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> { throw new RuntimeException("Always fails"); })
        );
    }
    
    @Test
    public void voidOperationRetries() throws Exception {
        AtomicInteger calls = new AtomicInteger(0);
        Retry retry = new Retry(2, 10);
        
        retry.executeVoid(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new RuntimeException("First call fails");
            }
        });
        
        assertEquals(2, calls.get());
    }
    
    @Test
    public void staticHelperWithRetry() throws Exception {
        int result = Retry.withRetry(() -> 99);
        assertEquals(99, result);
    }
    
    @Test
    public void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () ->
            new Retry(0, 100)
        );
    }
    
    @Test
    public void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () ->
            new Retry(3, -1)
        );
    }
}
