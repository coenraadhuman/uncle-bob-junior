// With return value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay
String result = retry.execute(() -> {
    return apiCall();
});

// Void operation
retry.executeVoid(() -> {
    database.connect();
});

// Lambda with network call
retry.execute(() -> {
    return httpClient.get("https://example.com");
});
