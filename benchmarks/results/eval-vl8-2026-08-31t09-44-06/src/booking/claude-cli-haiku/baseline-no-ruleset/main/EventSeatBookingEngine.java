import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EventSeatBookingEngine {
    
    enum SeatType {
        ADULT, CHILD, SENIOR, STUDENT
    }
    
    enum SeatStatus {
        AVAILABLE, HELD, BOOKED
    }
    
    enum BookingStatus {
        CONFIRMED, CANCELLED
    }
    
    static class Ticket {
        private final SeatType type;
        private final double price;
        
        Ticket(SeatType type, double price) {
            this.type = type;
            this.price = price;
        }
        
        double getPrice() {
            return price;
        }
    }
    
    static class Seat {
        private final String seatId;
        private SeatStatus status;
        private String holdId;
        private final SeatType type;
        
        Seat(String seatId, SeatType type) {
            this.seatId = seatId;
            this.type = type;
            this.status = SeatStatus.AVAILABLE;
        }
        
        synchronized boolean hold(String holdId) {
            if (status == SeatStatus.AVAILABLE) {
                this.status = SeatStatus.HELD;
                this.holdId = holdId;
                return true;
            }
            return false;
        }
        
        synchronized boolean release() {
            if (status == SeatStatus.HELD) {
                this.status = SeatStatus.AVAILABLE;
                this.holdId = null;
                return true;
            }
            return false;
        }
        
        synchronized boolean book() {
            if (status == SeatStatus.HELD) {
                this.status = SeatStatus.BOOKED;
                return true;
            }
            return false;
        }
        
        synchronized void cancel() {
            this.status = SeatStatus.AVAILABLE;
            this.holdId = null;
        }
        
        String getSeatId() {
            return seatId;
        }
        
        SeatStatus getStatus() {
            return status;
        }
        
        SeatType getType() {
            return type;
        }
    }
    
    static class SeatHold {
        private final String holdId;
        private final List<Seat> seats;
        private final LocalDateTime expiresAt;
        
        SeatHold(String holdId, List<Seat> seats, LocalDateTime createdAt) {
            this.holdId = holdId;
            this.seats = new ArrayList<>(seats);
            this.expiresAt = createdAt.plusMinutes(15);
        }
        
        String getHoldId() {
            return holdId;
        }
        
        List<Seat> getSeats() {
            return new ArrayList<>(seats);
        }
        
        boolean isExpired(LocalDateTime now) {
            return now.isAfter(expiresAt);
        }
        
        LocalDateTime getExpiresAt() {
            return expiresAt;
        }
    }
    
    static class Booking {
        private final String bookingId;
        private final List<Seat> seats;
        private final double totalPrice;
        private final LocalDateTime eventDate;
        private final LocalDateTime bookedAt;
        private BookingStatus status;
        
        Booking(String bookingId, List<Seat> seats, double totalPrice, LocalDateTime eventDate) {
            this.bookingId = bookingId;
            this.seats = new ArrayList<>(seats);
            this.totalPrice = totalPrice;
            this.eventDate = eventDate;
            this.bookedAt = LocalDateTime.now();
            this.status = BookingStatus.CONFIRMED;
        }
        
        String getBookingId() {
            return bookingId;
        }
        
        List<Seat> getSeats() {
            return new ArrayList<>(seats);
        }
        
        double getTotalPrice() {
            return totalPrice;
        }
        
        BookingStatus getStatus() {
            return status;
        }
        
        double calculateRefund(LocalDateTime now) {
            long daysBefore = ChronoUnit.DAYS.between(now, eventDate);
            
            if (daysBefore > 30) {
                return totalPrice;
            } else if (daysBefore >= 7) {
                return totalPrice * 0.5;
            } else {
                return 0;
            }
        }
    }
    
    static class WaitlistEntry {
        private final String entryId;
        private final int seatsRequested;
        private final Map<SeatType, Integer> seatTypes;
        
        WaitlistEntry(int seatsRequested, Map<SeatType, Integer> seatTypes) {
            this.entryId = UUID.randomUUID().toString();
            this.seatsRequested = seatsRequested;
            this.seatTypes = new HashMap<>(seatTypes);
        }
        
        String getEntryId() {
            return entryId;
        }
        
        int getSeatsRequested() {
            return seatsRequested;
        }
        
        Map<SeatType, Integer> getSeatTypes() {
            return new HashMap<>(seatTypes);
        }
    }
    
    private final Map<String, Seat> allSeats;
    private final Map<String, SeatHold> activeHolds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    private final Map<SeatType, Ticket> ticketPrices;
    private final LocalDateTime eventDate;
    
    public EventSeatBookingEngine(LocalDateTime eventDate, int totalSeats, Map<SeatType, Double> prices) {
        this.eventDate = eventDate;
        this.allSeats = new ConcurrentHashMap<>();
        this.activeHolds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitlist = new ConcurrentLinkedQueue<>();
        this.ticketPrices = new ConcurrentHashMap<>();
        
        for (int i = 0; i < totalSeats; i++) {
            SeatType type = SeatType.values()[i % 4];
            allSeats.put("SEAT-" + i, new Seat("SEAT-" + i, type));
        }
        
        for (SeatType type : SeatType.values()) {
            ticketPrices.put(type, new Ticket(type, prices.getOrDefault(type, 50.0)));
        }
    }
    
    public synchronized SeatHold findAndHoldSeats(int numSeats, Map<SeatType, Integer> seatTypes, LocalDateTime now) {
        expireOldHolds(now);
        
        List<Seat> availableSeats = new ArrayList<>();
        for (Map.Entry<SeatType, Integer> entry : seatTypes.entrySet()) {
            SeatType type = entry.getKey();
            int needed = entry.getValue();
            
            allSeats.values().stream()
                .filter(s -> s.getType() == type && s.getStatus() == SeatStatus.AVAILABLE)
                .limit(needed)
                .forEach(availableSeats::add);
        }
        
        if (availableSeats.size() < numSeats) {
            waitlist.add(new WaitlistEntry(numSeats, seatTypes));
            return null;
        }
        
        String holdId = UUID.randomUUID().toString();
        SeatHold hold = new SeatHold(holdId, availableSeats, now);
        
        for (Seat seat : availableSeats) {
            seat.hold(holdId);
        }
        
        activeHolds.put(holdId, hold);
        return hold;
    }
    
    public synchronized String confirmBooking(String holdId) {
        SeatHold hold = activeHolds.get(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Hold not found: " + holdId);
        }
        
        List<Seat> seats = hold.getSeats();
        for (Seat seat : seats) {
            if (!seat.book()) {
                throw new IllegalStateException("Cannot book seat: " + seat.getSeatId());
            }
        }
        
        double totalPrice = seats.stream()
            .mapToDouble(s -> ticketPrices.get(s.getType()).getPrice())
            .sum();
        
        if (seats.size() >= 10) {
            totalPrice *= 0.95;
        }
        
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, seats, totalPrice, eventDate);
        bookings.put(bookingId, booking);
        activeHolds.remove(holdId);
        
        return bookingId;
    }
    
    public synchronized void releaseHold(String holdId) {
        SeatHold hold = activeHolds.remove(holdId);
        if (hold != null) {
            hold.getSeats().forEach(Seat::release);
        }
    }
    
    public synchronized double cancelBooking(String bookingId, LocalDateTime cancellationTime) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }
        
        double refund = booking.calculateRefund(cancellationTime);
        booking.status = BookingStatus.CANCELLED;
        
        for (Seat seat : booking.getSeats()) {
            seat.cancel();
        }
        
        processWaitlist(cancellationTime);
        return refund;
    }
    
    private void expireOldHolds(LocalDateTime now) {
        List<String> expiredHolds = activeHolds.values().stream()
            .filter(h -> h.isExpired(now))
            .map(SeatHold::getHoldId)
            .collect(Collectors.toList());
        
        for (String holdId : expiredHolds) {
            releaseHold(holdId);
        }
    }
    
    private void processWaitlist(LocalDateTime now) {
        while (!waitlist.isEmpty()) {
            WaitlistEntry entry = waitlist.peek();
            Map<SeatType, Integer> needed = entry.getSeatTypes();
            
            List<Seat> available = new ArrayList<>();
            for (Map.Entry<SeatType, Integer> typeEntry : needed.entrySet()) {
                SeatType type = typeEntry.getKey();
                int count = typeEntry.getValue();
                
                allSeats.values().stream()
                    .filter(s -> s.getType() == type && s.getStatus() == SeatStatus.AVAILABLE)
                    .limit(count)
                    .forEach(available::add);
            }
            
            if (available.size() >= entry.getSeatsRequested()) {
                waitlist.poll();
                String holdId = UUID.randomUUID().toString();
                SeatHold hold = new SeatHold(holdId, available, now);
                
                for (Seat seat : available) {
                    seat.hold(holdId);
                }
                
                activeHolds.put(holdId, hold);
            } else {
                break;
            }
        }
    }
    
    public int getAvailableSeats() {
        return (int) allSeats.values().stream()
            .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
            .count();
    }
    
    public int getHeldSeats() {
        return (int) allSeats.values().stream()
            .filter(s -> s.getStatus() == SeatStatus.HELD)
            .count();
    }
    
    public int getBookedSeats() {
        return (int) allSeats.values().stream()
            .filter(s -> s.getStatus() == SeatStatus.BOOKED)
            .count();
    }
    
    public int getWaitlistSize() {
        return waitlist.size();
    }
    
    public static void main(String[] args) {
        LocalDateTime eventDate = LocalDateTime.now().plusMonths(2);
        Map<SeatType, Double> prices = new HashMap<>();
        prices.put(SeatType.ADULT, 50.0);
        prices.put(SeatType.CHILD, 25.0);
        prices.put(SeatType.SENIOR, 35.0);
        prices.put(SeatType.STUDENT, 30.0);
        
        EventSeatBookingEngine engine = new EventSeatBookingEngine(eventDate, 20, prices);
        LocalDateTime now = LocalDateTime.now();
        
        Map<SeatType, Integer> request1 = new HashMap<>();
        request1.put(SeatType.ADULT, 10);
        SeatHold hold1 = engine.findAndHoldSeats(10, request1, now);
        String booking1 = engine.confirmBooking(hold1.getHoldId());
        System.out.println("Booking 1 (10 adults, group discount): " + booking1);
        
        Map<SeatType, Integer> request2 = new HashMap<>();
        request2.put(SeatType.SENIOR, 5);
        SeatHold hold2 = engine.findAndHoldSeats(5, request2, now);
        String booking2 = engine.confirmBooking(hold2.getHoldId());
        System.out.println("Booking 2 (5 seniors): " + booking2);
        
        Map<SeatType, Integer> request3 = new HashMap<>();
        request3.put(SeatType.CHILD, 5);
        SeatHold hold3 = engine.findAndHoldSeats(5, request3, now);
        System.out.println("Booking 3 on waitlist: " + (hold3 == null ? "Yes" : "No"));
        
        double refund = engine.cancelBooking(booking2, eventDate.minusDays(45));
        System.out.println("Refund (45 days before): " + refund);
        System.out.println("Available: " + engine.getAvailableSeats() + ", Held: " + engine.getHeldSeats() + 
                          ", Booked: " + engine.getBookedSeats() + ", Waitlist: " + engine.getWaitlistSize());
    }
}
