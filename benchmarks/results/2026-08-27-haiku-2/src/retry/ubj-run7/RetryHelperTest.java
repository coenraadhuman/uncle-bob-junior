import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        
        int result = retry.execute(() -> 42);
        
        assertEquals(42, result);
    }
    
    @Test
    void successAfterRetry() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void throwsExceptionAfterMaxAttemptsExhausted() {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        String failureMessage = "Persistent failure";
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException(failureMessage);
            });
        });
        
        assertEquals(failureMessage, exception.getMessage());
        assertEquals(3, attempts.get());
    }
    
    @Test
    void waitsDelayBetweenAttempts() {
        RetryHelper retry = new RetryHelper(3, 100);
        long startTime = System.currentTimeMillis();
        
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Fail");
            });
        });
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= 200, "Expected ≥200ms for 2 delays");
    }
    
    @Test
    void supportsMultipleReturnTypes() throws Exception {
        RetryHelper retry = new RetryHelper(2, 50);
        
        String stringResult = retry.execute(() -> "hello");
        assertEquals("hello", stringResult);
        
        Integer intResult = retry.execute(() -> 123);
        assertEquals(123, intResult);
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(0, 100);
        });
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(3, -1);
        });
    }
}
