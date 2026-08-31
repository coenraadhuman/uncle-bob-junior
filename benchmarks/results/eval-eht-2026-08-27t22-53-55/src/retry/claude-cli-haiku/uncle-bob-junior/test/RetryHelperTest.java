import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    
    @Test
    public void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    public void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper retry = new RetryHelper(3, 50);
        
        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("fail");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throwsAfterMaxAttemptsExhausted() {
        AtomicInteger attempts = new AtomicInteger(0);
        RetryHelper retry = new RetryHelper(3, 10);
        
        assertThrows(IOException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new IOException("always fails");
            });
        });
        
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void validatesMaxAttemptsGreaterThanZero() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
    }
    
    @Test
    public void validatesDelayNonNegative() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
