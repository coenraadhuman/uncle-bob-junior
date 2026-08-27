import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class RetryHelperTest {
    
    @Test
    public void testSucceedsOnFirstAttempt() throws Exception {
        RetryHelper helper = new RetryHelper(3, 100);
        int result = helper.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    public void testSucceedsAfterRetry() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper helper = new RetryHelper(3, 100);
        
        int result = helper.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 2) {
                throw new IOException("Temporary failure");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(2, attempts.get());
    }
    
    @Test
    public void testThrowsAfterMaxAttempts() throws Exception {
        RetryHelper helper = new RetryHelper(3, 50);
        
        assertThrows(IOException.class, () -> {
            helper.execute(() -> {
                throw new IOException("Permanent failure");
            });
        });
    }
    
    @Test
    public void testVoidOperation() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        RetryHelper helper = new RetryHelper(2, 50);
        
        helper.execute(() -> {
            counter.incrementAndGet();
            return null;
        });
        
        assertEquals(1, counter.get());
    }
    
    @Test
    public void testRejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
    }
    
    @Test
    public void testRejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
