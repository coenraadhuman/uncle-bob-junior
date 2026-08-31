import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private static final int MAX_REQUESTS = 5;
    private RateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(MAX_REQUESTS);
    }
    
    @Test
    void allowsRequestsBelowLimit() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allowRequest("client1"));
        }
    }
    
    @Test
    void deniesRequestsAboveLimit() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertFalse(rateLimiter.allowRequest("client1"));
    }
    
    @Test
    void isolatesLimitPerClient() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertTrue(rateLimiter.allowRequest("client2"));
    }
    
    @Test
    void allowsNewRequestsAfterWindowExpires() throws InterruptedException {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertFalse(rateLimiter.allowRequest("client1"));
        
        Thread.sleep(61_000);
        
        assertTrue(rateLimiter.allowRequest("client1"));
    }
    
    @Test
    void handlesThreadSafetyCorrectly() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                rateLimiter.allowRequest("client1");
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                rateLimiter.allowRequest("client1");
            }
        });
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        assertFalse(rateLimiter.allowRequest("client1"));
    }
}
