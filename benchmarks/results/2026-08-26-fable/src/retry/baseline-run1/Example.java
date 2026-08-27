public class Example {
    public static void main(String[] args) throws InterruptedException {
        Retry retry = new Retry(3, java.time.Duration.ofSeconds(2));

        String result = retry.execute(() -> fetchFromFlakyService("participant_001"));
        System.out.println(result);

        retry.execute(() -> sendNotification("participant_001"));
    }

    static String fetchFromFlakyService(String id) throws Exception {
        // stand-in for a network call that may fail transiently
        return "data for " + id;
    }

    static void sendNotification(String id) throws Exception {
        // stand-in for a side-effecting call
    }
}
