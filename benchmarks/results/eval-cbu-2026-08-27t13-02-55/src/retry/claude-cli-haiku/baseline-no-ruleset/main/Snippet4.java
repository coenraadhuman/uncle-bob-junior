class Snippet4 {
RetryHelper retry = RetryHelper.builder()
  .maxAttempts(5)
  .delayMillis(500)
  .retryOn(IOException.class)  // Only retry on specific exception
  .build();

String result = retry.execute(() -> fetchFromAPI());

}
