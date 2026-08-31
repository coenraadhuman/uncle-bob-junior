import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

public class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 50;
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        String result = helper.execute(() -> "success");
        assertThat(result).isEqualTo("success");
    }
    
    @Test
    void succeedsAfterRetries() throws Exception {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = helper.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("Retry me");
            }
            return "success";
        });
        
        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }
    
    @Test
    void failsAfterMaxAttempts() throws Exception {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);
        
        assertThatThrownBy(() -> helper.execute(() -> {
            attempts.incrementAndGet();
            throw new IOException("Always fails");
        }))
        .isInstanceOf(IOException.class)
        .hasMessage("Always fails");
        
        assertThat(attempts.get()).isEqualTo(MAX_ATTEMPTS);
    }
    
    @Test
    void respectsDelayBetweenAttempts() throws Exception {
        RetryHelper helper = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        assertThatThrownBy(() -> helper.execute(() -> {
            attempts.incrementAndGet();
            throw new IOException("Fail");
        }))
        .isInstanceOf(IOException.class);
        
        long elapsed = System.currentTimeMillis() - startTime;
        assertThat(elapsed).isGreaterThanOrEqualTo(200);
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThatThrownBy(() -> new RetryHelper(0, DELAY_MILLIS))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThatThrownBy(() -> new RetryHelper(MAX_ATTEMPTS, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
