import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryTest {
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        int result = Retry.execute(() -> 42, 3, 10);
        assertEquals(42, result);
    }
    
    @Test
    void retriesAndSucceedsEventually() throws Exception {
        int[] calls = {0};
        
        int result = Retry.execute(
            () -> {
                calls[0]++;
                if (calls[0] < 3) throw new RuntimeException("fail");
                return 99;
            },
            5,
            10
        );
        
        assertEquals(99, result);
        assertEquals(3, calls[0]);
    }
    
    @Test
    void throwsLastExceptionAfterMaxAttempts() {
        assertThrows(RuntimeException.class, () ->
            Retry.execute(
                () -> { throw new RuntimeException("always fails"); },
                3,
                10
            )
        );
    }
    
    @Test
    void executesRunnableWithRetry() throws Exception {
        int[] calls = {0};
        
        Retry.execute(
            () -> {
                calls[0]++;
                if (calls[0] == 1) throw new IOException("fail");
            },
            3,
            10
        );
        
        assertEquals(2, calls[0]);
    }
}
