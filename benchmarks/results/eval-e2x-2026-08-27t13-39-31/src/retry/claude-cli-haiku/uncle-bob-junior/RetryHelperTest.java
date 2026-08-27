import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    void succeeds_afterFailures() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        int[] attemptCount = {0};
        
        String result = retry.execute(() -> {
            attemptCount[0]++;
            if (attemptCount[0] < 3) {
                throw new RuntimeException("attempt " + attemptCount[0]);
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attemptCount[0]);
    }
    
    @Test
    void throwsAfterMaxAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(2, 10);
        
        Exception thrown = assertThrows(RuntimeException.class, () -> 
            retry.execute(() -> {
                throw new RuntimeException("persistent failure");
            })
        );
        
        assertEquals("persistent failure", thrown.getMessage());
    }
    
    @Test
    void throwsOnInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RetryHelper(0, 100)
        );
    }
    
    @Test
    void throwsOnNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> 
            new RetryHelper(3, -1)
        );
    }
    
    @Test
    void supportsGenericReturnTypes() throws Exception {
        RetryHelper retry = new RetryHelper(1, 10);
        
        Integer number = retry.execute(() -> 42);
        assertEquals(42, number);
        
        Object[] array = retry.execute(() -> new Object[]{1, 2, 3});
        assertEquals(3, array.length);
    }
}
