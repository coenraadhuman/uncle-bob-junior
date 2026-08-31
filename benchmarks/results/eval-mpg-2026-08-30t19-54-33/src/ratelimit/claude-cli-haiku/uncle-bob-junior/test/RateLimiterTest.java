import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;

class RateLimiterTest {
    private RateLimiter limiter;
    
    @BeforeEach
    void setUp() {
        limiter = new RateLimiter();
    }
    
    @Test
    void allowsRequestsWithinLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("client1"));
        }
    }
    
    @Test
    void rejectsRequestsExceedingLimit() {
        for (int i = 0; i < 5; i++) {
            limiter.allowRequest("client1");
        }
        
        assertFalse(limiter.allowRequest("client1"));
    }
    
    @Test
    void tracksDifferentClientsIndependently() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("client1"));
            assertTrue(limiter.allowRequest("client2"));
        }
        
        assertFalse(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client2"));
    }
    
    @Test
    void handlesConcurrentRequests() throws InterruptedException {
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        
        Runnable fillLimit = () -> {
            for (int i = 0; i < 3; i++) {
                results.add(limiter.allowRequest("client1"));
            }
        };
        
        Thread t1 = new Thread(fillLimit);
        Thread t2 = new Thread(fillLimit);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        long allowed = results.stream().filter(b -> b).count();
        assertEquals(5, allowed);
    }
    
    @Test
    void returnsOldRequestsToPool() throws InterruptedException {
        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        
        Thread.sleep(10);
        
        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        
        assertFalse(limiter.allowRequest("client1"));
        
        Thread.sleep(60_010);
        
        assertTrue(limiter.allowRequest("client1"));
    }
}
