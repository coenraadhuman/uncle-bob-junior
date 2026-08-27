// Static convenience: defaults (3 attempts, 1000ms delay)
String data = RetryHelper.executeWithDefaults(() -> apiClient.fetchData());

// Static convenience: custom config
String result = RetryHelper.executeWithRetry(
    () -> apiClient.fetchData(),
    5,
    2000
);

// Instance for multiple operations with same retry policy
RetryHelper retryHelper = new RetryHelper(3, 1000);
String response1 = retryHelper.execute(() -> apiClient.fetchEndpoint1());
String response2 = retryHelper.execute(() -> apiClient.fetchEndpoint2());

// Void operations: return null from the lambda
retryHelper.execute(() -> {
    database.saveRecord(record);
    return null;
});
