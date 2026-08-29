import java.time.*;
import java.util.*;

class WaitingListEntry {
    private final String id;
    private final int count;
    private final TicketType type;
    private final LocalDateTime at;

    WaitingListEntry(String id, int count, TicketType type, LocalDateTime at) {
        this.id = id;
        this.count = count;
        this.type = type;
        this.at = at;
    }

    String id() {
        return id;
    }

    int count() {
        return count;
    }

    TicketType type() {
        return type;
    }
}
