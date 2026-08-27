// File: RetryableOperation.java
package com.example.retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T call() throws Exception;
}
