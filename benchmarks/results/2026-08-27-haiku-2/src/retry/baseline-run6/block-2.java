// Example 1: Void operation
RetryHelper retryHelper = new RetryHelper(3, 1000);
try {
    retryHelper.execute(() -> {
        // Some operation that might fail
        connectToDatabase();
        return null;
    });
} catch (RetryHelper.RetryException e) {
    System.err.println(e.getMessage());
    e.getCause().printStackTrace();
}

// Example 2: Operation with return value
RetryHelper retryHelper = new RetryHelper(5, 500);
try {
    String result = retryHelper.execute(() -> fetchDataFromApi());
    System.out.println("Result: " + result);
} catch (Exception e) {
    System.err.println("Failed to fetch data");
}

// Example 3: Lambda with logic
RetryHelper retryHelper = new RetryHelper(3, 2000);
try {
    int value = retryHelper.execute(() -> {
        int data = riskyCalculation();
        if (data < 0) throw new IllegalStateException("Invalid result");
        return data;
    });
} catch (Exception e) {
    e.printStackTrace();
}
