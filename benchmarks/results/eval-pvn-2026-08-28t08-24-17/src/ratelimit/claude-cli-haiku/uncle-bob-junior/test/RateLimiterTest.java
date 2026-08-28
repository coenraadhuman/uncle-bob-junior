import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @Before
    public void setUp() {
        rateLimiter = new RateLimiter(3, 1000);
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
    public void isolatesClientQuotas() {
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        
        assertTrue(rateLimiter.isAllowed("client2"));
    }
    
    @Test
    public void allowsRequestsAfterWindowExpires() throws InterruptedException {
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        assertFalse(rateLimiter.isAllowed("client1"));
        
        Thread.sleep(1100);
        
        assertTrue(rateLimiter.isAllowed("client1"));
    }
}
