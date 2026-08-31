import java.time.LocalDateTime;

class Seat {
  final String id;
  SeatStatus status;
  String holdId;
  LocalDateTime holdExpiresAt;
  
  Seat(String id) {
    this.id = id;
    this.status = SeatStatus.AVAILABLE;
  }
}
