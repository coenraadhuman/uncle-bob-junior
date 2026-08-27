public class RetryHelperTest {
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }

    @Test
    void retriesAndSucceedsAfterFailure() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) throw new IOException("fail");
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsAfterMaxAttemptsExhausted() throws Exception {
        RetryHelper retry = new RetryHelper(2, 100);
        AtomicInteger attempts = new AtomicInteger(0);
        
        assertThrows(IOException.class, () -> 
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new IOException("always fail");
            })
        );
        
        assertEquals(2, attempts.get());
    }

    @Test
    void voidOperationWorks() throws Exception {
        RetryHelper retry = new RetryHelper(3, 100);
        AtomicInteger callCount = new AtomicInteger(0);
        
        retry.executeVoid(() -> callCount.incrementAndGet());
        
        assertEquals(1, callCount.get());
    }

    @Test
    void respectsDelayBetweenAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(3, 200);
        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        
        try {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("fail");
            });
        } catch (RuntimeException e) {
            // expected
        }
        
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 400, "Should have delayed ~400ms for 2 retries");
    }
}
