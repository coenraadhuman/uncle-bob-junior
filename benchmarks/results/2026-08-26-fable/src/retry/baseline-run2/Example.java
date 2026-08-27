import java.time.Duration;

public class Example {
    public static void main(String[] args) throws InterruptedException {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        // Operation that returns a value
        String result = retry.execute(() -> fetchFromApi("participant_001"));
        System.out.println(result);

        // Void operation, retrying only on a specific exception type
        Retry ioRetry = Retry.of(5, Duration.ofMillis(500), e -> e instanceof java.io.IOException);
        ioRetry.execute(() -> sendNotification("participant_001"));
    }

    static String fetchFromApi(String id) throws Exception { /* ... */ return "ok"; }
    static void sendNotification(String id) throws Exception { /* ... */ }
}
