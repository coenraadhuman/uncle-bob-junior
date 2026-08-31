import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClientRateLimiterTest {
  private ClientRateLimiter limiter;
  private long currentTime = 0;

  @Before
  public void setUp() {
    limiter = new ClientRateLimiter(3, 1, () -> currentTime);
  }

  @Test
  public void allowsRequestsUnderLimit() {
    assertTrue(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client1"));
  }

  @Test
  public void blocksRequestsOverLimit() {
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");

    assertFalse(limiter.allowRequest("client1"));
  }

  @Test
  public void isolatesClientsFromEachOther() {
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");

    assertTrue(limiter.allowRequest("client2"));
  }

  @Test
  public void allowsNewRequestsAfterWindowExpires() {
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    assertFalse(limiter.allowRequest("client1"));

    currentTime += 61_000;

    assertTrue(limiter.allowRequest("client1"));
  }
}
