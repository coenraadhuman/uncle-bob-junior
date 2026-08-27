package retry;

/** An operation that produces a value and may fail with any exception. */
@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}
