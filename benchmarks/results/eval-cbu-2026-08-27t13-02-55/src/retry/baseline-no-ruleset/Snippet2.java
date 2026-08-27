// Basic usage
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

// Retry an operation that returns a value
String result = retry.execute(() -> {
  return callSomeService();
});

// Retry an operation without return value
retry.execute(() -> {
  updateDatabase();
  return null;
});

// Retry a lambda with checked exceptions
int value = retry.execute(() -> Integer.parseInt("42"));
