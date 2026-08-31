// Sleeper.java
package retry;

import java.time.Duration;

/** Seam over blocking waits so retry timing can be faked in tests. */
interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
