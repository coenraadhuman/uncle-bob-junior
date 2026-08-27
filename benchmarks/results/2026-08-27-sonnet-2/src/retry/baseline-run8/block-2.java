String result = RetryHelper.runWithRetry(
        () -> callFlakyService(),
        3,
        500,
        TimeUnit.MILLISECONDS
);

RetryHelper.runWithRetry(
        () -> writeToFile(data),
        5,
        1,
        TimeUnit.SECONDS
);
