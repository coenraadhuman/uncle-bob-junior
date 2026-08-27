String result = RetryHelper.retry(
        () -> callFlakyService(),
        5,
        Duration.ofSeconds(2)
);

RetryHelper.retry(
        () -> writeToFile(data),
        3,
        Duration.ofMillis(500)
);
