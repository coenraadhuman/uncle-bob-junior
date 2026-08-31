import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }
    
    @Test
    void allowsRequestsUpToLimit() {
        String client = "192.168.1.100";
        
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allowRequest(client), 
                "Request " + (i + 1) + " should be allowed");
        }
    }
    
    @Test
    void blocksRequestsExceedingLimit() {
        String client = "192.168.1.100";
        
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest(client);
        }
        
        assertFalse(rateLimiter.allowRequest(client), 
            "Request 11 should be blocked");
    }
    
    @Test
    void isolatesClientsFromEachOther() {
        assertTrue(rateLimiter.allowRequest("client1"));
        assertTrue(rateLimiter.allowRequest("client2"));
        
        for (int i = 0; i < 9; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertFalse(rateLimiter.allowRequest("client1"));
        assertTrue(rateLimiter.allowRequest("client2"), 
            "client2 should not be affected by client1 limit");
    }
    
    @Test
    void resetsWindowAfterTimeWindow() throws InterruptedException {
        String client = "192.168.1.100";
        
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest(client);
        }
        
        assertFalse(rateLimiter.allowRequest(client));
        
        Thread.sleep(61_000);
        
        assertTrue(rateLimiter.allowRequest(client), 
            "Should allow request after window expires");
    }
}
