String result = new RetryBuilder<>(() -> callAPI())
    .maxAttempts(5)
    .delay(500)
    .exponentialBackoff()
    .retryOn(IOException.class)
    .execute();
