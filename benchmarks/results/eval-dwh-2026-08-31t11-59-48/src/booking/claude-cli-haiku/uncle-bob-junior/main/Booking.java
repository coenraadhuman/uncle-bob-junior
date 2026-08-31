import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class Booking {
  final String bookingId;
  final String eventId;
  final String customerId;
  final List<Seat> seats;
  final TicketTier tier;
  final BigDecimal totalPrice;
  final LocalDateTime confirmedAt;
  
  Booking(String bookingId, String eventId, String customerId, List<Seat> seats,
          TicketTier tier, BigDecimal totalPrice, LocalDateTime confirmedAt) {
    this.bookingId = bookingId;
    this.eventId = eventId;
    this.customerId = customerId;
    this.seats = new ArrayList<>(seats);
    this.tier = tier;
    this.totalPrice = totalPrice;
    this.confirmedAt = confirmedAt;
  }
}
