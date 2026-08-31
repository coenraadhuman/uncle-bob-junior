import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventSeatBookingEngine {
    
    enum TicketType {
        ADULT(100.0),
        CHILD(50.0),
        SENIOR(70.0),
        STUDENT(60.0);
        
        private final double basePrice;
        TicketType(double basePrice) { this.basePrice = basePrice; }
        public double getBasePrice() { return basePrice; }
    }
    
    enum SeatStatus { AVAILABLE, HELD, BOOKED }
    
    static class Seat {
        final int number;
        SeatStatus status;
        LocalDateTime holdExpiresAt;
        String bookingId;
        
        Seat(int number) {
            this.number = number;
            this.status = SeatStatus.AVAILABLE;
        }
    }
    
    static class Hold {
        final String id;
        final List<Integer> seatNumbers;
        final TicketType ticketType;
        final int quantity;
        final LocalDateTime expiresAt;
        
        Hold(String id, List<Integer> seats, TicketType type, int qty) {
            this.id = id;
            this.seatNumbers = new ArrayList<>(seats);
            this.ticketType = type;
            this.quantity = qty;
            this.expiresAt = LocalDateTime.now().plusMinutes(15);
        }
    }
    
    static class Booking {
        final String id;
        final List<Integer> seatNumbers;
        final TicketType ticketType;
        final int quantity;
        final double totalPrice;
        final LocalDateTime bookedAt;
        LocalDateTime cancelledAt;
        double refundAmount;
        
        Booking(String id, List<Integer> seats, TicketType type, int qty, double price) {
            this.id = id;
            this.seatNumbers = new ArrayList<>(seats);
            this.ticketType = type;
            this.quantity = qty;
            this.totalPrice = price;
            this.bookedAt = LocalDateTime.now();
        }
    }
    
    static class WaitListEntry {
        final String id;
        final TicketType ticketType;
        final int quantity;
        final LocalDateTime addedAt;
        
        WaitListEntry(String id, TicketType type, int qty) {
            this.id = id;
            this.ticketType = type;
            this.quantity = qty;
            this.addedAt = LocalDateTime.now();
        }
    }
    
    static class Event {
        final String id;
        final LocalDateTime eventDate;
        final Map<Integer, Seat> seats;
        final Map<String, Hold> holds;
        final Map<String, Booking> bookings;
        final Queue<WaitListEntry> waitList;
        
        Event(String id, LocalDateTime eventDate, int capacity) {
            this.id = id;
            this.eventDate = eventDate;
            this.seats = new ConcurrentHashMap<>();
            this.holds = new ConcurrentHashMap<>();
            this.bookings = new ConcurrentHashMap<>();
            this.waitList = new LinkedList<>();
            
            for (int i = 1; i <= capacity; i++) {
                seats.put(i, new Seat(i));
            }
        }
        
        synchronized int countAvailableSeats() {
            return (int) seats.values().stream()
                .filter(s -> s.status == SeatStatus.AVAILABLE)
                .count();
        }
        
        synchronized int countBookedSeats() {
            return (int) seats.values().stream()
                .filter(s -> s.status == SeatStatus.BOOKED)
                .count();
        }
    }
    
    private final Map<String, Event> events = new ConcurrentHashMap<>();
    private int idCounter = 0;
    
    public String createEvent(String eventId, LocalDateTime eventDate, int capacity) {
        events.put(eventId, new Event(eventId, eventDate, capacity));
        return eventId;
    }
    
    public synchronized String holdSeats(String eventId, TicketType ticketType, int quantity) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        
        expireHolds(event);
        
        List<Integer> available = getAvailableSeats(event, quantity);
        if (available.isEmpty()) return null;
        
        String holdId = "HOLD_" + (++idCounter);
        Hold hold = new Hold(holdId, available, ticketType, quantity);
        
        for (int seatNum : available) {
            Seat s = event.seats.get(seatNum);
            s.status = SeatStatus.HELD;
            s.holdExpiresAt = hold.expiresAt;
        }
        
        event.holds.put(holdId, hold);
        return holdId;
    }
    
    public synchronized String confirmBooking(String eventId, String holdId) {
        Event event = events.get(eventId);
        Hold hold = event.holds.get(holdId);
        if (hold == null) throw new IllegalArgumentException("Hold not found or expired");
        
        double price = calculatePrice(hold.ticketType, hold.quantity);
        
        String bookingId = "BOOK_" + (++idCounter);
        Booking booking = new Booking(bookingId, hold.seatNumbers, 
                                      hold.ticketType, hold.quantity, price);
        
        for (int seatNum : hold.seatNumbers) {
            Seat s = event.seats.get(seatNum);
            s.status = SeatStatus.BOOKED;
            s.bookingId = bookingId;
        }
        
        event.bookings.put(bookingId, booking);
        event.holds.remove(holdId);
        processWaitList(event);
        
        return bookingId;
    }
    
    public synchronized void releaseHold(String eventId, String holdId) {
        Event event = events.get(eventId);
        Hold hold = event.holds.get(holdId);
        if (hold != null) {
            for (int seatNum : hold.seatNumbers) {
                event.seats.get(seatNum).status = SeatStatus.AVAILABLE;
            }
            event.holds.remove(holdId);
            processWaitList(event);
        }
    }
    
    public synchronized double cancelBooking(String eventId, String bookingId) {
        Event event = events.get(eventId);
        Booking booking = event.bookings.get(bookingId);
        if (booking == null || booking.cancelledAt != null) {
            throw new IllegalArgumentException("Booking not found or already cancelled");
        }
        
        long daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), event.eventDate);
        double refund = 0;
        
        if (daysUntil > 30) {
            refund = booking.totalPrice;
        } else if (daysUntil > 7) {
            refund = booking.totalPrice * 0.5;
        }
        
        booking.cancelledAt = LocalDateTime.now();
        booking.refundAmount = refund;
        
        for (int seatNum : booking.seatNumbers) {
            Seat s = event.seats.get(seatNum);
            s.status = SeatStatus.AVAILABLE;
            s.bookingId = null;
        }
        
        processWaitList(event);
        return refund;
    }
    
    public synchronized String addToWaitList(String eventId, TicketType ticketType, int quantity) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        
        if (event.countAvailableSeats() >= quantity) return null;
        
        String entryId = "WAIT_" + (++idCounter);
        event.waitList.offer(new WaitListEntry(entryId, ticketType, quantity));
        return entryId;
    }
    
    public double getTicketPrice(TicketType ticketType, int quantity) {
        return calculatePrice(ticketType, quantity);
    }
    
    public int getAvailableSeatsCount(String eventId) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        return event.countAvailableSeats();
    }
    
    public int getBookedSeatsCount(String eventId) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        return event.countBookedSeats();
    }
    
    // Private helpers
    
    private double calculatePrice(TicketType type, int quantity) {
        double base = type.getBasePrice() * quantity;
        if (quantity >= 10) base *= 0.95; // 5% group discount
        return Math.round(base * 100.0) / 100.0;
    }
    
    private synchronized void expireHolds(Event event) {
        LocalDateTime now = LocalDateTime.now();
        List<String> expired = new ArrayList<>();
        
        for (Hold hold : event.holds.values()) {
            if (now.isAfter(hold.expiresAt)) {
                expired.add(hold.id);
                for (int seatNum : hold.seatNumbers) {
                    event.seats.get(seatNum).status = SeatStatus.AVAILABLE;
                }
            }
        }
        
        expired.forEach(event.holds::remove);
    }
    
    private List<Integer> getAvailableSeats(Event event, int quantity) {
        List<Integer> result = new ArrayList<>();
        for (Seat seat : event.seats.values()) {
            if (seat.status == SeatStatus.AVAILABLE) {
                result.add(seat.number);
                if (result.size() == quantity) break;
            }
        }
        return result;
    }
    
    private synchronized void processWaitList(Event event) {
        while (!event.waitList.isEmpty()) {
            WaitListEntry entry = event.waitList.peek();
            if (event.countAvailableSeats() >= entry.quantity) {
                event.waitList.poll();
            } else {
                break;
            }
        }
    }
    
    // Demo
    public static void main(String[] args) throws InterruptedException {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        
        LocalDateTime eventDate = LocalDateTime.now().plusDays(45);
        String eventId = engine.createEvent("CONCERT_2026", eventDate, 100);
        
        System.out.println("=== Single booking ===");
        String hold1 = engine.holdSeats(eventId, TicketType.ADULT, 5);
        System.out.println("Hold created: " + hold1);
        String booking1 = engine.confirmBooking(eventId, hold1);
        System.out.println("Booking: " + booking1 + ", Price: €" + engine.getTicketPrice(TicketType.ADULT, 5));
        System.out.println("Available seats: " + engine.getAvailableSeatsCount(eventId));
        
        System.out.println("\n=== Group booking with discount ===");
        String hold2 = engine.holdSeats(eventId, TicketType.STUDENT, 15);
        String booking2 = engine.confirmBooking(eventId, hold2);
        System.out.println("Booking: " + booking2 + ", Price: €" + engine.getTicketPrice(TicketType.STUDENT, 15) + " (5% discount applied)");
        System.out.println("Available seats: " + engine.getAvailableSeatsCount(eventId));
        
        System.out.println("\n=== Cancellation refund (45 days before) ===");
        double refund = engine.cancelBooking(eventId, booking1);
        System.out.println("Booking " + booking1 + " cancelled, refund: €" + refund + " (100%)");
        System.out.println("Available seats: " + engine.getAvailableSeatsCount(eventId));
        
        System.out.println("\n=== Fill event and wait list ===");
        for (int i = 0; i < 6; i++) {
            String hold = engine.holdSeats(eventId, TicketType.CHILD, 15);
            if (hold != null) {
                engine.confirmBooking(eventId, hold);
            }
        }
        System.out.println("Available seats: " + engine.getAvailableSeatsCount(eventId));
        
        String waitId = engine.addToWaitList(eventId, TicketType.SENIOR, 10);
        System.out.println("Added to wait list: " + (waitId != null ? waitId : "Event sold out"));
    }
}
