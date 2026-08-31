import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RateLimiterTest {
  private final RateLimiter limiter = new RateLimiter();

  @Test
  void allowsRequestsWithinLimit() {
    String clientId = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allowRequest(clientId));
    }
  }

  @Test
  void rejectsRequestsExceedingLimit() {
    String clientId = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }
    assertFalse(limiter.allowRequest(clientId));
  }

  @Test
  void allowsDifferentClientsIndependently() {
    assertTrue(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client2"));
    for (int i = 0; i < 4; i++) {
      limiter.allowRequest("client1");
    }
    assertFalse(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client2"));
  }

  @Test
  void resetsAfterWindowExpires() throws InterruptedException {
    String clientId = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }
    assertFalse(limiter.allowRequest(clientId));

    Thread.sleep(61_000);
    assertTrue(limiter.allowRequest(clientId));
  }
}
