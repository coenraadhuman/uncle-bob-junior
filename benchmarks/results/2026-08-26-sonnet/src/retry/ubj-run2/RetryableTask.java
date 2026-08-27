package com.plg.retry;

/**
 * A unit of work that may fail and can be retried.
 *
 * @param <T> the type produced on success
 */
@FunctionalInterface
public interface RetryableTask<T> {

    T execute() throws Exception;
}
