import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
public class RateLimitedController {
    private final RateLimiter limiter = new RateLimiter();
    
    @GetMapping("/api/data")
    public ResponseEntity<?> getData(jakarta.servlet.http.HttpServletRequest request) {
        String clientId = getClientIp(request);
        
        if (!limiter.allowRequest(clientId)) {
            return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body("Rate limit exceeded. Max 5 requests per minute.");
        }
        
        return ResponseEntity
            .ok()
            .header("X-RateLimit-Remaining", String.valueOf(limiter.getRemainingRequests(clientId)))
            .body(Map.of("message", "Success"));
    }
    
    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
