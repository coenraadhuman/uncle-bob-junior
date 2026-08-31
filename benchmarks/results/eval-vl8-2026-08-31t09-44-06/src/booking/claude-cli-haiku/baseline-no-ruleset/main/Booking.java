import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

class Booking {
    private final String bookingId;
    private final String customerId;
    private final List<Seat> seats;
    private final double totalPrice;
    private final LocalDateTime bookedAt;
    private final LocalDateTime eventDate;
    private BookingStatus status;

    enum BookingStatus {
        CONFIRMED, CANCELLED
    }

    public Booking(String bookingId, String customerId, List<Seat> seats, double totalPrice, LocalDateTime bookedAt, LocalDateTime eventDate) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.seats = new ArrayList<>(seats);
        this.totalPrice = totalPrice;
        this.bookedAt = bookedAt;
        this.eventDate = eventDate;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public double calculateRefund(LocalDateTime cancelDate) {
        long daysUntilEvent = ChronoUnit.DAYS.between(cancelDate, eventDate);
        if (daysUntilEvent > 30) {
            return totalPrice;
        } else if (daysUntilEvent >= 7) {
            return totalPrice * 0.5;
        }
        return 0;
    }
}
