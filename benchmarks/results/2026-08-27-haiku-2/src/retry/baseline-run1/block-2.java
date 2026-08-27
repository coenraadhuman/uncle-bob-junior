// For operations that return a value
RetryHelper retry = new RetryHelper(3, 1000);

String result = retry.execute(() -> {
    // Some operation that might fail
    return fetchDataFromApi();
});

// For operations without return value
retry.execute(() -> {
    // Some operation that might fail
    saveToDatabase();
});
