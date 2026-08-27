// RetryableOperation.java
package retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T call() throws Exception;
}
