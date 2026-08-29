import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    private long fakeTime;
    
    @BeforeEach
    void setup() {
        fakeTime = 0;
        rateLimiter = new RateLimiter(() -> fakeTime);
    }
    
    @Test
    void allowsRequestsWithinLimit() {
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest(clientId));
        }
    }
    
    @Test
    void blocksRequestsExceedingLimit() {
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest(clientId);
        }
        
        assertFalse(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    void isolatesLimitPerClient() {
        assertTrue(rateLimiter.allowRequest("192.168.1.1"));
        assertTrue(rateLimiter.allowRequest("192.168.1.2"));
    }
    
    @Test
    void allowsNewRequestsAfterTimeWindow() {
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest(clientId);
        }
        assertFalse(rateLimiter.allowRequest(clientId));
        
        fakeTime += 61_000;
        
        assertTrue(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    void allowsPartialRefillBeforeWindowExpires() {
        String clientId = "192.168.1.1";
        
        rateLimiter.allowRequest(clientId);
        fakeTime += 30_000;
        rateLimiter.allowRequest(clientId);
        
        fakeTime += 30_500;
        
        assertTrue(rateLimiter.allowRequest(clientId));
    }
}
