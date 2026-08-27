import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private final RateLimiter limiter = new RateLimiter();

    @Test
    public void allowsRequestsUnderLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.allowRequest("client1"));
        }
    }

    @Test
    public void blocksRequestsOverLimit() {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest("client1");
        }
        assertFalse(limiter.allowRequest("client1"));
    }

    @Test
    public void isolatesDifferentClients() {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest("client1");
        }
        assertTrue(limiter.allowRequest("client2"));
    }

    @Test
    public void resetsAfterTimeWindow() throws InterruptedException {
        limiter.allowRequest("client1");
        Thread.sleep(61000); // Wait just over 1 minute
        assertTrue(limiter.allowRequest("client1"));
    }
}
