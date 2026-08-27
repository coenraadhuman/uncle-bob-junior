import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private static final String CLIENT_ID = "192.168.1.1";

    @Test
    public void allowsRequestsUnderLimit() {
        RateLimiter limiter = new RateLimiter(5);

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest(CLIENT_ID));
        }
    }

    @Test
    public void deniesRequestsAboveLimit() {
        RateLimiter limiter = new RateLimiter(3);

        for (int i = 0; i < 3; i++) {
            limiter.allowRequest(CLIENT_ID);
        }

        assertFalse(limiter.allowRequest(CLIENT_ID));
    }

    @Test
    public void tracksRemainingRequests() {
        RateLimiter limiter = new RateLimiter(5);

        limiter.allowRequest(CLIENT_ID);
        limiter.allowRequest(CLIENT_ID);

        assertEquals(3, limiter.remainingRequests(CLIENT_ID));
    }

    @Test
    public void resetsClientAfterReset() {
        RateLimiter limiter = new RateLimiter(2);

        limiter.allowRequest(CLIENT_ID);
        limiter.allowRequest(CLIENT_ID);
        limiter.reset(CLIENT_ID);

        assertEquals(2, limiter.remainingRequests(CLIENT_ID));
    }

    @Test
    public void distinguishesDifferentClients() {
        RateLimiter limiter = new RateLimiter(2);

        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        assertTrue(limiter.allowRequest("client2"));
    }
}
