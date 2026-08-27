// Default: 3 attempts, 1000ms delay
String data = RetryHelper.retry(() -> fetchFromApi());

// Custom: 5 attempts, 2000ms delay
String data = RetryHelper.retry(() -> fetchFromApi(), 5, 2000);

// Void operation
RetryHelper.retry(() -> {
    database.write(record);
    return null;
}, 3, 500);

// Handles both checked and unchecked exceptions
Integer count = RetryHelper.retry(() -> {
    return database.query("SELECT COUNT(*) FROM users");
}, 4, 1000);
