package retry;

/** An operation with no result that may fail with any exception. */
@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Exception;
}
