public class Example {
    public static void main(String[] args) throws Exception {
        Retry retry = Retry.of(3, 500); // up to 3 attempts, 500 ms between them

        // Value-returning operation
        String response = retry.call(() -> fetchFromApi("participant_001"));
        System.out.println(response);

        // Void operation, retrying only on IllegalStateException
        Retry selective = Retry.of(5, 1000, e -> e instanceof IllegalStateException);
        selective.run(() -> sendNotification("participant_001"));
    }

    static String fetchFromApi(String id) { return "ok:" + id; }
    static void sendNotification(String id) { }
}
