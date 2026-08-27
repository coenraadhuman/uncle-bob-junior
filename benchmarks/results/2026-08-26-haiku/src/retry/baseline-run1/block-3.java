// Simple usage with builder
RetryHelper retrier = new RetryBuilder()
    .maxAttempts(5)
    .delayMillis(500)
    .build();

// Returns a value
String result = retrier.execute(() -> fetchDataFromApi());

// No return value (void operation)
retrier.execute(() -> {
    saveToDatabase();
    return null;
});

// Direct instantiation
RetryHelper simpleRetrier = new RetryHelper(3, 1000);
int count = simpleRetrier.execute(() -> apiCall());
