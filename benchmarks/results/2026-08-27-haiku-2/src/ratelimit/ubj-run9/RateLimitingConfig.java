@Configuration
public class RateLimitingConfig {
  @Bean
  public RateLimiter rateLimiter() {
    return new RateLimiter(10, 60_000);
  }
}
