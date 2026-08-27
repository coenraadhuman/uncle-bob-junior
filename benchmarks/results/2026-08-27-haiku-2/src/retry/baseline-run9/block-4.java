RetryHelper helper = RetryHelper.builder()
    .maxAttempts(5)
    .delayMs(2000)
    .retryOn(e -> e instanceof IOException)  // only retry on IOException
    .build();

String result = helper.execute(() -> someApiCall());
