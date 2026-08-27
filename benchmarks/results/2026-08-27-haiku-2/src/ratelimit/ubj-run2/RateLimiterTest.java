public class RateLimiterTest {
    private TokenBucketRateLimiter limiter;

    @Before
    public void setUp() {
        limiter = new TokenBucketRateLimiter();
    }

    @Test
    public void allowsUpToCapacity() {
        for (int i = 0; i < 10; i++) {
            assertTrue("Request " + i + " should be allowed", limiter.allowRequest());
        }
    }

    @Test
    public void rejectsExcessiveRequests() {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest();
        }
        assertFalse("11th request should be rejected", limiter.allowRequest());
    }

    @Test
    public void refillsTokensOverTime() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            limiter.allowRequest();
        }
        assertFalse("Immediate refill should not occur", limiter.allowRequest());
        
        Thread.sleep(6100);  // Wait ~6 seconds for 1 token
        assertTrue("Should allow after refill period", limiter.allowRequest());
    }

    @Test
    public void handlerTracksClientsIndependently() {
        RateLimitingHandler handler = new RateLimitingHandler();
        assertTrue("Client A allowed", handler.isAllowed("192.168.1.1"));
        assertTrue("Client B allowed", handler.isAllowed("192.168.1.2"));
    }
}
