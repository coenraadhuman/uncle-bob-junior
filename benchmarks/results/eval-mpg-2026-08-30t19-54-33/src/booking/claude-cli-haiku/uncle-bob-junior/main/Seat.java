// Seat.java
public final class Seat {
    private final String id;
    private final SeatState state;

    public Seat(String id) {
        this.id = id;
        this.state = SeatState.AVAILABLE;
    }

    private Seat(String id, SeatState state) {
        this.id = id;
        this.state = state;
    }

    public String id() {
        return id;
    }

    public SeatState state() {
        return state;
    }

    public Seat withState(SeatState newState) {
        return new Seat(id, newState);
    }

    public boolean isAvailable() {
        return state == SeatState.AVAILABLE;
    }
}
