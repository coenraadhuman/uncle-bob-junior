import java.time.*;
import java.util.*;
import java.util.concurrent.*;

class Event {
    private final String eventId;
    private final String eventName;
    private final LocalDateTime eventDate;
    private final Map<String, Seat> seats;

    public Event(String eventId, String eventName, LocalDateTime eventDate) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.seats = new ConcurrentHashMap<>();
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public Map<String, Seat> getSeats() {
        return new HashMap<>(seats);
    }

    public void addSeat(Seat seat) {
        seats.put(seat.getSeatNumber(), seat);
    }

    public int getAvailableSeatsCount() {
        return (int) seats.values().stream()
                .filter(s -> s.getStatus() == Seat.SeatStatus.AVAILABLE)
                .count();
    }

    public boolean isSoldOut() {
        return getAvailableSeatsCount() == 0;
    }
}
