import java.time.Duration;

/**
 * An operation that produces a value and may fail with an exception.
 *
 * @param <T> the type of the result
 */
@FunctionalInterface
public interface RetryableOperation<T> {
    T run() throws Exception;
}
