// RetryableAction.java
package retry;

@FunctionalInterface
public interface RetryableAction {
    void run() throws Exception;
}
