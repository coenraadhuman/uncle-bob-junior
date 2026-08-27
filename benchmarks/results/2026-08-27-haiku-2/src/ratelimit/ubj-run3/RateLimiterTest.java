import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @Before
    public void setUp() {
        rateLimiter = new RateLimiter(3); // 3 requests per window for testing
    }
    
    @Test
    public void allowsRequestsWithinLimit() {
        String clientId = "client-1";
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    public void deniesRequestsExceedingLimit() {
        String clientId = "client-1";
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
        assertTrue(rateLimiter.allowRequest(clientId));
        
        assertFalse(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    public void isolatesRateLimitsPerClient() {
        assertTrue(rateLimiter.allowRequest("client-1"));
        assertTrue(rateLimiter.allowRequest("client-2"));
        assertTrue(rateLimiter.allowRequest("client-1"));
        
        assertTrue(rateLimiter.allowRequest("client-2"));
        assertTrue(rateLimiter.allowRequest("client-2"));
        
        assertFalse(rateLimiter.allowRequest("client-1")); // client-1 at limit
        assertFalse(rateLimiter.allowRequest("client-2")); // client-2 at limit
    }
    
    @Test
    public void handlesMultipleClientsConcurrently() throws InterruptedException {
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final String clientId = "client-" + i;
            threads[i] = new Thread(() -> {
                assertTrue(rateLimiter.allowRequest(clientId));
                assertTrue(rateLimiter.allowRequest(clientId));
                assertTrue(rateLimiter.allowRequest(clientId));
                assertFalse(rateLimiter.allowRequest(clientId));
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
