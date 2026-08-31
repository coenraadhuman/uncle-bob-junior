import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryTest {
    
    @Test
    public void successOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 100);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    public void successAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Retry retry = new Retry(3, 50);
        String result = retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("Not yet");
            }
            return "success";
        });
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throwsExceptionAfterMaxAttempts() throws Exception {
        Retry retry = new Retry(2, 50);
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> retry.execute(() -> {
                throw new RuntimeException("Always fails");
            }));
        assertEquals("Always fails", exception.getMessage());
    }
    
    @Test
    public void respectsDelayBetweenAttempts() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        long[] timestamps = new long[3];
        Retry retry = new Retry(3, 100);
        
        retry.execute(() -> {
            int attempt = attempts.getAndIncrement();
            timestamps[attempt] = System.currentTimeMillis();
            if (attempt < 2) {
                throw new RuntimeException("Retry");
            }
            return "success";
        });
        
        assertTrue(timestamps[1] - timestamps[0] >= 100, "First delay should be >= 100ms");
        assertTrue(timestamps[2] - timestamps[1] >= 100, "Second delay should be >= 100ms");
    }
    
    @Test
    public void preservesExceptionType() throws Exception {
        Retry retry = new Retry(2, 50);
        assertThrows(IllegalArgumentException.class,
            () -> retry.execute(() -> {
                throw new IllegalArgumentException("Invalid");
            }));
    }
    
    @Test
    public void supportsGenericReturnTypes() throws Exception {
        Retry retry = new Retry(3, 50);
        Integer result = retry.execute(() -> 42);
        assertEquals(42, result.intValue());
    }
}
