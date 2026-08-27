// Operation.java
package com.plg.retry;

@FunctionalInterface
public interface Operation<T> {
    T execute() throws Exception;
}
