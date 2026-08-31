public class RetryHelperTest {
    @Test
    public void successOnFirstAttempt() throws Exception {
        Operation<String> operation = () -> "success";
        String result = RetryHelper.executeWithRetry(operation);
        assertEquals("success", result);
    }

    @Test
    public void successAfterRetry() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Operation<String> operation = () -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        };

        String result = RetryHelper.executeWithRetry(operation, 3, 10);
        assertEquals("success", result);
        assertEquals(3, attemptCount.get());
    }

    @Test
    public void failureAfterMaxAttempts() throws Exception {
        Operation<String> operation = () -> {
            throw new RuntimeException("Always fails");
        };

        assertThrows(RuntimeException.class, () -> 
            RetryHelper.executeWithRetry(operation, 2, 10));
    }

    @Test
    public void respectsDelayBetweenAttempts() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Operation<String> operation = () -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 2) {
                throw new RuntimeException("Fail");
            }
            return "success";
        };

        long start = System.currentTimeMillis();
        RetryHelper.executeWithRetry(operation, 2, 50);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed >= 50, "Should wait between attempts");
    }
}
