import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retryHelper = new RetryHelper(3, 10);
        
        String result = retryHelper.execute(() -> "success");
        
        assertEquals("success", result);
    }
    
    @Test
    void successAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper retryHelper = new RetryHelper(3, 10);
        
        String result = retryHelper.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("failed");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void failsAfterMaxAttempts() throws Exception {
        RetryHelper retryHelper = new RetryHelper(2, 10);
        IOException originalException = new IOException("failed");
        
        IOException thrown = assertThrows(IOException.class, () -> {
            retryHelper.execute(() -> {
                throw originalException;
            });
        });
        
        assertEquals(originalException, thrown);
    }
    
    @Test
    void waitsBeforeRetry() throws Exception {
        RetryHelper retryHelper = new RetryHelper(2, 50);
        long startTime = System.currentTimeMillis();
        
        assertThrows(Exception.class, () -> {
            retryHelper.execute(() -> {
                throw new Exception("failed");
            });
        });
        
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 50, "Should have waited at least 50ms");
    }
    
    @Test
    void validatesMaxAttemptsOnConstruction() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(0, 10);
        });
    }
}
