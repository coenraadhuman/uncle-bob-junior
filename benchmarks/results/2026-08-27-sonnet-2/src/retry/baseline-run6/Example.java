import java.time.Duration;

public class Example {
    public static void main(String[] args) {
        String result = RetryHelper.runWithRetry(3, Duration.ofSeconds(2), () -> {
            return callFlakyService();
        });
        System.out.println(result);
    }

    private static String callFlakyService() throws Exception {
        // simulate a call that might throw
        return "ok";
    }
}
