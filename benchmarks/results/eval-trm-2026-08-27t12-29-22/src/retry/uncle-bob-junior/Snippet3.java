// Simple usage: retry up to 3 times with 100ms between attempts
RetryHelper retry = new RetryHelper(3, 100);

String data = retry.execute(() -> fetchFromUnstableAPI());

// With void operations, wrap in a Callable
retry.execute(() -> {
    saveToDatabase();
    return null;
});
