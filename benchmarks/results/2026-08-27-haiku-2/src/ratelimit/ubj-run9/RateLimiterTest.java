public class RateLimiterTest {
  private RateLimiter rateLimiter;

  @Before
  public void setUp() {
    rateLimiter = new RateLimiter(3, 1000);
  }

  @Test
  public void allowsRequestsUnderLimit() {
    for (int i = 0; i < 3; i++) {
      assertTrue(rateLimiter.allowRequest("client1"));
    }
  }

  @Test
  public void rejectsRequestsOverLimit() {
    for (int i = 0; i < 3; i++) {
      rateLimiter.allowRequest("client1");
    }
    assertFalse(rateLimiter.allowRequest("client1"));
  }

  @Test
  public void allowsAfterWindowExpires() throws InterruptedException {
    for (int i = 0; i < 3; i++) {
      rateLimiter.allowRequest("client1");
    }
    Thread.sleep(1100);
    assertTrue(rateLimiter.allowRequest("client1"));
  }

  @Test
  public void isolatesClientsIndependently() {
    for (int i = 0; i < 3; i++) {
      rateLimiter.allowRequest("client1");
    }
    assertTrue(rateLimiter.allowRequest("client2"));
  }

  @Test
  public void handlesHighConcurrency() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    AtomicInteger allowed = new AtomicInteger();
    
    for (int i = 0; i < 50; i++) {
      executor.submit(() -> {
        if (rateLimiter.allowRequest("concurrent")) {
          allowed.incrementAndGet();
        }
      });
    }
    
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
    
    assertEquals(3, allowed.get());
  }
}
