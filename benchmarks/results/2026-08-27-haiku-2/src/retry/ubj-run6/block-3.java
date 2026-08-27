// With defaults: 3 attempts, 1 second delay
Retry retry = Retry.withDefaults();

// Operation returning a value
String data = retry.execute(() -> fetchFromAPI());

// Void operation
retry.execute(() -> writeToDatabase());

// Custom attempts and delay
new Retry(5, 2000).execute(() -> callUnreliableService());
