// Basic usage with return value
RetryHelper retryHelper = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

String result = retryHelper.execute(() -> {
    // Your operation here
    return apiCall();
});

// With void operations
retryHelper.execute(() -> {
    database.write(data);
    return null;
});

// With specific exception handling
try {
    retryHelper.execute(() -> {
        return risky Operation();
    });
} catch (IOException | TimeoutException e) {
    // Handle final failure
}
