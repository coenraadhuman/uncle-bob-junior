# Generated sources: with vs without uncle-bob-junior

Every fenced code block from each run, exactly as the model wrote it.
Metrics and medians live in the clean-code report next to this file.

## email

### email · run 1 · baseline

````java
import java.util.regex.Pattern;

public class EmailValidator {
    
    // RFC 5322 simplified regex pattern for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    /**
     * Validates an email address using regex pattern matching
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
````

````java
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {
    
    /**
     * Validates an email address using Jakarta Mail library
     * More compliant with RFC 5321/5322 standards
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
````

### email · run 1 · uncle-bob-junior

````java
// Simple regex-based validation
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
public class EmailValidator {
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_LENGTH = 64;
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        if (email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        if (parts[0].length() > MAX_LOCAL_LENGTH || parts[0].startsWith(".") || parts[0].endsWith(".")) {
            return false;
        }
        
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidationExample {
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
````

### email · run 2 · baseline

````java
// Simple regex-based approach
public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
}
````

````java
public static boolean isValidEmailStrict(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    String emailRegex = 
        "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    return email.matches(emailRegex);
}
````

````java
import org.apache.commons.validator.routines.EmailValidator;

public static boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
}
````

### email · run 2 · uncle-bob-junior

````java
public class EmailValidator {
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    
    public static boolean isValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
}
````

````java
import java.net.URI;
import java.net.URISyntaxException;

public class EmailValidator {
    private static final String EMAIL_SCHEME = "mailto";
    
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        try {
            URI uri = new URI(EMAIL_SCHEME + ":" + email);
            String localPart = uri.getSchemeSpecificPart();
            return !localPart.isEmpty() && 
                   email.contains("@") && 
                   hasValidDomain(email);
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    private static boolean hasValidDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 1 || atIndex == email.length() - 1) {
            return false;
        }
        
        String domain = email.substring(atIndex + 1);
        return domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".");
    }
}
````

````java
import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidation {
    private static final EmailValidator VALIDATOR = EmailValidator.getInstance();
    
    public static boolean isValid(String email) {
        return VALIDATOR.isValid(email);
    }
}
````

## csv

### csv · run 1 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalysis {
    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double sum = 0.0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isFirstLine) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else if (amountColumnIndex != -1) {
                    // Parse and sum the amount values
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid number in row: " + line);
                    }
                }
            }
            
            System.out.println("Total amount: " + sum);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
````

### csv · run 1 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    private static final String CSV_FILE = "sales.csv";
    private static final int AMOUNT_COLUMN_INDEX = 1; // Assumes 'amount' is the 2nd column

    public static void main(String[] args) {
        try {
            double totalAmount = sumAmountColumn();
            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double sumAmountColumn() throws IOException {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return sum;
            }
            
            String[] headers = headerLine.split(",");
            int amountIndex = findColumnIndex(headers, "amount");
            
            if (amountIndex == -1) {
                throw new IOException("'amount' column not found in CSV");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (amountIndex < values.length) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount: " + values[amountIndex]);
                    }
                }
            }
        }
        
        return sum;
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

### csv · run 2 · baseline

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int amountColumnIndex = -1;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                if (lineNumber == 0) {
                    // Parse header row to find 'amount' column index
                    String[] headers = line.split(",");
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
                } else {
                    // Parse data rows and sum the amount column
                    String[] values = line.split(",");
                    if (amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Invalid number on line " + (lineNumber + 1));
                        }
                    }
                }
                lineNumber++;
            }
            
            System.out.println("Sum of amount column: " + total);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
````

````bash
javac SalesAnalyzer.java
java SalesAnalyzer
````

### csv · run 2 · uncle-bob-junior

````java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    public static void main(String[] args) {
        try {
            double total = calculateSalesTotal("sales.csv");
            System.out.println("Total Amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double calculateSalesTotal(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }

            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                throw new IOException("Column 'amount' not found in CSV");
            }

            double total = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                total += parseAmount(line, amountColumnIndex);
            }

            return total;
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

    private static double parseAmount(String line, int columnIndex) {
        String[] fields = line.split(",");
        if (columnIndex >= fields.length) {
            return 0;
        }
        try {
            return Double.parseDouble(fields[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
````

## retry

### retry · run 1 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
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
                if (attempt < maxAttempts) {
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
RetryHelper retryHelper = new RetryHelper(3, 1000); // 3 attempts, 1-second delay

// For operations that return a value
String result = retryHelper.execute(() -> {
    return callUnstableAPI();
});

// For void operations (return null)
retryHelper.execute(() -> {
    writeToDatabase();
    return null;
});

// With lambda that may throw specific exceptions
retryHelper.execute(() -> {
    URL url = new URL("https://api.example.com/data");
    return new String(url.openStream().readAllBytes());
});
````

### retry · run 1 · uncle-bob-junior

````java
public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLISECONDS = 1000;
    
    private final int maxAttempts;
    private final long delayMilliseconds;
    
    public RetryHelper(int maxAttempts, long delayMilliseconds) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMilliseconds < 0) {
            throw new IllegalArgumentException("delayMilliseconds cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMilliseconds = delayMilliseconds;
    }
    
    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception exception) {
                lastException = exception;
                if (shouldRetry(attempt)) {
                    Thread.sleep(delayMilliseconds);
                }
            }
        }
        
        throw lastException;
    }
    
    private boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }
    
    public static <T> T executeWithDefaults(Operation<T> operation) throws Exception {
        return new RetryHelper(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLISECONDS).execute(operation);
    }
    
    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts, long delayMilliseconds) throws Exception {
        return new RetryHelper(maxAttempts, delayMilliseconds).execute(operation);
    }
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
}
````

````java
// Static convenience: defaults (3 attempts, 1000ms delay)
String data = RetryHelper.executeWithDefaults(() -> apiClient.fetchData());

// Static convenience: custom config
String result = RetryHelper.executeWithRetry(
    () -> apiClient.fetchData(),
    5,
    2000
);

// Instance for multiple operations with same retry policy
RetryHelper retryHelper = new RetryHelper(3, 1000);
String response1 = retryHelper.execute(() -> apiClient.fetchEndpoint1());
String response2 = retryHelper.execute(() -> apiClient.fetchEndpoint2());

// Void operations: return null from the lambda
retryHelper.execute(() -> {
    database.saveRecord(record);
    return null;
});
````

### retry · run 2 · baseline

````java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    private RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public static RetryHelper withAttempts(int maxAttempts) {
        return new RetryHelper(maxAttempts, 1000);
    }

    public static RetryHelper withAttemptsAndDelay(int maxAttempts, long delayMillis) {
        return new RetryHelper(maxAttempts, delayMillis);
    }

    public <T> T execute(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }

    public void execute(CheckedRunnable operation) throws Exception {
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
        throw lastException;
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}
````

````java
import java.util.function.Supplier;

public class Example {
    public static void main(String[] args) throws Exception {
        // Retry with return value (3 attempts, 1 second delay)
        RetryHelper retrier = RetryHelper.withAttempts(3);
        
        String result = retrier.execute(() -> {
            System.out.println("Attempting operation...");
            if (Math.random() > 0.7) {
                return "Success!";
            }
            throw new RuntimeException("Operation failed");
        });
        System.out.println(result);

        // Retry void operation (5 attempts, 500ms delay)
        RetryHelper retrier2 = RetryHelper.withAttemptsAndDelay(5, 500);
        
        retrier2.execute(() -> {
            System.out.println("Performing action...");
            if (Math.random() > 0.6) {
                throw new IOException("Network error");
            }
        });
    }
}
````

### retry · run 2 · uncle-bob-junior

````java
import java.util.concurrent.Callable;

public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1000;
    
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryHelper() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }
    
    public RetryHelper(int maxAttempts, long delayMs) {
        validateConfig(maxAttempts, delayMs);
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    public void executeVoid(RunnableWithException operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    private void validateConfig(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
    }
    
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
````

````java
// With return value (3 attempts, 1 second delay)
RetryHelper retryHelper = new RetryHelper();
String result = retryHelper.execute(() -> fetchDataFromApi());

// Void operation
retryHelper.executeVoid(() -> sendRequest());

// Custom configuration (5 attempts, 500ms delay)
RetryHelper customRetry = new RetryHelper(5, 500);
String data = customRetry.execute(() -> callUnreliableService());
````

## ratelimit

### ratelimit · run 1 · baseline

````java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static class TokenBucket {
        private final long maxTokens;
        private final long refillRate; // tokens per millisecond
        private double tokens;
        private long lastRefillTime;

        TokenBucket(long maxTokens, long refillIntervalMs) {
            this.maxTokens = maxTokens;
            this.refillRate = maxTokens / (double) refillIntervalMs;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(int tokensNeeded) {
            refill();
            if (tokens >= tokensNeeded) {
                tokens -= tokensNeeded;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            tokens = Math.min(maxTokens, tokens + timePassed * refillRate);
            lastRefillTime = now;
        }
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long maxRequests;
    private final long windowMs;
    private final ScheduledExecutorService cleanupExecutor;

    public RateLimiter(long maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.cleanupExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        // Clean up old entries periodically
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, windowMs, windowMs, TimeUnit.MILLISECONDS);
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
            k -> new TokenBucket(maxRequests, windowMs));
        return bucket.tryConsume(1);
    }

    private void cleanup() {
        // Remove entries that haven't been used (optional, keeps memory bounded)
        // In production, you might want a more sophisticated eviction policy
    }

    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpServer {
    private static final RateLimiter rateLimiter = new RateLimiter(10, 60_000); // 10 requests per 60 seconds

    static class RateLimitedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                // Rate limit exceeded
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, 0); // 429 Too Many Requests
                exchange.close();
                return;
            }

            // Handle the actual request
            String response = "Hello, " + clientIp + "!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on port 8080");
    }
}
````

### ratelimit · run 1 · uncle-bob-junior

````java
import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long WINDOW_SIZE_MS = 60_000;
    
    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public ClientRateLimiter() {
        scheduler.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_SIZE_MS) {
            timestamps.removeFirst();
        }
        
        if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> 
            entry.getValue().isEmpty() || entry.getValue().peekLast() < now - WINDOW_SIZE_MS
        );
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}
````

````java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final ClientRateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHttpHandler(HttpHandler delegate, ClientRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "Rate limit exceeded: max 20 requests per minute.".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        delegate.handle(exchange);
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        ClientRateLimiter rateLimiter = new ClientRateLimiter();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", new RateLimitedHttpHandler(
            exchange -> {
                byte[] response = "Hello!".getBytes();
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            },
            rateLimiter
        ));
        
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            rateLimiter.shutdown();
            server.stop(0);
        }));
    }
}
````

### ratelimit · run 2 · baseline

````java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {
    private final int requestsPerMinute;
    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientQuota quota = quotas.compute(clientId, (key, existing) -> {
            if (existing == null) {
                existing = new ClientQuota();
            }
            existing.cleanup(now);
            return existing;
        });

        synchronized (quota) {
            // Remove requests older than 1 minute
            quota.requests.removeIf(timestamp -> now - timestamp > 60_000);

            if (quota.requests.size() < requestsPerMinute) {
                quota.requests.add(now);
                return true;
            }
            return false;
        }
    }

    private static class ClientQuota {
        final java.util.LinkedList<Long> requests = new java.util.LinkedList<>();

        void cleanup(long now) {
            if (!requests.isEmpty() && now - requests.getFirst() > 120_000) {
                // Clean up if oldest request is older than 2 minutes
                requests.clear();
            }
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitedHttpHandler(HttpHandler delegate, int requestsPerMinute) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = getClientId(exchange);

        if (!rateLimiter.allowRequest(clientId)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.close();
            return;
        }

        delegate.handle(exchange);
    }

    private String getClientId(HttpExchange exchange) {
        // Try to get forwarded IP first (for proxied requests)
        String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        // Fall back to remote address
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
````

````java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Create your main handler
        HttpHandler myHandler = exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("OK".getBytes());
            exchange.close();
        };

        // Wrap with rate limiting: 10 requests per minute per client
        server.createContext("/api", new RateLimitedHttpHandler(myHandler, 10));

        server.setExecutor(null);
        server.start();
        System.out.println("Server listening on port 8080");
    }
}
````

### ratelimit · run 2 · uncle-bob-junior

````java
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60_000;
    
    private final int requestsPerMinute;
    private final ConcurrentHashMap<String, TokenBucket> buckets;
    
    public RateLimiter() {
        this(DEFAULT_REQUESTS_PER_MINUTE);
    }
    
    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.buckets = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(
            clientId,
            k -> new TokenBucket(requestsPerMinute)
        );
        return bucket.tryConsume();
    }
    
    private static class TokenBucket {
        private final int capacity;
        private double tokens;
        private long lastRefillTime;
        
        TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        synchronized boolean tryConsume() {
            refillTokens();
            
            if (tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }
        
        private void refillTokens() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            double tokensToAdd = (timePassed / (double) MINUTE_IN_MILLIS) * capacity;
            
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class RateLimitingHttpHandler implements HttpHandler {
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 10;
    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final String RETRY_AFTER_SECONDS = "60";
    
    private final RateLimiter rateLimiter;
    private final HttpHandler nextHandler;
    
    public RateLimitingHttpHandler(HttpHandler nextHandler) {
        this(nextHandler, DEFAULT_REQUESTS_PER_MINUTE);
    }
    
    public RateLimitingHttpHandler(HttpHandler nextHandler, int requestsPerMinute) {
        this.nextHandler = nextHandler;
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }
        
        nextHandler.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
    
    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", RETRY_AFTER_SECONDS);
        
        String message = "Rate limit exceeded. Maximum " + DEFAULT_REQUESTS_PER_MINUTE + 
                         " requests per minute.";
        byte[] responseBytes = message.getBytes();
        
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
````

````java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class RateLimitedServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        HttpHandler helloHandler = exchange -> {
            String response = "Hello, World!";
            byte[] bytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        };
        
        server.createContext("/", new RateLimitingHttpHandler(helloHandler, 10));
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running on http://localhost:8080");
        System.out.println("Rate limit: 10 requests per minute per client");
    }
}
````

## order

### order · run 1 · baseline

````java
import java.util.ArrayList;
import java.util.List;

class LineItem {
    String description;
    double price;
    int quantity;

    LineItem(String description, double price, int quantity) {
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    double getSubtotal() {
        return price * quantity;
    }
}

class Order {
    private List<LineItem> items;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    Order() {
        this.items = new ArrayList<>();
    }

    void addLineItem(String description, double price, int quantity) {
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and quantity must be non-negative");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        items.add(new LineItem(description, price, quantity));
    }

    String generateReceipt() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot generate receipt for empty order");
        }

        double preVatTotal = 0;
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");

        for (LineItem item : items) {
            double subtotal = item.getSubtotal();
            preVatTotal += subtotal;
            receipt.append(String.format("%-20s %5d x €%7.2f = €%8.2f\n",
                    item.description, item.quantity, item.price, subtotal));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:           €%8.2f\n", preVatTotal));

        double discount = 0;
        if (preVatTotal > DISCOUNT_THRESHOLD) {
            discount = preVatTotal * DISCOUNT_RATE;
            receipt.append(String.format("Discount (10%%):     -€%8.2f\n", discount));
        }

        double afterDiscount = preVatTotal - discount;
        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        receipt.append(String.format("VAT (21%%):          €%8.2f\n", vat));
        receipt.append("-----------------------------\n");
        receipt.append(String.format("Total:              €%8.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem("Laptop", 899.99, 1);
        order.addLineItem("Mouse", 29.99, 2);
        order.addLineItem("Cable", 12.50, 1);

        System.out.println(order.generateReceipt());

        // Example 2: Order below discount threshold
        Order order2 = new Order();
        order2.addLineItem("Book", 19.99, 2);
        order2.addLineItem("Pen", 5.00, 3);

        System.out.println(order2.generateReceipt());
    }
}
````

### order · run 1 · uncle-bob-junior

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LineItem {
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    public LineItem(String productName, int quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public void validate() {
        if (productName == null || productName.isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }
    
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
    
    public String getProductName() {
        return productName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
````

````java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    
    private final List<LineItem> items;
    
    public Order(List<LineItem> items) {
        validateItems(items);
        this.items = items;
    }
    
    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        items.forEach(LineItem::validate);
    }
    
    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        
        return formatReceipt(subtotal, discount, discountedSubtotal, vat, total);
    }
    
    private BigDecimal calculateSubtotal() {
        return items.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    private String formatReceipt(BigDecimal subtotal, BigDecimal discount, 
                                  BigDecimal discountedSubtotal, BigDecimal vat, 
                                  BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append("Items:\n");
        
        items.forEach(item -> 
            receipt.append(String.format("  %s x%d @ €%.2f = €%.2f\n", 
                item.getProductName(), 
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()))
        );
        
        receipt.append(String.format("\nSubtotal:  €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%) -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):  €%.2f\n", vat));
        receipt.append(String.format("TOTAL:  €%.2f\n", total));
        
        return receipt.toString();
    }
}
````

````java
// Example usage
public class OrderExample {
    public static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Widget A", 2, new BigDecimal("45.50")),
            new LineItem("Widget B", 1, new BigDecimal("30.00")),
            new LineItem("Service Fee", 1, new BigDecimal("35.75"))
        );
        
        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
````

### order · run 2 · baseline

````java
import java.util.*;

class OrderItem {
    String productName;
    double unitPrice;
    int quantity;

    OrderItem(String productName, double unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}

class Order {
    private List<OrderItem> items;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    Order(List<OrderItem> items) {
        this.items = items;
    }

    void validate() throws IllegalArgumentException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (OrderItem item : items) {
            if (item.productName == null || item.productName.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            if (item.unitPrice <= 0) {
                throw new IllegalArgumentException("Unit price must be positive: " + item.productName);
            }
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.productName);
            }
        }
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (OrderItem item : items) {
            subtotal += item.unitPrice * item.quantity;
        }
        return subtotal;
    }

    private double calculateDiscountAmount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0;
    }

    String generateReceipt() {
        validate();

        double subtotal = calculateSubtotal();
        double discountAmount = calculateDiscountAmount(subtotal);
        double preVatTotal = subtotal - discountAmount;
        double vatAmount = preVatTotal * VAT_RATE;
        double total = preVatTotal + vatAmount;

        StringBuilder receipt = new StringBuilder();
        receipt.append("===== RECEIPT =====\n");

        for (OrderItem item : items) {
            double itemTotal = item.unitPrice * item.quantity;
            receipt.append(String.format("%s x%d @ €%.2f: €%.2f\n",
                    item.productName, item.quantity, item.unitPrice, itemTotal));
        }

        receipt.append("\nSubtotal: €").append(String.format("%.2f", subtotal)).append("\n");

        if (discountAmount > 0) {
            receipt.append("Discount (10%): -€").append(String.format("%.2f", discountAmount)).append("\n");
        }

        receipt.append("Pre-VAT Total: €").append(String.format("%.2f", preVatTotal)).append("\n");
        receipt.append("VAT (21%): €").append(String.format("%.2f", vatAmount)).append("\n");
        receipt.append("Total: €").append(String.format("%.2f", total)).append("\n");
        receipt.append("==================\n");

        return receipt.toString();
    }

    public static void main(String[] args) {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("Widget", 25.00, 2),
                new OrderItem("Gadget", 30.00, 2),
                new OrderItem("Tool", 15.00, 1)
        );

        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
````

````
===== RECEIPT =====
Widget x2 @ €25.00: €50.00
Gadget x2 @ €30.00: €60.00
Tool x1 @ €15.00: €15.00

Subtotal: €125.00
Discount (10%): -€12.50
Pre-VAT Total: €112.50
VAT (21%): €23.63
Total: €136.13
==================
````

### order · run 2 · uncle-bob-junior

````java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public static class LineItem {
        private final String description;
        private final int quantity;
        private final double unitPrice;

        public LineItem(String description, int quantity, double unitPrice) {
            validateLineItem(description, quantity, unitPrice);
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        private void validateLineItem(String description, int quantity, double unitPrice) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Line item description cannot be empty");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Line item quantity must be positive");
            }
            if (unitPrice < 0) {
                throw new IllegalArgumentException("Line item unit price cannot be negative");
            }
        }

        public double lineTotal() {
            return quantity * unitPrice;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }
    }

    public static class Order {
        private final List<LineItem> items;

        public Order() {
            this.items = new ArrayList<>();
        }

        public void addLineItem(LineItem item) {
            Objects.requireNonNull(item, "Line item cannot be null");
            items.add(item);
        }

        public double subtotal() {
            return items.stream().mapToDouble(LineItem::lineTotal).sum();
        }

        public double discountAmount() {
            double subtotal = subtotal();
            return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        }

        public double subtotalAfterDiscount() {
            return subtotal() - discountAmount();
        }

        public double vatAmount() {
            return subtotalAfterDiscount() * VAT_RATE;
        }

        public double total() {
            return subtotalAfterDiscount() + vatAmount();
        }

        public String generateReceipt() {
            if (items.isEmpty()) {
                throw new IllegalStateException("Cannot generate receipt for empty order");
            }

            StringBuilder receipt = new StringBuilder();
            receipt.append("========== RECEIPT ==========\n");

            for (LineItem item : items) {
                receipt.append(String.format("%-25s %3d x €%.2f = €%.2f\n",
                    item.getDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.lineTotal()));
            }

            receipt.append("-----------------------------\n");
            receipt.append(String.format("Subtotal: €%.2f\n", subtotal()));

            if (discountAmount() > 0) {
                receipt.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
            }

            receipt.append(String.format("Subtotal after discount: €%.2f\n", subtotalAfterDiscount()));
            receipt.append(String.format("VAT (21%%): €%.2f\n", vatAmount()));
            receipt.append("-----------------------------\n");
            receipt.append(String.format("Total: €%.2f\n", total()));
            receipt.append("=============================\n");

            return receipt.toString();
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Widget", 2, 45.00));
        order.addLineItem(new LineItem("Gadget", 1, 20.00));
        order.addLineItem(new LineItem("Doohickey", 3, 15.00));

        System.out.println(order.generateReceipt());
    }
}
````
