import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    
    @Test
    void operationSucceedsOnFirstAttempt() throws Exception {
        Callable<String> operation = () -> "success";
        String result = RetryHelper.retryOnException(operation, 3, 100);
        assertEquals("success", result);
    }
    
    @Test
    void operationFailsThenSucceeds() throws Exception {
        CallCounter counter = new CallCounter(1);
        String result = RetryHelper.retryOnException(counter, 3, 10);
        assertEquals("success", result);
        assertEquals(2, counter.attemptCount());
    }
    
    @Test
    void operationFailsAllAttempts() {
        CallCounter counter = new CallCounter(5);
        assertThrows(RuntimeException.class, () -> 
            RetryHelper.retryOnException(counter, 3, 10)
        );
        assertEquals(3, counter.attemptCount());
    }
    
    @Test
    void nullOperationThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            RetryHelper.retryOnException(null, 3, 100)
        );
    }
    
    @Test
    void invalidMaxAttempts() {
        Callable<String> operation = () -> "success";
        assertThrows(IllegalArgumentException.class, () -> 
            RetryHelper.retryOnException(operation, 0, 100)
        );
    }
    
    @Test
    void invalidDelay() {
        Callable<String> operation = () -> "success";
        assertThrows(IllegalArgumentException.class, () -> 
            RetryHelper.retryOnException(operation, 3, -1)
        );
    }
    
    @Test
    void delayIsRespected() throws Exception {
        CallCounter counter = new CallCounter(2);
        long startTime = System.currentTimeMillis();
        RetryHelper.retryOnException(counter, 3, 100);
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration >= 150);
    }
    
    private static class CallCounter implements Callable<String> {
        private final int failureCount;
        private int attempts = 0;
        
        CallCounter(int failureCount) {
            this.failureCount = failureCount;
        }
        
        @Override
        public String call() {
            attempts++;
            if (attempts <= failureCount) {
                throw new RuntimeException("Simulated failure");
            }
            return "success";
        }
        
        int attemptCount() {
            return attempts;
        }
    }
}
