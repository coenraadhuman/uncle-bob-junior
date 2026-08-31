// Event.java
public final class Event {
    private final String id;
    private final String name;
    private final long eventDateMillis;
    private final int capacity;
    private final java.util.Map<String, Seat> seats;
    private final java.util.Deque<WaitListEntry> waitList;

    public Event(String id, String name, long eventDateMillis, int capacity) {
        this.id = id;
        this.name = name;
        this.eventDateMillis = eventDateMillis;
        this.capacity = capacity;
        this.seats = new java.util.HashMap<>();
        this.waitList = new java.util.LinkedList<>();
        initializeSeats();
    }

    private void initializeSeats() {
        for (int i = 0; i < capacity; i++) {
            String seatId = String.format("SEAT_%03d", i);
            seats.put(seatId, new Seat(seatId));
        }
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long eventDateMillis() {
        return eventDateMillis;
    }

    public int capacity() {
        return capacity;
    }

    public java.util.Map<String, Seat> seats() {
        return new java.util.HashMap<>(seats);
    }

    public void updateSeat(String seatId, Seat seat) {
        seats.put(seatId, seat);
    }

    public void addToWaitList(WaitListEntry entry) {
        waitList.addLast(entry);
    }

    public WaitListEntry pollWaitList() {
        return waitList.pollFirst();
    }

    public int availableSeats() {
        return (int) seats.values().stream()
            .filter(Seat::isAvailable)
            .count();
    }

    public boolean isSoldOut() {
        return availableSeats() == 0;
    }
}
