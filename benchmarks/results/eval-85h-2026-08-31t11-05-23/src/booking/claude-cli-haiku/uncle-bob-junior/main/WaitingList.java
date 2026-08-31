import java.util.*;

class WaitingList {
    private final Queue<WaitingListEntry> entries = new LinkedList<>();

    void add(WaitingListEntry entry) { entries.offer(entry); }
    Optional<WaitingListEntry> next() { return Optional.ofNullable(entries.poll()); }
    boolean isEmpty() { return entries.isEmpty(); }
}
