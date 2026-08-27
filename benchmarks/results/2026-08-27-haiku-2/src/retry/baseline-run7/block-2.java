// For operations that return a value
RetryHelper retry = new RetryHelper(3, 1000);

String result = retry.execute(() -> {
    // Your operation here
    return fetchDataFromAPI();
});

// For operations with no return value
retry.execute(() -> {
    // Your operation here
    saveDataToDB();
});
