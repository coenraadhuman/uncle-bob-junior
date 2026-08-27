import java.time.Duration;

public class Example {
    public static void main(String[] args) throws InterruptedException {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        // Operation with a result
        String response = retry.execute(() -> fetchFromApi("participant_001"));
        System.out.println(response);

        // Fire-and-forget operation
        retry.execute(() -> sendNotification("participant_001"));

        // Only retry transient failures
        Retry selective = Retry.of(5, Duration.ofMillis(500),
                e -> e instanceof java.io.IOException);
        selective.execute(() -> uploadReport());
    }

    static String fetchFromApi(String id) { /* ... */ return "ok"; }
    static void sendNotification(String id) { /* ... */ }
    static void uploadReport() throws java.io.IOException { /* ... */ }
}
