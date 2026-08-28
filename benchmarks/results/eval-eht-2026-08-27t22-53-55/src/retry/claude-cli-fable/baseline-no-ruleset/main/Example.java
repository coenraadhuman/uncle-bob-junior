import java.time.Duration;

public class Example {
    public static void main(String[] args) throws Exception {
        Retry retry = Retry.of(3, Duration.ofSeconds(2));

        String result = retry.execute(() -> fetchDrawResult("participant_001"));
        System.out.println(result);

        // Retry only on transient network errors:
        Retry networkRetry = Retry.of(5, Duration.ofMillis(500),
                e -> e instanceof java.io.IOException);
        networkRetry.execute(() -> uploadReport("participant_001"));
    }

    static String fetchDrawResult(String participantId) {
        // placeholder for a flaky operation
        return "ok";
    }

    static void uploadReport(String participantId) {
        // placeholder
    }
}
