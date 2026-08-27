// Example 1: Operation that returns a value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

String result = retry.execute(() -> {
    // Your operation here
    return callUnstableService();
});

// Example 2: Operation with no return value
retry.executeVoid(() -> {
    // Your operation here
    saveToDatabase();
});

// Example 3: With specific exception handling
try {
    retry.execute(() -> {
        if (Math.random() < 0.7) {
            throw new IOException("Service unavailable");
        }
        return "Success";
    });
} catch (Exception e) {
    System.err.println("Failed after retries: " + e.getMessage());
}
