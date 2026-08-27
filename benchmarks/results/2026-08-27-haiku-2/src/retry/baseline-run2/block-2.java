// Example 1: Operation with return value
RetryHelper retryHelper = new RetryHelper(3, 1000); // 3 attempts, 1 second delay
String result = retryHelper.execute(() -> {
    // Your operation here
    return fetchDataFromAPI();
});

// Example 2: Void operation
retryHelper.execute(() -> {
    // Your operation here
    saveToDatabase();
});

// Example 3: Using static convenience methods
String data = RetryHelper.retryOperation(5, 500, () -> {
    return callRemoteService();
});

// Example 4: With lambda exception handling
RetryHelper.retryOperation(3, 2000, () -> {
    URL url = new URL("http://api.example.com");
    // This will retry up to 3 times with 2-second delays
    return url.openConnection();
});
