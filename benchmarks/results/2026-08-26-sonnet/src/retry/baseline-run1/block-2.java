String result = RetryHelper.runWithRetry(
        () -> callFlakyService(),
        3,
        Duration.ofSeconds(2)
);

RetryHelper.runWithRetry(
        () -> writeToFile(data),
        5,
        Duration.ofMillis(500)
);
