// With return value (3 attempts, 1 second delay)
RetryHelper retryHelper = new RetryHelper();
String result = retryHelper.execute(() -> fetchDataFromApi());

// Void operation
retryHelper.executeVoid(() -> sendRequest());

// Custom configuration (5 attempts, 500ms delay)
RetryHelper customRetry = new RetryHelper(5, 500);
String data = customRetry.execute(() -> callUnreliableService());
