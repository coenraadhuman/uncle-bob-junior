public class RetryTest {
    @Test
    public void successOnFirstAttempt() throws Exception {
        Retry retry = new Retry(3, 100);
        boolean[] executed = {false};
        
        retry.execute(() -> executed[0] = true);
        
        assertTrue(executed[0]);
    }
    
    @Test
    public void successAfterRetry() throws Exception {
        Retry retry = new Retry(3, 50);
        int[] attempts = {0};
        
        retry.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 2) throw new RuntimeException("Fail");
        });
        
        assertEquals(2, attempts[0]);
    }
    
    @Test
    public void maxAttemptsExceeded() throws Exception {
        Retry retry = new Retry(3, 50);
        
        assertThrows(RuntimeException.class, () -> 
            retry.execute(() -> { throw new RuntimeException("Always fail"); })
        );
    }
    
    @Test
    public void waitsDelayBetweenAttempts() throws Exception {
        Retry retry = new Retry(3, 100);
        long[] times = new long[3];
        int[] attempts = {0};
        
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> {
                times[attempts[0]] = System.currentTimeMillis();
                attempts[0]++;
                throw new RuntimeException("Fail");
            })
        );
        
        assertTrue(times[1] - times[0] >= 100);
        assertTrue(times[2] - times[1] >= 100);
    }
    
    @Test
    public void returnsValueAfterRetry() throws Exception {
        Retry retry = new Retry(3, 50);
        int[] attempts = {0};
        
        String result = retry.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 2) throw new RuntimeException("Fail");
            return "Success";
        });
        
        assertEquals("Success", result);
    }
    
    @Test
    public void throwsOnInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, 100));
    }
}
