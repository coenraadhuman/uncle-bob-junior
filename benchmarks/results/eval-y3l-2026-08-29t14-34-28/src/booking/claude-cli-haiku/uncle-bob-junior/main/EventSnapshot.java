import java.time.*;
import java.util.*;

class EventSnapshot {
    private final int available;
    private final int booked;
    private final int waitlisted;

    EventSnapshot(int available, int booked, int waitlisted) {
        this.available = available;
        this.booked = booked;
        this.waitlisted = waitlisted;
    }

    boolean isSoldOut() {
        return available == 0;
    }

    @Override
    public String toString() {
        return String.format("Available: %d, Booked: %d, Waitlist: %d", available, booked, waitlisted);
    }
}
