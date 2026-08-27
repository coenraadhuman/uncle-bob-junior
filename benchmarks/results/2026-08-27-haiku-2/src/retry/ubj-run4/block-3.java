// Retry a function that returns a value
String response = Retry.execute(
    () -> httpClient.get("https://api.example.com/data"),
    3,
    1000
);

// Retry a void operation
Retry.execute(
    () -> database.save(record),
    5,
    2000
);
