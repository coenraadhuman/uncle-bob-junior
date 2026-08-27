RetryHelper retrier = new RetryHelper(5, 2000); // 5 attempts, 2-second delay

String response = retrier.execute(() -> {
    return fetchFromUnreliableService();
});

// Or with the default constructor:
new RetryHelper().execute(() -> {
    databaseOperation();
    return null;
});
