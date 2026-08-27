import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RetryHelperTest {
    private RetryHelper retryHelper;

    @Before
    public void setUp() {
        retryHelper = new RetryHelper(3, 10);
    }

    @Test
    public void execute_returns_result_on_first_attempt() throws Exception {
        int result = retryHelper.execute(() -> 42);
        assertEquals(42, result);
    }

    @Test
    public void execute_retries_until_success() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        int result = retryHelper.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("fail");
            }
            return 42;
        });
        assertEquals(3, attempts.get());
        assertEquals(42, result);
    }

    @Test(expected = RuntimeException.class)
    public void execute_throws_exception_after_max_attempts() throws Exception {
        retryHelper.execute(() -> {
            throw new RuntimeException("permanent failure");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejects_zero_attempts() {
        new RetryHelper(0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejects_negative_delay() {
        new RetryHelper(3, -1);
    }
}
