import java.time.LocalDateTime;
import java.util.*;

class SeatHold {
    private final String id;
    private final List<Integer> seatNumbers;
    private final LocalDateTime expiryTime;
    private final double totalPrice;
    
    public SeatHold(String id, List<Integer> seatNumbers, LocalDateTime expiryTime, double totalPrice) {
        this.id = id;
        this.seatNumbers = seatNumbers;
        this.expiryTime = expiryTime;
        this.totalPrice = totalPrice;
    }
    
    public String id() { return id; }
    public List<Integer> seatNumbers() { return seatNumbers; }
    public LocalDateTime expiryTime() { return expiryTime; }
    public double totalPrice() { return totalPrice; }
}
