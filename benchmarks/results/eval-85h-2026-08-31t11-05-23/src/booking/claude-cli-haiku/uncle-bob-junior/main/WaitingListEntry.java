import java.util.*;

class WaitingListEntry {
    private final String id;
    private final int count;
    private final List<TicketType> ticketTypes;

    WaitingListEntry(String id, int count, List<TicketType> ticketTypes) {
        this.id = id;
        this.count = count;
        this.ticketTypes = new ArrayList<>(ticketTypes);
    }

    String id() { return id; }
    int count() { return count; }
    List<TicketType> ticketTypes() { return new ArrayList<>(ticketTypes); }
}
