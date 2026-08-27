String result = Retry.run(
        () -> callFlakyService(),
        3,
        Duration.ofSeconds(2)
);
