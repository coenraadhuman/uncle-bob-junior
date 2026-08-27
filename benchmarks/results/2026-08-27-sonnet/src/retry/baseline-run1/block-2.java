String result = Retry.run(
        3,
        Duration.ofSeconds(2),
        () -> callFlakyService(),
        failure -> System.out.println(failure + " - retrying...")
);
