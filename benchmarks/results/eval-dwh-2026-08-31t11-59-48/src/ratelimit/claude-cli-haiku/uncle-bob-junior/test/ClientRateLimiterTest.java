// ClientRateLimiterTest.java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientRateLimiterTest {
    private ClientRateLimiter rateLimiter;
    private long fakeTime;

    @BeforeEach
    void setUp() {
        fakeTime = 0;
        rateLimiter = new ClientRateLimiter(() -> fakeTime);
    }

    @Test
    void allowsUpToMaxRequests() {
        String clientIp = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(clientIp));
        }
    }

    @Test
    void blocksExcessRequests() {
        String clientIp = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(clientIp);
        }
        assertFalse(rateLimiter.isAllowed(clientIp));
    }

    @Test
    void isolatesPerClient() {
        assertTrue(rateLimiter.isAllowed("192.168.1.1"));
        assertTrue(rateLimiter.isAllowed("192.168.1.2"));
        for (int i = 1; i < 5; i++) {
            rateLimiter.isAllowed("192.168.1.1");
        }
        assertFalse(rateLimiter.isAllowed("192.168.1.1"));
        assertTrue(rateLimiter.isAllowed("192.168.1.2"));
    }

    @Test
    void resetsAfterWindowExpires() {
        String clientIp = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(clientIp);
        }
        assertFalse(rateLimiter.isAllowed(clientIp));
        
        fakeTime += 60_001;
        
        assertTrue(rateLimiter.isAllowed(clientIp));
    }

    @Test
    void tracksRemaining() {
        String clientIp = "192.168.1.1";
        assertEquals(5, rateLimiter.remaining(clientIp));
        rateLimiter.isAllowed(clientIp);
        assertEquals(4, rateLimiter.remaining(clientIp));
    }
}
