import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryTest {
    @Test
    public void succeeds_on_first_attempt() throws Exception {
        String result = Retry.execute(() -> "success", 3, 100);
        assertEquals("success", result);
    }
    
    @Test
    public void succeeds_after_multiple_retries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = Retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IOException("retry me");
            }
            return "success";
        }, 5, 50);
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    public void throws_last_exception_after_exhausting_retries() {
        IOException thrown = assertThrows(IOException.class, () -> 
            Retry.execute(() -> {
                throw new IOException("always fails");
            }, 2, 50)
        );
        assertEquals("always fails", thrown.getMessage());
    }
    
    @Test
    public void rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException.class, () -> 
            Retry.execute(() -> "ok", 0, 100)
        );
    }
    
    @Test
    public void rejects_negative_delay() {
        assertThrows(IllegalArgumentException.class, () -> 
            Retry.execute(() -> "ok", 1, -1)
        );
    }
    
    @Test
    public void uses_defaults_when_not_specified() throws Exception {
        String result = Retry.execute(() -> "success");
        assertEquals("success", result);
    }
}
