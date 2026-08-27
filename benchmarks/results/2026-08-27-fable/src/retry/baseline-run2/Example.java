import java.time.Duration;

public class Example {
    public static void main(String[] args) {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        // Operation with a result:
        String payload = retry.call(() -> fetchFromApi("participant_001"));
        System.out.println(payload);

        // Void operation, retrying only on a specific exception type:
        Retry ioRetry = Retry.of(5, Duration.ofMillis(500),
                e -> e instanceof java.io.IOException);
        ioRetry.run(() -> uploadReport("draw-results.csv"));
    }

    static String fetchFromApi(String id) throws Exception { /* ... */ return "ok"; }
    static void uploadReport(String name) throws Exception { /* ... */ }
}
