@FunctionalInterface
public interface Retryable<T> {
    T execute() throws Exception;
}
