import org.junit.Test;
import static org.junit.Assert.*;

class RateLimitedHandlerTest {
  private final RateLimiter rateLimiter = new RateLimiter();
  private final RateLimitedHandler handler = new RateLimitedHandler(rateLimiter);

  @Test
  void returns200ForAllowedRequest() {
    HttpResponse response = handler.handle("192.168.1.1", "test");
    assertEquals(200, response.statusCode);
    assertEquals("OK", response.body);
  }

  @Test
  void returns429WhenRateLimited() {
    String clientIp = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      handler.handle(clientIp, "test");
    }

    HttpResponse response = handler.handle(clientIp, "test");
    assertEquals(429, response.statusCode);
    assertEquals("Too Many Requests", response.body);
  }
}
