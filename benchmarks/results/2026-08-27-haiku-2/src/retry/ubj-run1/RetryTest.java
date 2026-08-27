class RetryTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 50;

    @Test
    void successesImmediately() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }

    @Test
    void retriesAndEventuallySucceeds() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);

        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new IOException("Temporary failure");
            }
            return "success";
        });

        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsAfterExhaustingAttempts() {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);

        IOException thrown = assertThrows(IOException.class, () ->
            retry.execute(() -> {
                throw new IOException("Persistent failure");
            })
        );

        assertEquals("Persistent failure", thrown.getMessage());
    }

    @Test
    void waitsBeforeRetrying() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, 100);
        long start = System.currentTimeMillis();

        assertThrows(IOException.class, () ->
            retry.execute(() -> {
                throw new IOException("Fail");
            })
        );

        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 200, "Should wait at least 2 * 100ms between 3 attempts");
    }

    @Test
    void workdsWithOperationsThatReturnNull() throws Exception {
        Retry retry = new Retry(MAX_ATTEMPTS, DELAY_MILLIS);
        String result = retry.execute(() -> null);
        assertNull(result);
    }
}
