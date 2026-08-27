// RetryableOperation.java
package com.plg.retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T call() throws Exception;
}
