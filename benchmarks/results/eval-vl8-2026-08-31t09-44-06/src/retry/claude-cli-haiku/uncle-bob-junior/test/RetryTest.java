import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

class RetryTest {
  
  @Test
  void succeedsOnFirstAttempt() throws Exception {
    String result = Retry.builder()
      .maxAttempts(3)
      .delayMs(10)
      .execute(() -> "success");
    
    assertEquals("success", result);
  }

  @Test
  void succeedsAfterFailures() throws Exception {
    AtomicInteger attempts = new AtomicInteger(0);
    
    String result = Retry.builder()
      .maxAttempts(3)
      .delayMs(10)
      .execute(() -> {
        attempts.incrementAndGet();
        if (attempts.get() < 3) {
          throw new IOException("retry me");
        }
        return "success";
      });
    
    assertEquals("success", result);
    assertEquals(3, attempts.get());
  }

  @Test
  void throwsAfterMaxAttempts() {
    AtomicInteger attempts = new AtomicInteger(0);
    
    assertThrows(IOException.class, () -> {
      Retry.builder()
        .maxAttempts(3)
        .delayMs(10)
        .execute(() -> {
          attempts.incrementAndGet();
          throw new IOException("always fails");
        });
    });
    
    assertEquals(3, attempts.get());
  }

  @Test
  void respectsDelayBetweenAttempts() {
    AtomicInteger attempts = new AtomicInteger(0);
    long startTime = System.currentTimeMillis();
    
    assertThrows(IOException.class, () -> {
      Retry.builder()
        .maxAttempts(3)
        .delayMs(50)
        .execute(() -> {
          attempts.incrementAndGet();
          throw new IOException("always fails");
        });
    });
    
    long elapsed = System.currentTimeMillis() - startTime;
    assertEquals(3, attempts.get());
    assertTrue(elapsed >= 100, "Should delay at least 100ms for 2 retries");
  }

  @Test
  void throwsInvalidArgumentForZeroAttempts() {
    assertThrows(IllegalArgumentException.class, () -> 
      Retry.builder()
        .maxAttempts(0)
        .execute(() -> "test")
    );
  }

  @Test
  void propagatesInterruptedException() {
    assertThrows(InterruptedException.class, () -> {
      Retry.builder()
        .maxAttempts(2)
        .delayMs(100)
        .execute(() -> {
          throw new InterruptedException("interrupted");
        });
    });
  }
}
