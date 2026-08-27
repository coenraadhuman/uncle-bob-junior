Retry retry = new Retry(3, 500); // 3 attempts, 500ms delay

// Void operation
retry.execute(() -> makeNetworkCall());

// Operation with return value
String result = retry.execute(() -> fetchDataFromApi());
