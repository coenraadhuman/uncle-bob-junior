import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {

    @Test
    public void allowsRequestsUnderLimit() {
        RateLimiter limiter = new RateLimiter(3, 1000);

        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
    }

    @Test
    public void deniesRequestsOverLimit() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));
    }

    @Test
    public void isolatesClientsFromEachOther() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));

        assertTrue(limiter.allowRequest("client2"));
        assertTrue(limiter.allowRequest("client2"));
    }

    @Test
    public void allowsRequestsAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, 100);

        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));

        Thread.sleep(150);
        assertTrue(limiter.allowRequest("client1"));
    }
}
