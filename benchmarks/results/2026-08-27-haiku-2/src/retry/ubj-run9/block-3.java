// Retry a callable operation
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1s delay

String data = retry.execute(() -> 
    fetchDataFromApi("https://api.example.com/data")
);

// Retry a void operation
retry.execute(() -> 
    database.executeUpdate("UPDATE users SET active = true")
);

// Exception propagates after max attempts exhausted
try {
    retry.execute(() -> riskyOperation());
} catch (IOException e) {
    logger.error("Failed after retries", e);
}
