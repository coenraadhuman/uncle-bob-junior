import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    
    @Test
    void allowsRequestsWithinLimit() {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest(clientId));
        }
    }
    
    @Test
    void blocksRequestsExceedingLimit() {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            limiter.allowRequest(clientId);
        }
        assertFalse(limiter.allowRequest(clientId));
    }
    
    @Test
    void tracksRemainingRequests() {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        assertEquals(5, limiter.getRemainingRequests(clientId));
        limiter.allowRequest(clientId);
        assertEquals(4, limiter.getRemainingRequests(clientId));
        limiter.allowRequest(clientId);
        assertEquals(3, limiter.getRemainingRequests(clientId));
    }
    
    @Test
    void isolatesClientLimits() {
        RateLimiter limiter = new RateLimiter();
        
        for (int i = 0; i < 5; i++) {
            limiter.allowRequest("client-1");
        }
        
        assertTrue(limiter.allowRequest("client-2"));
    }
    
    @Test
    void resetsAfterWindow() throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        String clientId = "192.168.1.1";
        
        limiter.allowRequest(clientId);
        assertEquals(4, limiter.getRemainingRequests(clientId));
        
        Thread.sleep(61_000); // Wait for window to expire
        
        assertEquals(5, limiter.getRemainingRequests(clientId));
    }
}
