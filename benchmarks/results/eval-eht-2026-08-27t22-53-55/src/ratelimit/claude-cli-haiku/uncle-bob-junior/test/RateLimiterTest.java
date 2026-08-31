import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private static final String CLIENT = "192.168.1.1";
    
    @Test
    void allowsRequestsUnderLimit() {
        RateLimiter limiter = new RateLimiter(3, 60_000);
        
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
    }
    
    @Test
    void rejectsRequestsOverLimit() {
        RateLimiter limiter = new RateLimiter(3, 60_000);
        
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertFalse(limiter.allowRequest(CLIENT));
    }
    
    @Test
    void resetAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(2, 100); // 100ms window
        
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertFalse(limiter.allowRequest(CLIENT));
        
        Thread.sleep(110);
        assertTrue(limiter.allowRequest(CLIENT));
    }
    
    @Test
    void isolatesLimitPerClient() {
        RateLimiter limiter = new RateLimiter(2, 60_000);
        
        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));
        
        assertTrue(limiter.allowRequest("client2"));
        assertTrue(limiter.allowRequest("client2"));
        assertFalse(limiter.allowRequest("client2"));
    }
}
