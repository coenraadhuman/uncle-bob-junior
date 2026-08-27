// File: RetryableOperation.java
package com.plg.retry;

/**
 * A unit of work that may fail and is safe to attempt again.
 *
 * @param <T> the type produced on success
 */
@FunctionalInterface
public interface RetryableOperation<T> {

    /**
     * Performs the operation.
     *
     * @return the result on success
     * @throws Exception if the operation fails; the caller may retry
     */
    T execute() throws Exception;
}
