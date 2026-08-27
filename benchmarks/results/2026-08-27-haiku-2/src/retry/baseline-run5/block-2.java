// Void operation - retries up to 3 times with 1 second delay
RetryHelper.retry(
    () -> someMethod(),
    3,
    1000
);

// Operation returning a value
String result = RetryHelper.retry(
    () -> fetchData(),
    5,
    2000
);

// With lambdas that throw checked exceptions
RetryHelper.retry(
    () -> {
        URL url = new URL("http://example.com");
        url.openConnection().getInputStream();
    },
    3,
    500
);
