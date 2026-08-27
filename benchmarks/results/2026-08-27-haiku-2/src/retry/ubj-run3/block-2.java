// Operation returning a value
RetryHelper retry = new RetryHelper(3, 1000);
String result = retry.execute(() -> fetchDataFromAPI());

// Void operation
retry.executeVoid(() -> saveToDatabase());

// With lambda capturing local state
int retries = 5;
int delayMs = 500;
User user = retry.execute(() -> lookupUser(userId));
