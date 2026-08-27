String result = RetryHelper.runWithRetry(
        () -> callFlakyService(),
        3,      // max attempts
        500L    // 500ms delay between attempts
);
