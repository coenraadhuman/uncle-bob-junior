import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
  @Test
  void allowsRequestsWithinLimit() {
    RateLimiter limiter = new RateLimiter();
    String client = "192.168.1.1";

    for (int i = 0; i < 10; i++) {
      assertTrue(limiter.allowRequest(client));
    }
  }

  @Test
  void rejectsRequestsOverLimit() {
    RateLimiter limiter = new RateLimiter();
    String client = "192.168.1.1";

    for (int i = 0; i < 10; i++) {
      limiter.allowRequest(client);
    }

    assertFalse(limiter.allowRequest(client));
  }

  @Test
  void isolatesLimitsPerClient() {
    RateLimiter limiter = new RateLimiter();

    for (int i = 0; i < 10; i++) {
      limiter.allowRequest("192.168.1.1");
    }

    assertTrue(limiter.allowRequest("192.168.1.2"));
  }

  @Test
  void resetsAfterWindow() throws InterruptedException {
    RateLimiter limiter = new RateLimiter();
    String client = "192.168.1.1";

    for (int i = 0; i < 10; i++) {
      limiter.allowRequest(client);
    }
    assertFalse(limiter.allowRequest(client));

    Thread.sleep(61_000);
    assertTrue(limiter.allowRequest(client));
  }
}
