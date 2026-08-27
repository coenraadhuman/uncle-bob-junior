import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
  
  @Test
  public void allowsFirstFiveRequests() {
    RateLimiter limiter = new RateLimiter();
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.tryConsume(), "Request " + i + " should be allowed");
    }
  }
  
  @Test
  public void rejectsSixthRequest() {
    RateLimiter limiter = new RateLimiter();
    for (int i = 0; i < 5; i++) {
      limiter.tryConsume();
    }
    assertFalse(limiter.tryConsume(), "Sixth request should be rejected");
  }
  
  @Test
  public void refillsAfterWindow() throws InterruptedException {
    RateLimiter limiter = new RateLimiter();
    for (int i = 0; i < 5; i++) {
      limiter.tryConsume();
    }
    assertFalse(limiter.tryConsume());
    
    Thread.sleep(60_100);
    assertTrue(limiter.tryConsume(), "Should allow request after window refills");
  }
}
