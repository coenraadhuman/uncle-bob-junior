import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class Event {
  final String eventId;
  final String name;
  final LocalDateTime eventDate;
  final int totalSeats;
  final Map<TicketTier, BigDecimal> pricing;
  
  Event(String eventId, String name, LocalDateTime eventDate, int totalSeats,
        Map<TicketTier, BigDecimal> pricing) {
    this.eventId = eventId;
    this.name = name;
    this.eventDate = eventDate;
    this.totalSeats = totalSeats;
    this.pricing = new HashMap<>(pricing);
  }
}
