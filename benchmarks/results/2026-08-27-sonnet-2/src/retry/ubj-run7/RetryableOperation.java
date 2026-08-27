package com.example.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * An operation that can be retried. May throw any exception; the retry
 * mechanism does not assume a checked/unchecked distinction.
 */
@FunctionalInterface
public interface RetryableOperation<T> {
    T execute() throws Exception;
}
