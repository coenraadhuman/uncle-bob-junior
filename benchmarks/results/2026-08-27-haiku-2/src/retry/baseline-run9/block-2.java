// With return value
String result = RetryHelper.retry(
    () -> someApiCall(),
    3,      // max attempts
    1000    // 1 second delay
);

// Void operation
RetryHelper.retryVoid(
    () -> writeToDatabase(),
    3,
    1000
);
