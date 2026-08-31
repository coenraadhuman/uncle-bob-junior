// RateLimitConfig.java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return new RateLimiter();
    }
    
    @Bean
    public WebMvcConfigurer webMvcConfigurer(RateLimiter rateLimiter) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new RateLimitInterceptor(rateLimiter));
            }
        };
    }
}
