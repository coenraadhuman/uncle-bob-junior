import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClientRateLimiterTest {
    private ClientRateLimiter rateLimiter;
    private static final int LIMIT = 3;
    private static final long WINDOW = 100;
    
    @BeforeEach
    public void setUp() {
        rateLimiter = new ClientRateLimiter(LIMIT, WINDOW);
    }
    
    @AfterEach
    public void tearDown() {
        rateLimiter.shutdown();
    }
    
    @Test
    public void allowsRequestsWithinLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allowRequest("client1"));
        }
    }
    
    @Test
    public void rejectsRequestsExceedingLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allowRequest("client1"));
        }
        assertFalse(rateLimiter.allowRequest("client1"));
    }
    
    @Test
    public void allowsRequestsAfterWindowExpires() throws InterruptedException {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allowRequest("client1"));
        }
        assertFalse(rateLimiter.allowRequest("client1"));
        
        Thread.sleep(WINDOW + 10);
        
        assertTrue(rateLimiter.allowRequest("client1"));
    }
    
    @Test
    public void isolatesPerClient() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(rateLimiter.allowRequest("client1"));
            assertTrue(rateLimiter.allowRequest("client2"));
        }
    }
}
