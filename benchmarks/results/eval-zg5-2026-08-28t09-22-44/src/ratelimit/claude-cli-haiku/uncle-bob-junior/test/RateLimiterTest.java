class RateLimiterTest {
  private long time;
  private RateLimiter limiter;
  
  @Before
  public void setUp() {
    time = 1000;
    limiter = new RateLimiter(() -> time);
  }
  
  @Test
  public void allowsRequestsWithinLimit() {
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allowRequest("client1"));
    }
  }
  
  @Test
  public void blocksRequestsExceedingLimit() {
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client1");
    }
    assertFalse(limiter.allowRequest("client1"));
  }
  
  @Test
  public void resetsAfterWindowExpires() {
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client1");
    }
    assertFalse(limiter.allowRequest("client1"));
    
    time += 60_001;
    assertTrue(limiter.allowRequest("client1"));
  }
  
  @Test
  public void isolatesClientsPerIdentifier() {
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client1");
      assertTrue(limiter.allowRequest("client2"));
    }
  }
}
