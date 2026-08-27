// For operations that return a value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1s delay

String result = retry.execute(() -> {
    return fetchDataFromApi();
});

// For void operations
retry.executeVoid(() -> {
    saveDataToDatabase(result);
});

// With custom exceptions
try {
    Integer count = retry.execute(() -> {
        if (Math.random() < 0.7) {
            throw new IOException("Network error");
        }
        return 42;
    });
} catch (Exception e) {
    System.err.println("Failed after retries: " + e.getMessage());
}
