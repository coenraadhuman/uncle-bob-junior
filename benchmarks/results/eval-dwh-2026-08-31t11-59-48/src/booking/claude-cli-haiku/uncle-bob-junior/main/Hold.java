import java.time.LocalDateTime;
import java.util.*;

class Hold {
  final String holdId;
  final String eventId;
  final String customerId;
  final List<Seat> seats;
  final TicketTier tier;
  final LocalDateTime expiresAt;
  
  Hold(String holdId, String eventId, String customerId, List<Seat> seats,
       TicketTier tier, LocalDateTime expiresAt) {
    this.holdId = holdId;
    this.eventId = eventId;
    this.customerId = customerId;
    this.seats = new ArrayList<>(seats);
    this.tier = tier;
    this.expiresAt = expiresAt;
  }
}
