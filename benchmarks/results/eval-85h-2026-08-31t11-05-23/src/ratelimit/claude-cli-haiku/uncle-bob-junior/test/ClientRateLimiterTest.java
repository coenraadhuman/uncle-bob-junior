import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClientRateLimiterTest {
    private ClientRateLimiter rateLimiter;
    
    @Before
    public void setUp() {
        rateLimiter = new ClientRateLimiter(2);
    }
    
    @Test
    public void tracksClientsIndependently() {
        assertTrue(rateLimiter.allowRequest("192.168.1.1"));
        assertTrue(rateLimiter.allowRequest("192.168.1.1"));
        assertFalse(rateLimiter.allowRequest("192.168.1.1"));
        
        assertTrue(rateLimiter.allowRequest("192.168.1.2"));
        assertTrue(rateLimiter.allowRequest("192.168.1.2"));
        assertFalse(rateLimiter.allowRequest("192.168.1.2"));
    }
}
