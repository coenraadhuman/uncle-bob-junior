import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientRateLimiterRegistryTest {

    @Test
    void eachClientGetsAnIndependentBudget() {
        try (ClientRateLimiterRegistry registry =
                     new ClientRateLimiterRegistry(1, Duration.ofMinutes(1), Clock.systemUTC())) {

            assertTrue(registry.tryAcquire("client-a"));
            assertTrue(registry.tryAcquire("client-b"));
        }
    }

    @Test
    void sameClientSharesBudgetAcrossCalls() {
        try (ClientRateLimiterRegistry registry =
                     new ClientRateLimiterRegistry(1, Duration.ofMinutes(1), Clock.systemUTC())) {

            assertTrue(registry.tryAcquire("client-a"));
            assertFalse(registry.tryAcquire("client-a"));
        }
    }
}
