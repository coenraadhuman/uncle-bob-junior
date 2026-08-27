RetryHelper retryHelper = new RetryHelper(3, 1000); // 3 attempts, 1-second delay

// For operations that return a value
String result = retryHelper.execute(() -> {
    return callUnstableAPI();
});

// For void operations (return null)
retryHelper.execute(() -> {
    writeToDatabase();
    return null;
});

// With lambda that may throw specific exceptions
retryHelper.execute(() -> {
    URL url = new URL("https://api.example.com/data");
    return new String(url.openStream().readAllBytes());
});
