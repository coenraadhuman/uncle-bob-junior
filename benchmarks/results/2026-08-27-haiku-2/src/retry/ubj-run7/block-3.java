RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1s delay

try {
    String result = retry.execute(() -> unreliableApiCall());
    System.out.println("Success: " + result);
} catch (Exception e) {
    System.err.println("Failed after all retries: " + e.getMessage());
}
