import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @Before
    public void setUp() {
        rateLimiter = new RateLimiter(3, 60_000);
    }
    
    @Test
    public void allowsRequestsWithinLimit() {
        assertTrue(rateLimiter.isAllowed("client1"));
        assertTrue(rateLimiter.isAllowed("client1"));
        assertTrue(rateLimiter.isAllowed("client1"));
    }
    
    @Test
    public void deniesRequestsExceedingLimit() {
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        assertFalse(rateLimiter.isAllowed("client1"));
    }
    
    @Test
    public void treatsDifferentClientsIndependently() {
        for (int i = 0; i < 3; i++) {
            rateLimiter.isAllowed("client1");
        }
        assertTrue(rateLimiter.isAllowed("client2"));
    }
    
    @Test
    public void allowsNewRequestsAfterWindowExpires() throws InterruptedException {
        RateLimiter quickLimiter = new RateLimiter(1, 100);
        assertTrue(quickLimiter.isAllowed("client1"));
        assertFalse(quickLimiter.isAllowed("client1"));
        Thread.sleep(101);
        assertTrue(quickLimiter.isAllowed("client1"));
    }
}
