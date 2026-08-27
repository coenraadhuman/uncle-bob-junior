import java.util.HashMap;
import java.util.Map;

public class RateLimitingFilterTest {
    private ClientRateLimiter limiter;

    public static void main(String[] args) {
        RateLimitingFilterTest test = new RateLimitingFilterTest();
        test.testAllowsInitialRequests();
        test.testBlocksExcessRequests();
        test.testTokenRefill();
        System.out.println("All tests passed!");
    }

    void testAllowsInitialRequests() {
        limiter = new ClientRateLimiter(10, 60);
        String clientId = "client-1";
        for (int i = 0; i < 10; i++) {
            assert limiter.allowRequest(clientId) : "Request " + i + " should be allowed";
        }
        assert !limiter.allowRequest(clientId) : "Request 11 should be blocked";
        limiter.shutdown();
    }

    void testBlocksExcessRequests() {
        limiter = new ClientRateLimiter(3, 60);
        String clientId = "client-2";
        assert limiter.allowRequest(clientId);
        assert limiter.allowRequest(clientId);
        assert limiter.allowRequest(clientId);
        assert !limiter.allowRequest(clientId) : "4th request should be blocked";
        limiter.shutdown();
    }

    void testTokenRefill() throws InterruptedException {
        limiter = new ClientRateLimiter(2, 1);
        String clientId = "client-3";
        assert limiter.allowRequest(clientId);
        assert limiter.allowRequest(clientId);
        assert !limiter.allowRequest(clientId);
        
        Thread.sleep(1100);
        assert limiter.allowRequest(clientId) : "Should allow after refill";
        limiter.shutdown();
    }
}
