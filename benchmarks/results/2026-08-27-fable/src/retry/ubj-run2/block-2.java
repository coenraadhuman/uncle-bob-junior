Retry retry = new Retry(3, Duration.ofMillis(500));

String response = retry.execute(() -> httpClient.fetch("https://example.org"));
retry.executeVoid(() -> repository.save(record));
