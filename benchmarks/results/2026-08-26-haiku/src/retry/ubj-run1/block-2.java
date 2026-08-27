// Using constructor directly
RetryHelper retrier = new RetryHelper(3, 1000);

// Retrying an operation that returns a value
String result = retrier.execute(() -> callExternalApi());

// Retrying a void operation
retrier.execute(() -> saveToDatabase(data));

// Using the builder pattern
RetryHelper customRetrier = new RetryHelper.Builder()
    .maxAttempts(5)
    .delayMillis(2000)
    .build();

customRetrier.execute(() -> unreliableService.call());
