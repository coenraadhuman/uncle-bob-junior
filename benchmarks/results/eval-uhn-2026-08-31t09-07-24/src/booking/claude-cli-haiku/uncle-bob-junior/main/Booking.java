import java.time.LocalDateTime;
import java.util.*;

class Booking {
    private final String id;
    private final List<Integer> seatNumbers;
    private final double totalPrice;
    private final LocalDateTime bookedAt;
    
    public Booking(String id, List<Integer> seatNumbers, double totalPrice, LocalDateTime bookedAt) {
        this.id = id;
        this.seatNumbers = seatNumbers;
        this.totalPrice = totalPrice;
        this.bookedAt = bookedAt;
    }
    
    public String id() { return id; }
    public List<Integer> seatNumbers() { return seatNumbers; }
    public double totalPrice() { return totalPrice; }
    public LocalDateTime bookedAt() { return bookedAt; }
}
