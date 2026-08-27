// ThreadSleeper.java
package retry;

import java.time.Duration;

final class ThreadSleeper implements Sleeper {
    @Override
    public void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
