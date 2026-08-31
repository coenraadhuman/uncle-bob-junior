import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class EventSeatBookingEngine {
    
    public enum TicketType {
        ADULT(100.0),
        CHILD(50.0),
        SENIOR(75.0),
        STUDENT(60.0);
        
        private final double basePrice;
        
        TicketType(double basePrice) {
            this.basePrice = basePrice;
        }
        
        public double getBasePrice() {
            return basePrice;
        }
    }
    
    private enum SeatStatus {
        AVAILABLE, HELD, BOOKED
    }
    
    public static class Event {
        private final String eventId;
        private final LocalDateTime eventDateTime;
        private final int totalSeats;
        private final Map<Integer, Seat> seats;
        private final Map<String, Hold> holds;
        private final Map<String, Booking> bookings;
        private final Queue<WaitlistEntry> waitlist;
        private final ScheduledExecutorService scheduler;
        
        public Event(String eventId, LocalDateTime eventDateTime, int totalSeats) {
            this.eventId = eventId;
            this.eventDateTime = eventDateTime;
            this.totalSeats = totalSeats;
            this.seats = new ConcurrentHashMap<>();
            this.holds = new ConcurrentHashMap<>();
            this.bookings = new ConcurrentHashMap<>();
            this.waitlist = new ConcurrentLinkedQueue<>();
            this.scheduler = Executors.newScheduledThreadPool(1);
            
            for (int i = 1; i <= totalSeats; i++) {
                seats.put(i, new Seat(i));
            }
        }
        
        public void shutdown() {
            scheduler.shutdown();
        }
    }
    
    private static class Seat {
        private final int seatNumber;
        private SeatStatus status;
        
        Seat(int seatNumber) {
            this.seatNumber = seatNumber;
            this.status = SeatStatus.AVAILABLE;
        }
    }
    
    public static class Hold {
        private final String holdId;
        private final List<Integer> seatNumbers;
        private final TicketType ticketType;
        private final int quantity;
        private final LocalDateTime expiresAt;
        private final double totalPrice;
        private HoldStatus status;
        
        public enum HoldStatus {
            ACTIVE, EXPIRED, CONFIRMED, RELEASED
        }
        
        Hold(String holdId, List<Integer> seatNumbers, TicketType ticketType, int quantity, double totalPrice) {
            this.holdId = holdId;
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.ticketType = ticketType;
            this.quantity = quantity;
            this.expiresAt = LocalDateTime.now().plusMinutes(15);
            this.totalPrice = totalPrice;
            this.status = HoldStatus.ACTIVE;
        }
        
        public String getHoldId() {
            return holdId;
        }
        
        public List<Integer> getSeatNumbers() {
            return new ArrayList<>(seatNumbers);
        }
        
        public double getTotalPrice() {
            return totalPrice;
        }
        
        public HoldStatus getStatus() {
            return status;
        }
    }
    
    public static class Booking {
        private final String bookingId;
        private final List<Integer> seatNumbers;
        private final TicketType ticketType;
        private final int quantity;
        private final double totalPrice;
        private final LocalDateTime bookedAt;
        
        Booking(String bookingId, List<Integer> seatNumbers, TicketType ticketType, int quantity, double totalPrice) {
            this.bookingId = bookingId;
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.ticketType = ticketType;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.bookedAt = LocalDateTime.now();
        }
        
        public String getBookingId() {
            return bookingId;
        }
        
        public List<Integer> getSeatNumbers() {
            return new ArrayList<>(seatNumbers);
        }
        
        public double getTotalPrice() {
            return totalPrice;
        }
        
        public LocalDateTime getBookedAt() {
            return bookedAt;
        }
    }
    
    private static class WaitlistEntry {
        private final String waitlistId;
        private final int quantity;
        private final TicketType ticketType;
        
        WaitlistEntry(String waitlistId, int quantity, TicketType ticketType) {
            this.waitlistId = waitlistId;
            this.quantity = quantity;
            this.ticketType = ticketType;
        }
    }
    
    public static class RefundInfo {
        private final double originalPrice;
        private final double refundAmount;
        private final double refundPercentage;
        
        RefundInfo(double originalPrice, double refundAmount, double refundPercentage) {
            this.originalPrice = originalPrice;
            this.refundAmount = refundAmount;
            this.refundPercentage = refundPercentage;
        }
        
        public double getOriginalPrice() {
            return originalPrice;
        }
        
        public double getRefundAmount() {
            return refundAmount;
        }
        
        public double getRefundPercentage() {
            return refundPercentage;
        }
    }
    
    public static class BookingEngine {
        private final Map<String, Event> events = new ConcurrentHashMap<>();
        private static final int GROUP_DISCOUNT_THRESHOLD = 10;
        private static final double GROUP_DISCOUNT_RATE = 0.05;
        
        public void addEvent(Event event) {
            events.put(event.eventId, event);
        }
        
        public String holdSeats(String eventId, int quantity, TicketType ticketType) throws Exception {
            Event event = getEvent(eventId);
            
            List<Integer> availableSeats = findAvailableSeats(event, quantity);
            
            if (availableSeats.isEmpty()) {
                String waitlistId = UUID.randomUUID().toString();
                event.waitlist.offer(new WaitlistEntry(waitlistId, quantity, ticketType));
                throw new IllegalStateException("Event sold out. Waitlist ID: " + waitlistId);
            }
            
            double totalPrice = calculatePrice(ticketType.getBasePrice() * quantity, quantity);
            
            String holdId = UUID.randomUUID().toString();
            Hold hold = new Hold(holdId, availableSeats, ticketType, quantity, totalPrice);
            
            for (int seatNumber : availableSeats) {
                event.seats.get(seatNumber).status = SeatStatus.HELD;
            }
            
            event.holds.put(holdId, hold);
            event.scheduler.schedule(() -> expireHold(eventId, holdId), 15, TimeUnit.MINUTES);
            
            return holdId;
        }
        
        public String confirmHold(String eventId, String holdId) throws Exception {
            Event event = getEvent(eventId);
            Hold hold = event.holds.get(holdId);
            
            if (hold == null) {
                throw new IllegalArgumentException("Hold not found: " + holdId);
            }
            
            if (LocalDateTime.now().isAfter(hold.expiresAt)) {
                hold.status = Hold.HoldStatus.EXPIRED;
                throw new IllegalStateException("Hold expired");
            }
            
            String bookingId = UUID.randomUUID().toString();
            Booking booking = new Booking(bookingId, hold.seatNumbers, hold.ticketType, hold.quantity, hold.totalPrice);
            
            for (int seatNumber : hold.seatNumbers) {
                event.seats.get(seatNumber).status = SeatStatus.BOOKED;
            }
            
            event.bookings.put(bookingId, booking);
            hold.status = Hold.HoldStatus.CONFIRMED;
            
            return bookingId;
        }
        
        public void releaseHold(String eventId, String holdId) throws Exception {
            Event event = getEvent(eventId);
            Hold hold = event.holds.get(holdId);
            
            if (hold == null) {
                throw new IllegalArgumentException("Hold not found: " + holdId);
            }
            
            for (int seatNumber : hold.seatNumbers) {
                event.seats.get(seatNumber).status = SeatStatus.AVAILABLE;
            }
            
            hold.status = Hold.HoldStatus.RELEASED;
            event.holds.remove(holdId);
            processWaitlist(eventId);
        }
        
        public RefundInfo cancelBooking(String eventId, String bookingId) throws Exception {
            Event event = getEvent(eventId);
            Booking booking = event.bookings.get(bookingId);
            
            if (booking == null) {
                throw new IllegalArgumentException("Booking not found: " + bookingId);
            }
            
            long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), event.eventDateTime);
            double refundPercentage = 0.0;
            
            if (daysUntilEvent > 30) {
                refundPercentage = 1.0;
            } else if (daysUntilEvent > 7) {
                refundPercentage = 0.5;
            }
            
            double refundAmount = booking.totalPrice * refundPercentage;
            
            for (int seatNumber : booking.seatNumbers) {
                event.seats.get(seatNumber).status = SeatStatus.AVAILABLE;
            }
            
            event.bookings.remove(bookingId);
            processWaitlist(eventId);
            
            return new RefundInfo(booking.totalPrice, refundAmount, refundPercentage);
        }
        
        public int getAvailableSeats(String eventId) {
            Event event = getEvent(eventId);
            return (int) event.seats.values().stream()
                    .filter(s -> s.status == SeatStatus.AVAILABLE)
                    .count();
        }
        
        public int getWaitlistSize(String eventId) {
            return getEvent(eventId).waitlist.size();
        }
        
        private Event getEvent(String eventId) {
            Event event = events.get(eventId);
            if (event == null) {
                throw new IllegalArgumentException("Event not found: " + eventId);
            }
            return event;
        }
        
        private List<Integer> findAvailableSeats(Event event, int quantity) {
            List<Integer> available = new ArrayList<>();
            for (Seat seat : event.seats.values()) {
                if (seat.status == SeatStatus.AVAILABLE) {
                    available.add(seat.seatNumber);
                    if (available.size() == quantity) {
                        return available;
                    }
                }
            }
            return Collections.emptyList();
        }
        
        private double calculatePrice(double baseTotal, int quantity) {
            if (quantity >= GROUP_DISCOUNT_THRESHOLD) {
                return baseTotal * (1 - GROUP_DISCOUNT_RATE);
            }
            return baseTotal;
        }
        
        private void expireHold(String eventId, String holdId) {
            try {
                Event event = events.get(eventId);
                if (event != null) {
                    Hold hold = event.holds.get(holdId);
                    if (hold != null && hold.status == Hold.HoldStatus.ACTIVE) {
                        releaseHold(eventId, holdId);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error expiring hold: " + e.getMessage());
            }
        }
        
        private void processWaitlist(String eventId) throws Exception {
            Event event = events.get(eventId);
            if (event == null) return;
            
            while (!event.waitlist.isEmpty()) {
                WaitlistEntry entry = event.waitlist.peek();
                List<Integer> availableSeats = findAvailableSeats(event, entry.quantity);
                
                if (availableSeats.isEmpty()) break;
                
                event.waitlist.poll();
                double totalPrice = calculatePrice(entry.ticketType.getBasePrice() * entry.quantity, entry.quantity);
                
                String holdId = UUID.randomUUID().toString();
                Hold hold = new Hold(holdId, availableSeats, entry.ticketType, entry.quantity, totalPrice);
                
                for (int seatNumber : availableSeats) {
                    event.seats.get(seatNumber).status = SeatStatus.HELD;
                }
                
                event.holds.put(holdId, hold);
                event.scheduler.schedule(() -> expireHold(eventId, holdId), 15, TimeUnit.MINUTES);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        BookingEngine engine = new BookingEngine();
        
        Event concert = new Event("EVT001", LocalDateTime.now().plusDays(45), 100);
        engine.addEvent(concert);
        
        String hold1 = engine.holdSeats("EVT001", 5, TicketType.ADULT);
        String booking1 = engine.confirmHold("EVT001", hold1);
        System.out.println("Booking: " + booking1 + ", Available: " + engine.getAvailableSeats("EVT001"));
        
        String hold2 = engine.holdSeats("EVT001", 10, TicketType.STUDENT);
        String booking2 = engine.confirmHold("EVT001", hold2);
        System.out.println("Group booking: " + booking2 + " (5% discount applied)");
        
        RefundInfo refund = engine.cancelBooking("EVT001", booking1);
        System.out.println("Refund: " + refund.getRefundAmount() + " of " + refund.getOriginalPrice());
        System.out.println("Available after cancel: " + engine.getAvailableSeats("EVT001"));
        
        concert.shutdown();
    }
}
