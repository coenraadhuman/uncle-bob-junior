# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
EmailValidator.isValidEmail("user@example.com");        // true
EmailValidator.isValidEmail("invalid.email@");          // false
EmailValidator.isValidEmail(null);                      // false
````

````java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
````

### email · run 1 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public class EmailValidator {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  );

  public static boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }
}
````

````java
EmailValidator.isValidEmail("user@example.com");        // true
EmailValidator.isValidEmail("invalid.email");            // false
EmailValidator.isValidEmail("user+tag@example.co.uk");  // true
EmailValidator.isValidEmail("");                         // false
EmailValidator.isValidEmail(null);                       // false
````

### email · run 2 · baseline

````java
import java.util.regex.Pattern;

public class EmailValidator {
    // RFC 5322 simplified pattern for practical email validation
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        return pattern.matcher(email).matches();
    }
}
````

````java
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            new InternetAddress(email, true);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
````

### email · run 2 · uncle-bob-junior

````java
import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(email).matches();
    }
}
````

````java
import javax.mail.internet.InternetAddress;

public class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            new InternetAddress(email).validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.*;

public class SalesSum {
    public static void main(String[] args) {
        String fileName = "sales.csv";
        double sum = 0;
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            
            // Read and parse header
            String header = reader.readLine();
            if (header == null) {
                System.out.println("Error: File is empty");
                return;
            }
            
            String[] headers = header.split(",");
            int amountIndex = -1;
            
            // Find the 'amount' column index
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            
            if (amountIndex == -1) {
                System.out.println("Error: 'amount' column not found");
                reader.close();
                return;
            }
            
            // Sum the amount values
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
            }
            
            reader.close();
            System.out.println("Sum of amount column: " + sum);
            
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 1 · uncle-bob-junior

````java
import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("CSV file is empty");
                return;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                System.out.println("Column 'amount' not found in CSV");
                return;
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (amountColumnIndex < values.length) {
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        totalAmount += amount;
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid amount value: " + values[amountColumnIndex]);
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + totalAmount);
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
````

### csv · run 2 · baseline

````java
import java.io.*;
import java.util.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String[] headers = null;
            int amountColumnIndex = -1;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isFirstLine) {
                    headers = values;
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                    isFirstLine = false;
                } else {
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        total += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value '" + values[amountColumnIndex] + "' as a number");
                    }
                }
            }
            
            System.out.println("Total amount: " + total);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: File '" + filename + "' not found");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                if (isHeader) {
                    amountColumnIndex = findColumnIndex(fields, "amount");
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found in CSV header.");
                        return;
                    }
                    isHeader = false;
                    continue;
                }

                if (amountColumnIndex < fields.length) {
                    try {
                        total += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid number in amount column: " + fields[amountColumnIndex]);
                    }
                }
            }

            System.out.println("Total amount: " + total);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static int findColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
````

````bash
javac SalesSum.java
java SalesSum
````

## retry

### retry · run 1 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be non-negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts && delayMillis > 0) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw lastException;
    }

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
````

````java
public class RetryBuilder {
    private int maxAttempts = 3;
    private long delayMillis = 1000;

    public RetryBuilder maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RetryBuilder delayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    public RetryHelper build() {
        return new RetryHelper(maxAttempts, delayMillis);
    }
}
````

````java
// Simple usage with builder
RetryHelper retrier = new RetryBuilder()
    .maxAttempts(5)
    .delayMillis(500)
    .build();

// Returns a value
String result = retrier.execute(() -> fetchDataFromApi());

// No return value (void operation)
retrier.execute(() -> {
    saveToDatabase();
    return null;
});

// Direct instantiation
RetryHelper simpleRetrier = new RetryHelper(3, 1000);
int count = simpleRetrier.execute(() -> apiCall());
````

### retry · run 1 · uncle-bob-junior

````java
import java.util.concurrent.Callable;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw new RetryException(
            String.format("Operation failed after %d attempts", maxAttempts),
            lastException
        );
    }

    public void execute(ThrowingRunnable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw new RetryException(
            String.format("Operation failed after %d attempts", maxAttempts),
            lastException
        );
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static class RetryException extends Exception {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Builder {
        private int maxAttempts = 3;
        private long delayMillis = 1000;

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder delayMillis(long delayMillis) {
            this.delayMillis = delayMillis;
            return this;
        }

        public RetryHelper build() {
            return new RetryHelper(maxAttempts, delayMillis);
        }
    }
}
````

````java
// Using constructor directly
RetryHelper retrier = new RetryHelper(3, 1000);

// Retrying an operation that returns a value
String result = retrier.execute(() -> callExternalApi());

// Retrying a void operation
retrier.execute(() -> saveToDatabase(data));

// Using the builder pattern
RetryHelper customRetrier = new RetryHelper.Builder()
    .maxAttempts(5)
    .delayMillis(2000)
    .build();

customRetrier.execute(() -> unreliableService.call());
````

### retry · run 2 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }

    public void executeVoid(RetryableRunnable operation) throws Exception {
        execute(() -> {
            operation.execute();
            return null;
        });
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface RetryableRunnable {
        void execute() throws Exception;
    }
}
````

````java
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

// With return value
String result = retry.execute(() -> {
    return callExternalApi();
});

// Without return value
retry.executeVoid(() -> {
    saveToDatabase();
});

// With lambda capturing variables
retry.execute(() -> {
    return fetchData(userId);
});
````

### retry · run 2 · uncle-bob-junior

````java
@FunctionalInterface
public interface Operation<T> {
    T execute() throws Exception;
}

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;

    public static <T> T retry(Operation<T> operation, int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep(delayMillis);
                }
            }
        }
        
        throw new RetryExhaustedException("Failed after " + maxAttempts + " attempts", lastException);
    }

    public static <T> T retry(Operation<T> operation, int maxAttempts) {
        return retry(operation, maxAttempts, DEFAULT_DELAY_MILLIS);
    }

    public static <T> T retry(Operation<T> operation) {
        return retry(operation, DEFAULT_MAX_ATTEMPTS);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException("Retry interrupted", e);
        }
    }

    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RetryInterruptedException extends RuntimeException {
        public RetryInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
````

````java
// Default: 3 attempts, 1000ms delay
String data = RetryHelper.retry(() -> fetchFromApi());

// Custom: 5 attempts, 2000ms delay
String data = RetryHelper.retry(() -> fetchFromApi(), 5, 2000);

// Void operation
RetryHelper.retry(() -> {
    database.write(record);
    return null;
}, 3, 500);

// Handles both checked and unchecked exceptions
Integer count = RetryHelper.retry(() -> {
    return database.query("SELECT COUNT(*) FROM users");
}, 4, 1000);
````

## ratelimit

### ratelimit · run 1 · baseline

````java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class ClientQuota {
        private final Queue<Long> requestTimestamps;
        private final int maxRequests;
        private final long windowMillis;

        ClientQuota(int maxRequests, long windowMillis) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
            this.requestTimestamps = new ConcurrentLinkedQueue<>();
        }

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            // Remove timestamps outside the window
            while (!requestTimestamps.isEmpty() && 
                   requestTimestamps.peek() < now - windowMillis) {
                requestTimestamps.poll();
            }

            // Check if under limit
            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.offer(now);
                return true;
            }
            return false;
        }
    }

    private final ConcurrentHashMap<String, ClientQuota> quotas;
    private final int maxRequests;
    private final long windowMillis;
    private final Timer cleanupTimer;

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.quotas = new ConcurrentHashMap<>();
        
        // Cleanup stale entries every 5 minutes
        this.cleanupTimer = new Timer(true);
        this.cleanupTimer.scheduleAtFixedRate(this::cleanup, 300000, 300000);
    }

    public boolean isAllowed(String clientId) {
        return quotas.computeIfAbsent(clientId, k -> 
            new ClientQuota(maxRequests, windowMillis)
        ).allowRequest();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> {
            Queue<Long> timestamps = entry.getValue().requestTimestamps;
            return timestamps.isEmpty() || 
                   (timestamps.peek() < now - windowMillis);
        });
    }

    public void shutdown() {
        cleanupTimer.cancel();
    }
}
````

````java
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private RateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) throws ServletException {
        // 5 requests per 60 seconds per client
        rateLimiter = new RateLimiter(5, 60000);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        String clientId = getClientIp((HttpServletRequest) request);
        
        if (!rateLimiter.isAllowed(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rateLimiter.shutdown();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

````java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(5, 60000);

    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        String clientId = getClientIp(request);
        
        if (!rateLimiter.isAllowed(clientId)) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> clientRequestTimes;
    private final int maxRequests;
    private final long windowMillis;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.clientRequestTimes = new ConcurrentHashMap<>();
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }
    
    public RateLimiter() {
        this(MAX_REQUESTS_PER_MINUTE, MINUTE_IN_MILLIS);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequestTimes.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        removeExpiredRequests(timestamps, now);
        
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
    
    private void removeExpiredRequests(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequestTimes.getOrDefault(clientId, new ConcurrentLinkedDeque<>());
        
        removeExpiredRequests(timestamps, now);
        return Math.max(0, maxRequests - timestamps.size());
    }
}
````

````java
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        rateLimiter = new RateLimiter();
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            int remaining = rateLimiter.getRemainingRequests(clientIp);
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded\", \"remaining\": " + remaining + "}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    @Override
    public void destroy() {
    }
}
````

````java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = getClientIpAddress(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            return false;
        }
        
        response.addHeader("X-RateLimit-Remaining", String.valueOf(rateLimiter.getRemainingRequests(clientIp)));
        return true;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
````

### ratelimit · run 2 · baseline

````java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    private static final long CLEANUP_INTERVAL_MS = 300_000; // 5 minutes
    
    private final Map<String, LinkedList<Long>> clientRequests = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long lastCleanup = System.currentTimeMillis();
    
    /**
     * Check if a client is allowed to make a request.
     * @param clientIp The client's IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String clientIp) {
        lock.writeLock().lock();
        try {
            cleanupIfNeeded();
            
            long now = System.currentTimeMillis();
            LinkedList<Long> requests = clientRequests.computeIfAbsent(clientIp, k -> new LinkedList<>());
            
            // Remove timestamps outside the window
            while (!requests.isEmpty() && requests.getFirst() < now - WINDOW_SIZE_MS) {
                requests.removeFirst();
            }
            
            // Check if limit exceeded
            if (requests.size() >= REQUESTS_PER_MINUTE) {
                return false;
            }
            
            // Record this request
            requests.addLast(now);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get remaining requests for a client in the current window.
     */
    public int getRemainingRequests(String clientIp) {
        lock.readLock().lock();
        try {
            long now = System.currentTimeMillis();
            LinkedList<Long> requests = clientRequests.get(clientIp);
            if (requests == null) {
                return REQUESTS_PER_MINUTE;
            }
            
            long validCount = requests.stream()
                .filter(ts -> ts >= now - WINDOW_SIZE_MS)
                .count();
            
            return Math.max(0, REQUESTS_PER_MINUTE - (int) validCount);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            clientRequests.entrySet().removeIf(entry -> 
                entry.getValue().isEmpty() || 
                entry.getValue().getLast() < now - WINDOW_SIZE_MS
            );
            lastCleanup = now;
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/**
 * HTTP handler with rate limiting applied.
 */
public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            // Return 429 Too Many Requests
            byte[] response = "Rate limit exceeded. Max 5 requests per minute.".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        // Add rate limit info to response headers
        int remaining = rateLimiter.getRemainingRequests(clientIp);
        exchange.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
        exchange.getResponseHeaders().set("X-RateLimit-Limit", "5");
        
        // Delegate to the actual handler
        delegate.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        // Try X-Forwarded-For header first (for proxies)
        String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        
        // Fall back to direct connection IP
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
````

````java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

// Example usage:
public class ExampleServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimiter rateLimiter = new RateLimiter();
        
        // Wrap your handler with rate limiting
        HttpHandler originalHandler = exchange -> {
            exchange.sendResponseHeaders(200, 5);
            exchange.getResponseBody().write("Hello".getBytes());
            exchange.close();
        };
        
        server.createContext("/", new RateLimitedHandler(originalHandler, rateLimiter));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class RateLimiter {
  private static final int MAX_REQUESTS = 10;
  private static final long WINDOW_MINUTES = 1;
  private static final long WINDOW_MILLIS = WINDOW_MINUTES * 60 * 1000;
  
  private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
  
  public boolean allowRequest(String clientId) {
    long now = System.currentTimeMillis();
    Deque<Long> timestamps = requestTimestamps.computeIfAbsent(
        clientId,
        k -> new LinkedBlockingDeque<>()
    );
    
    removeExpiredRequests(timestamps, now);
    
    if (timestamps.size() < MAX_REQUESTS) {
      timestamps.addLast(now);
      return true;
    }
    
    return false;
  }
  
  private void removeExpiredRequests(Deque<Long> timestamps, long now) {
    while (!timestamps.isEmpty() && timestamps.getFirst() <= now - WINDOW_MILLIS) {
      timestamps.removeFirst();
    }
  }
}
````

````java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
  private final RateLimiter rateLimiter = new RateLimiter();
  private static final int TOO_MANY_REQUESTS = 429;
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    String clientIp = getClientIp(httpRequest);
    
    if (rateLimiter.allowRequest(clientIp)) {
      chain.doFilter(request, response);
    } else {
      httpResponse.setStatus(TOO_MANY_REQUESTS);
      httpResponse.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
    }
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
  
  @Override
  public void init(FilterConfig config) {}
  
  @Override
  public void destroy() {}
}
````

````java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
  private final RateLimiter rateLimiter = new RateLimiter();
  private static final int TOO_MANY_REQUESTS = 429;
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    
    String clientIp = getClientIp(request);
    
    if (!rateLimiter.allowRequest(clientIp)) {
      response.setStatus(TOO_MANY_REQUESTS);
      response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
      return false;
    }
    
    return true;
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
````

````java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final RateLimitInterceptor rateLimitInterceptor;
  
  public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
    this.rateLimitInterceptor = rateLimitInterceptor;
  }
  
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor);
  }
}
````

## order

### order · run 1 · baseline

````java
import java.util.*;
import java.text.DecimalFormat;

class LineItem {
    private String productName;
    private int quantity;
    private double unitPrice;

    public LineItem(String productName, int quantity, double unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return quantity * unitPrice;
    }
}

class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final DecimalFormat currencyFormat = new DecimalFormat("€#,##0.00");

    public Order(List<LineItem> lineItems) {
        this.lineItems = lineItems != null ? new ArrayList<>(lineItems) : new ArrayList<>();
    }

    public void validateLineItems() {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (int i = 0; i < lineItems.size(); i++) {
            LineItem item = lineItems.get(i);
            if (item == null) {
                throw new IllegalArgumentException("Line item " + i + " is null");
            }
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Line item " + i + " has invalid product name");
            }
            if (item.getQuantity() < 0) {
                throw new IllegalArgumentException("Line item " + i + " has negative quantity");
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Line item " + i + " has negative unit price");
            }
        }
    }

    public double getSubtotal() {
        return lineItems.stream()
                .mapToDouble(LineItem::getLineTotal)
                .sum();
    }

    public double getDiscount() {
        double subtotal = getSubtotal();
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }

    public double getDiscountedSubtotal() {
        return getSubtotal() - getDiscount();
    }

    public double getVat() {
        return getDiscountedSubtotal() * VAT_RATE;
    }

    public double getTotal() {
        return getDiscountedSubtotal() + getVat();
    }

    public String produceReceipt() {
        validateLineItems();
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("═════════════════════════════════\n");
        receipt.append("               RECEIPT\n");
        receipt.append("═════════════════════════════════\n\n");
        
        receipt.append("Item                    Qty    Price\n");
        receipt.append("─────────────────────────────────\n");
        for (LineItem item : lineItems) {
            String name = item.getProductName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            receipt.append(String.format("%-20s %3d  %s\n", 
                    name, 
                    item.getQuantity(), 
                    currencyFormat.format(item.getLineTotal())));
        }
        
        receipt.append("─────────────────────────────────\n");
        receipt.append(String.format("Subtotal:                    %s\n", 
                currencyFormat.format(getSubtotal())));
        
        double discount = getDiscount();
        if (discount > 0) {
            receipt.append(String.format("Discount (10%):             -%s\n", 
                    currencyFormat.format(discount)));
        }
        
        receipt.append(String.format("Subtotal after discount:     %s\n", 
                currencyFormat.format(getDiscountedSubtotal())));
        receipt.append(String.format("VAT (21%):                   %s\n", 
                currencyFormat.format(getVat())));
        
        receipt.append("═════════════════════════════════\n");
        receipt.append(String.format("TOTAL:                       %s\n", 
                currencyFormat.format(getTotal())));
        receipt.append("═════════════════════════════════\n");
        
        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = Arrays.asList(
                new LineItem("Laptop", 1, 80.00),
                new LineItem("Mouse", 2, 15.00),
                new LineItem("Keyboard", 1, 45.00)
        );
        
        Order order = new Order(items);
        System.out.println(order.produceReceipt());
        
        // Example with discount threshold exceeded
        System.out.println("\n\nSecond order (with discount):\n");
        List<LineItem> items2 = Arrays.asList(
                new LineItem("Monitor", 1, 300.00),
                new LineItem("HDMI Cable", 2, 10.00)
        );
        
        Order order2 = new Order(items2);
        System.out.println(order2.produceReceipt());
    }
}
````

### order · run 1 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description);
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void validate() {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    String getDescription() {
        return description;
    }

    int getQuantity() {
        return quantity;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int SCALE = 2;

    private final List<LineItem> lineItems = new ArrayList<>();

    void addLineItem(LineItem item) {
        item.validate();
        lineItems.add(item);
    }

    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateTaxableAmount(BigDecimal subtotal, BigDecimal discount) {
        return subtotal.subtract(discount).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVat(BigDecimal taxableAmount) {
        return taxableAmount.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotal(BigDecimal taxableAmount, BigDecimal vat) {
        return taxableAmount.add(vat).setScale(SCALE, RoundingMode.HALF_UP);
    }

    String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal taxableAmount = calculateTaxableAmount(subtotal, discount);
        BigDecimal vat = calculateVat(taxableAmount);
        BigDecimal total = calculateTotal(taxableAmount, vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append("Items:\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("  %s x%d @ €%.2f = €%.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal()));
        }

        receipt.append(String.format("Subtotal:       €%.2f\n", subtotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%): -€%.2f\n", discount));
        }

        receipt.append(String.format("Subtotal:       €%.2f\n", taxableAmount));
        receipt.append(String.format("VAT (21%%):      €%.2f\n", vat));
        receipt.append(String.format("Total:          €%.2f\n", total));

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Widget A", 3, new BigDecimal("25.00")));
        order.addLineItem(new LineItem("Widget B", 2, new BigDecimal("20.00")));
        order.addLineItem(new LineItem("Widget C", 1, new BigDecimal("15.00")));

        System.out.println(order.generateReceipt());
    }
}
````

### order · run 2 · baseline

````java
import java.util.*;
import java.text.DecimalFormat;

public class OrderProcessor {
    static class LineItem {
        String productName;
        int quantity;
        double unitPrice;

        public LineItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        double getSubtotal() {
            return quantity * unitPrice;
        }
    }

    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget A", 5, 15.00));
        items.add(new LineItem("Widget B", 3, 20.00));
        items.add(new LineItem("Service Fee", 1, 50.00));

        String receipt = processOrder(items);
        System.out.println(receipt);
    }

    public static String processOrder(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        double preVatTotal = calculatePreVatTotal(lineItems);
        double discount = preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0.0;
        double discountedTotal = preVatTotal - discount;
        double vat = discountedTotal * VAT_RATE;
        double finalTotal = discountedTotal + vat;

        return generateReceipt(lineItems, preVatTotal, discount, discountedTotal, vat, finalTotal);
    }

    private static void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : lineItems) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for: " + item.productName);
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative for: " + item.productName);
            }
        }
    }

    private static double calculatePreVatTotal(List<LineItem> lineItems) {
        return lineItems.stream().mapToDouble(LineItem::getSubtotal).sum();
    }

    private static String generateReceipt(List<LineItem> items, double preVatTotal, 
                                          double discount, double discountedTotal, 
                                          double vat, double finalTotal) {
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuilder receipt = new StringBuilder();

        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append("\nLine Items:\n");
        for (LineItem item : items) {
            receipt.append(String.format("  %-20s | Qty: %3d | Unit: €%8s | Subtotal: €%8s\n",
                    item.productName, item.quantity, df.format(item.unitPrice), 
                    df.format(item.getSubtotal())));
        }

        receipt.append("\n-------------------\n");
        receipt.append(String.format("Pre-VAT Total:       €%8s\n", df.format(preVatTotal)));
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):     -€%8s\n", df.format(discount)));
        }
        receipt.append(String.format("Subtotal:            €%8s\n", df.format(discountedTotal)));
        receipt.append(String.format("VAT (21%%):           €%8s\n", df.format(vat)));
        receipt.append("-------------------\n");
        receipt.append(String.format("TOTAL:               €%8s\n", df.format(finalTotal)));

        return receipt.toString();
    }
}
````

### order · run 2 · uncle-bob-junior

````java
public class LineItem {
    private final String name;
    private final int quantity;
    private final double unitPrice;
    
    public LineItem(String name, int quantity, double unitPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive for " + name);
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative for " + name);
        }
    }
    
    public double getLineTotal() {
        return quantity * unitPrice;
    }
    
    public String getName() {
        return name;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
}
````

````java
import java.util.List;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final double DISCOUNT_RATE = 0.10;
    
    private final List<LineItem> lineItems;
    
    public OrderProcessor(List<LineItem> lineItems) {
        validateAllLineItems(lineItems);
        this.lineItems = lineItems;
    }
    
    private void validateAllLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            item.validate();
        }
    }
    
    public String generateReceipt() {
        double subtotal = calculateSubtotal();
        double discountAmount = calculateDiscount(subtotal);
        double subtotalAfterDiscount = subtotal - discountAmount;
        double vatAmount = subtotalAfterDiscount * VAT_RATE;
        double total = subtotalAfterDiscount + vatAmount;
        
        return formatReceipt(subtotal, discountAmount, vatAmount, total);
    }
    
    private double calculateSubtotal() {
        return lineItems.stream()
            .mapToDouble(LineItem::getLineTotal)
            .sum();
    }
    
    private double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }
    
    private String formatReceipt(double subtotal, double discount, double vat, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("════════════════════════════════════\n");
        receipt.append("           ORDER RECEIPT\n");
        receipt.append("════════════════════════════════════\n\n");
        
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-25s %3d × €%7.2f = €%8.2f\n",
                item.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
            ));
        }
        
        receipt.append("\n────────────────────────────────────\n");
        receipt.append(String.format("%-30s €%10.2f\n", "Subtotal:", subtotal));
        
        if (discount > 0) {
            receipt.append(String.format("%-30s €%10.2f\n", "Discount (10%):", -discount));
        }
        
        double subtotalAfterDiscount = subtotal - discount;
        receipt.append(String.format("%-30s €%10.2f\n", "VAT (21%):", vat));
        receipt.append("════════════════════════════════════\n");
        receipt.append(String.format("%-30s €%10.2f\n", "TOTAL:", total));
        receipt.append("════════════════════════════════════\n");
        
        return receipt.toString();
    }
}
````

````java
public class Main {
    public static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Laptop", 1, 799.99),
            new LineItem("Mouse", 2, 24.99),
            new LineItem("USB-C Cable", 3, 12.50)
        );
        
        OrderProcessor order = new OrderProcessor(items);
        System.out.println(order.generateReceipt());
    }
}
````
