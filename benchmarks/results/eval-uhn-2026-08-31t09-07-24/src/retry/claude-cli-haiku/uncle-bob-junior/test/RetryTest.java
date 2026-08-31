import org.junit.jupiter.api.Test;
import java.util.concurrent.Callable;
import static org.junit.jupiter.api.Assertions.*;

public class RetryTest {
    
    @Test
    public void succeedsOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 10);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    public void succeedsAfterRetries() throws Exception {
        Retry retry = new Retry(3, 10);
        int[] count = {0};
        
        int result = retry.execute(() -> {
            count[0]++;
            if (count[0] < 3) throw new RuntimeException("Fail");
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, count[0]);
    }
    
    @Test
    public void throwsAfterMaxAttemptsExceeded() throws Exception {
        Retry retry = new Retry(3, 10);
        int[] count = {0};
        
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                count[0]++;
                throw new RuntimeException("Always fails");
            });
        });
        
        assertEquals(3, count[0]);
    }
    
    @Test
    public void respectsDelayBetweenAttempts() throws Exception {
        Retry retry = new Retry(2, 50);
        long startTime = System.currentTimeMillis();
        
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Fail");
            });
        });
        
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 50, "Expected delay of at least 50ms, got " + elapsed);
    }
    
    @Test
    public void validatesMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, 100));
    }
    
    @Test
    public void validatesDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(3, -1));
    }
}
