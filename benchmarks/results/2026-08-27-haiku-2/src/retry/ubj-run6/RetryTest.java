class RetryTest {
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 0);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    void succeedsAfterRetries() throws Exception {
        Retry retry = new Retry(3, 0);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Not yet");
            }
            return 42;
        });
        
        assertEquals(42, result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void throwsAfterMaxAttempts() {
        Retry retry = new Retry(3, 0);
        
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> {
                throw new RuntimeException("Always fails");
            })
        );
    }
    
    @Test
    void executesRunnableWithRetry() throws Exception {
        Retry retry = new Retry(2, 0);
        AtomicInteger counter = new AtomicInteger(0);
        
        retry.execute(() -> counter.incrementAndGet());
        
        assertEquals(1, counter.get());
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, 1000));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(3, -1));
    }
}
