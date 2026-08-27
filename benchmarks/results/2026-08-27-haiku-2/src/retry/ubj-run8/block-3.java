RetryHelper retryHelper = new RetryHelper(3, 1000);

// String result
String data = retryHelper.execute(() -> fetchDataFromAPI());

// Void operation
retryHelper.execute(() -> {
    sendMessage();
    return null;
});
