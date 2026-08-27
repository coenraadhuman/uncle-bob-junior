public class Example {
    public static void main(String[] args) throws Exception {
        Retry retry = new Retry(3, 1000); // 3 attempts, 1 second delay

        // With return value
        String result = retry.execute(() -> {
            return fetchDataFromApi();
        });

        // Void operation
        retry.executeVoid(() -> {
            saveDataToDatabase();
        });
    }

    private static String fetchDataFromApi() {
        return "data";
    }

    private static void saveDataToDatabase() {
    }
}
