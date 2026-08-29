import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ApiController {
    private static final RateLimiter rateLimiter = new RateLimiter(5, 60_000);

    @GetMapping("/api/data")
    public ResponseEntity<?> getData(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            return ResponseEntity
                .status(429)
                .body("{\"error\":\"Rate limit exceeded. Max 5 requests per minute.\"}");
        }

        return ResponseEntity.ok("{\"data\":\"your response here\"}");
    }
}
