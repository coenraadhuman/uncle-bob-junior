import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class SeatInventory {
  private final Map<String, Seat> seats = new ConcurrentHashMap<>();
  private final Clock clock;
  
  SeatInventory(int totalSeats, Clock clock) {
    this.clock = clock;
    for (int i = 0; i < totalSeats; i++) {
      seats.put("S" + i, new Seat("S" + i));
    }
  }
  
  synchronized List<Seat> reserveSeats(int quantity) {
    List<Seat> available = findAvailableSeats(quantity);
    if (available.size() < quantity) return new ArrayList<>();
    return available;
  }
  
  synchronized List<Seat> findAvailableSeats(int quantity) {
    return seats.values().stream()
        .filter(s -> s.status == SeatStatus.AVAILABLE)
        .limit(quantity)
        .collect(Collectors.toList());
  }
  
  synchronized void holdSeats(List<Seat> seatsToHold, String holdId, LocalDateTime expiresAt) {
    for (Seat seat : seatsToHold) {
      seat.status = SeatStatus.HELD;
      seat.holdId = holdId;
      seat.holdExpiresAt = expiresAt;
    }
  }
  
  synchronized void confirmSeats(List<Seat> seatsToConfirm) {
    for (Seat seat : seatsToConfirm) {
      seat.status = SeatStatus.CONFIRMED;
      seat.holdId = null;
      seat.holdExpiresAt = null;
    }
  }
  
  synchronized void releaseSeats(List<Seat> seatsToRelease) {
    for (Seat seat : seatsToRelease) {
      seat.status = SeatStatus.AVAILABLE;
      seat.holdId = null;
      seat.holdExpiresAt = null;
    }
  }
  
  synchronized int getAvailableSeatCount() {
    return (int) seats.values().stream()
        .filter(s -> s.status == SeatStatus.AVAILABLE)
        .count();
  }
}
