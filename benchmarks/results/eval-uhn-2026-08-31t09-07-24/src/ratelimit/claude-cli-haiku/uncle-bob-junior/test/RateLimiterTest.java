import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

class RateLimiterTest {
  private final RateLimiter rateLimiter = new RateLimiter();
  private final String clientId = "192.168.1.1";

  @Test
  void allowsFirstFiveRequests() {
    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimiter.isAllowed(clientId));
    }
  }

  @Test
  void blocksSixthRequest() {
    for (int i = 0; i < 5; i++) {
      rateLimiter.isAllowed(clientId);
    }
    assertFalse(rateLimiter.isAllowed(clientId));
  }

  @Test
  void allowsRequestsAfterWindowExpires() throws InterruptedException {
    for (int i = 0; i < 5; i++) {
      rateLimiter.isAllowed(clientId);
    }
    assertFalse(rateLimiter.isAllowed(clientId));
    
    TimeUnit.SECONDS.sleep(1);
    assertTrue(rateLimiter.isAllowed(clientId));
  }

  @Test
  void isolatesClientQuotas() {
    String client1 = "192.168.1.1";
    String client2 = "192.168.1.2";

    for (int i = 0; i < 5; i++) {
      rateLimiter.isAllowed(client1);
    }

    assertTrue(rateLimiter.isAllowed(client2));
  }
}
