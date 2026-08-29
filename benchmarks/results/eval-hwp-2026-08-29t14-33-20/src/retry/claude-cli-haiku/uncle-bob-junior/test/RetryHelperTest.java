import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    
    @Test
    public void succeedsOnFirstAttempt() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = RetryHelper.executeWithRetry(
            () -> {
                callCount.incrementAndGet();
                return "success";
            }
        );
        
        assertEquals("success", result);
        assertEquals(1, callCount.get());
    }
    
    @Test
    public void retriesAndSucceedsOnThirdAttempt() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        
        Integer result = RetryHelper.executeWithRetry(
            () -> {
                callCount.incrementAndGet();
                if (callCount.get() < 3) {
                    throw new RuntimeException("not yet");
                }
                return 42;
            },
            3,
            10
        );
        
        assertEquals(42, result);
        assertEquals(3, callCount.get());
    }
    
    @Test
    public void throwsExceptionAfterMaxAttemptsExceeded() {
        assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(
                () -> { throw new RuntimeException("permanent failure"); },
                2,
                10
            );
        });
    }
    
    @Test
    public void validatesMaxAttemptsGreaterThanZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            RetryHelper.executeWithRetry(() -> "ok", 0, 100);
        });
    }
    
    @Test
    public void usesDefaultParametersWhenNotSpecified() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        
        String result = RetryHelper.executeWithRetry(
            () -> {
                callCount.incrementAndGet();
                return "done";
            }
        );
        
        assertEquals("done", result);
        assertEquals(1, callCount.get());
    }
}
