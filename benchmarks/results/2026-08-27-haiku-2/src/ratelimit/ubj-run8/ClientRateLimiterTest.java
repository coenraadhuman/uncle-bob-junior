import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientRateLimiterTest {
  @Test
  void allowsFirstFiveRequests() {
    ClientRateLimiter limiter = new ClientRateLimiter();
    String clientId = "192.168.1.1";

    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allowRequest(clientId), "Request " + (i + 1) + " should be allowed");
    }
  }

  @Test
  void deniesRequestsAfterLimit() {
    ClientRateLimiter limiter = new ClientRateLimiter();
    String clientId = "192.168.1.1";

    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }

    assertFalse(limiter.allowRequest(clientId), "Sixth request should be denied");
    assertFalse(limiter.allowRequest(clientId), "Seventh request should be denied");
  }

  @Test
  void isolatesClientsIndependently() {
    ClientRateLimiter limiter = new ClientRateLimiter();

    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client-a");
    }

    assertTrue(limiter.allowRequest("client-b"), "Other client should not be affected");
  }

  @Test
  void resetsAfterMinute() throws InterruptedException {
    ClientRateLimiter limiter = new ClientRateLimiter();
    String clientId = "192.168.1.1";

    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }
    assertFalse(limiter.allowRequest(clientId));

    Thread.sleep(61_000);

    assertTrue(limiter.allowRequest(clientId), "Should allow request after window expires");
  }
}
