import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private static final int MAX_REQUESTS = 3;
    private static final long WINDOW_MILLIS = 60_000;
    
    @Test
    void allowsRequestsWithinLimit() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS);
        
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.isAllowed("client-1"));
        }
    }
    
    @Test
    void rejectsRequestsExceedingLimit() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS);
        String clientId = "client-1";
        
        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.isAllowed(clientId);
        }
        
        assertFalse(limiter.isAllowed(clientId));
    }
    
    @Test
    void isolatesLimitPerClient() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS);
        
        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.isAllowed("client-1");
        }
        
        assertTrue(limiter.isAllowed("client-2"));
    }
    
    @Test
    void allowsNewRequestsAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, 100);
        
        assertTrue(limiter.isAllowed("client-1"));
        assertFalse(limiter.isAllowed("client-1"));
        
        Thread.sleep(110);
        assertTrue(limiter.isAllowed("client-1"));
    }
}
