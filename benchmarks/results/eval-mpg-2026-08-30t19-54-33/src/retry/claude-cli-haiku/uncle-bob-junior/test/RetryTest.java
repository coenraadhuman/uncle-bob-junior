import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryTest {
    @Test
    void successOnFirstAttempt() throws Exception {
        Callable<Integer> operation = () -> 42;
        RetryConfig config = new RetryConfig(3, 100);
        
        Integer result = Retry.execute(operation, config);
        
        assertEquals(42, result);
    }

    @Test
    void successAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<Integer> operation = () -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("Fail");
            }
            return 99;
        };
        RetryConfig config = new RetryConfig(5, 10);
        
        Integer result = Retry.execute(operation, config);
        
        assertEquals(99, result);
        assertEquals(3, attempts.get());
    }

    @Test
    void failsAfterMaxAttempts() {
        Callable<Integer> operation = () -> {
            throw new IOException("Always fails");
        };
        RetryConfig config = new RetryConfig(3, 10);
        
        assertThrows(IOException.class, () -> Retry.execute(operation, config));
    }

    @Test
    void appliesDelayBetweenAttempts() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<Integer> operation = () -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("Fail");
            }
            return 1;
        };
        long delay = 50;
        RetryConfig config = new RetryConfig(3, delay);
        
        long start = System.currentTimeMillis();
        Retry.execute(operation, config);
        long elapsed = System.currentTimeMillis() - start;
        
        assertTrue(elapsed >= delay * 2);
    }

    @Test
    void preservesOriginalException() {
        Callable<Integer> operation = () -> {
            throw new IllegalArgumentException("Bad input");
        };
        RetryConfig config = new RetryConfig(2, 10);
        
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> Retry.execute(operation, config)
        );
        assertEquals("Bad input", ex.getMessage());
    }

    @Test
    void worksWithVoidOperations() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);
        Callable<Void> operation = () -> {
            executed.set(true);
            return null;
        };
        RetryConfig config = new RetryConfig(3, 10);
        
        Retry.execute(operation, config);
        
        assertTrue(executed.get());
    }
}
