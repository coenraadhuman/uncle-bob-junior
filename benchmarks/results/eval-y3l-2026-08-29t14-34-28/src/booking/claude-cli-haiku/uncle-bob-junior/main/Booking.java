import java.time.*;
import java.util.*;

class Booking {
    enum Status { CONFIRMED, CANCELLED }

    private final String id;
    private final List<Seat> seats;
    private final TicketType type;
    private final Money price;
    private final LocalDateTime bookedAt;
    private final LocalDateTime eventDate;
    private Status status;

    Booking(String id, List<Seat> seats, TicketType type, Money price, 
            LocalDateTime bookedAt, LocalDateTime eventDate) {
        this.id = id;
        this.seats = new ArrayList<>(seats);
        this.type = type;
        this.price = price;
        this.bookedAt = bookedAt;
        this.eventDate = eventDate;
        this.status = Status.CONFIRMED;
    }

    String id() {
        return id;
    }

    List<Seat> seats() {
        return new ArrayList<>(seats);
    }

    Money price() {
        return price;
    }

    LocalDateTime eventDate() {
        return eventDate;
    }

    boolean isConfirmed() {
        return status == Status.CONFIRMED;
    }

    Money refundAmount(LocalDateTime now) {
        long daysLeft = ChronoUnit.DAYS.between(now, eventDate);
        if (daysLeft > 30) return price;
        if (daysLeft >= 7) return price.discounted(50.0);
        return new Money(0);
    }

    void cancel() {
        status = Status.CANCELLED;
    }
}
