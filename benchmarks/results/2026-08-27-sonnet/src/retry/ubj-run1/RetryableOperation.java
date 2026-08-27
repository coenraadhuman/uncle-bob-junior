// RetryableOperation.java
@FunctionalInterface
public interface RetryableOperation<T> {
    T run() throws Exception;
}
