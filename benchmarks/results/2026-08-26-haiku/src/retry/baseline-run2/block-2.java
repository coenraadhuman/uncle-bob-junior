RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

// With return value
String result = retry.execute(() -> {
    return callExternalApi();
});

// Without return value
retry.executeVoid(() -> {
    saveToDatabase();
});

// With lambda capturing variables
retry.execute(() -> {
    return fetchData(userId);
});
