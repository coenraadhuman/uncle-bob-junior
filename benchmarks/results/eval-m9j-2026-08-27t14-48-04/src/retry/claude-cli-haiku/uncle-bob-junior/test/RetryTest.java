public class RetryTest {
    private Retry<String> retry;

    @Before
    public void setUp() {
        retry = new Retry<>(3, 100);
    }

    @Test
    public void successOnFirstAttempt() throws Exception {
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }

    @Test
    public void successAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("fail");
            }
            return "success";
        });
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    public void throwsAfterMaxAttempts() {
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> {
                throw new RuntimeException("always fails");
            })
        );
    }

    @Test
    public void invalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry<>(0, 1000));
    }

    @Test
    public void negativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retry<>(3, -1));
    }
}
