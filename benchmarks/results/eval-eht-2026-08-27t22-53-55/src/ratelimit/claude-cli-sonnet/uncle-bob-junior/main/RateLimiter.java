// RateLimiter.java
package com.example.ratelimit;

import java.time.Instant;

/**
 * Decides whether a client may proceed with a request right now.
 * Implementations must be safe for concurrent use by multiple client IDs.
 */
public interface RateLimiter {

    /**
     * @param clientId identifier of the caller (e.g. IP address)
     * @param now      current instant, supplied by the caller for testability
     * @return true if the request is allowed and has been recorded, false if it must be rejected
     */
    boolean tryAcquire(String clientId, Instant now);
}
