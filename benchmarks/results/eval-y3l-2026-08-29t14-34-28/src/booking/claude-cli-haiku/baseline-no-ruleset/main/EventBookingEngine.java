import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EventBookingEngine {
    
    enum TicketType {
        ADULT(100.00), CHILD(50.00), SENIOR(75.00), STUDENT(60.00);
        
        private final double basePrice;
        TicketType(double basePrice) { this.basePrice = basePrice; }
        public double getBasePrice() { return basePrice; }
    }
    
    enum SeatStatus { AVAILABLE, HELD, BOOKED }
    enum HoldStatus { ACTIVE, CONFIRMED, EXPIRED }
    enum BookingStatus { CONFIRMED, CANCELLED }
    
    static class Seat {
        int number;
        SeatStatus status;
        Seat(int number) { this.number = number; this.status = SeatStatus.AVAILABLE; }
    }
    
    static class SeatHold {
        String holdId;
        String eventId;
        List<Integer> seatNumbers;
        LocalDateTime createdAt;
        HoldStatus status;
        
        SeatHold(String holdId, String eventId, List<Integer> seatNumbers) {
            this.holdId = holdId;
            this.eventId = eventId;
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.createdAt = LocalDateTime.now();
            this.status = HoldStatus.ACTIVE;
        }
        
        boolean isExpired() {
            return status == HoldStatus.ACTIVE && 
                   ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now()) >= 15;
        }
    }
    
    static class BookingRecord {
        String bookingId;
        String eventId;
        String customerEmail;
        Map<TicketType, Integer> ticketCounts;
        double totalPrice;
        BookingStatus status;
        LocalDateTime confirmedAt;
        LocalDateTime cancelledAt;
        List<Integer> seatNumbers;
        
        BookingRecord(String bookingId, String eventId, String customerEmail,
                     Map<TicketType, Integer> ticketCounts, double totalPrice, List<Integer> seatNumbers) {
            this.bookingId = bookingId;
            this.eventId = eventId;
            this.customerEmail = customerEmail;
            this.ticketCounts = new HashMap<>(ticketCounts);
            this.totalPrice = totalPrice;
            this.status = BookingStatus.CONFIRMED;
            this.confirmedAt = LocalDateTime.now();
            this.seatNumbers = new ArrayList<>(seatNumbers);
        }
        
        void cancel() {
            this.status = BookingStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
        }
    }
    
    static class WaitingListEntry {
        String customerId;
        String customerEmail;
        Map<TicketType, Integer> ticketCounts;
        int seatsRequested;
        
        WaitingListEntry(String customerEmail, Map<TicketType, Integer> ticketCounts) {
            this.customerId = UUID.randomUUID().toString();
            this.customerEmail = customerEmail;
            this.ticketCounts = new HashMap<>(ticketCounts);
            this.seatsRequested = ticketCounts.values().stream().mapToInt(Integer::intValue).sum();
        }
    }
    
    static class Event {
        String eventId;
        LocalDateTime eventDate;
        Map<Integer, Seat> seats;
        Map<String, SeatHold> holds;
        Map<String, BookingRecord> bookings;
        Queue<WaitingListEntry> waitingList;
        
        Event(String eventId, LocalDateTime eventDate, int totalSeats) {
            this.eventId = eventId;
            this.eventDate = eventDate;
            this.seats = new HashMap<>();
            this.holds = new HashMap<>();
            this.bookings = new HashMap<>();
            this.waitingList = new LinkedList<>();
            
            for (int i = 1; i <= totalSeats; i++) {
                seats.put(i, new Seat(i));
            }
        }
        
        void cleanupExpiredHolds() {
            holds.values().stream()
                .filter(h -> h.status == HoldStatus.ACTIVE && h.isExpired())
                .forEach(this::releaseHoldSeats);
        }
        
        void releaseHoldSeats(SeatHold hold) {
            hold.status = HoldStatus.EXPIRED;
            hold.seatNumbers.forEach(n -> {
                if (seats.get(n).status == SeatStatus.HELD) {
                    seats.get(n).status = SeatStatus.AVAILABLE;
                }
            });
        }
        
        int getAvailableSeats() {
            cleanupExpiredHolds();
            return (int) seats.values().stream()
                .filter(s -> s.status == SeatStatus.AVAILABLE)
                .count();
        }
    }
    
    private Map<String, Event> events = new HashMap<>();
    private Map<String, BookingRecord> allBookings = new HashMap<>();
    private Map<String, SeatHold> allHolds = new HashMap<>();
    private int bookingCounter = 0;
    private int holdCounter = 0;
    
    public void createEvent(String eventId, LocalDateTime eventDate, int totalSeats) {
        events.put(eventId, new Event(eventId, eventDate, totalSeats));
    }
    
    public SeatHold findAndHoldSeats(String eventId, int numSeats, String customerEmail,
                                    Map<TicketType, Integer> ticketCounts) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        
        event.cleanupExpiredHolds();
        
        List<Integer> available = event.seats.values().stream()
            .filter(s -> s.status == SeatStatus.AVAILABLE)
            .map(s -> s.number)
            .limit(numSeats)
            .collect(Collectors.toList());
        
        if (available.size() < numSeats) {
            event.waitingList.add(new WaitingListEntry(customerEmail, ticketCounts));
            return null;
        }
        
        String holdId = "HOLD_" + (++holdCounter);
        SeatHold hold = new SeatHold(holdId, eventId, available);
        available.forEach(n -> event.seats.get(n).status = SeatStatus.HELD);
        
        event.holds.put(holdId, hold);
        allHolds.put(holdId, hold);
        return hold;
    }
    
    public BookingRecord reserveSeats(String eventId, String holdId, String customerEmail,
                                     Map<TicketType, Integer> ticketCounts) {
        Event event = events.get(eventId);
        SeatHold hold = allHolds.get(holdId);
        
        if (event == null || hold == null || !hold.eventId.equals(eventId)) 
            throw new IllegalArgumentException("Invalid event or hold");
        if (hold.status != HoldStatus.ACTIVE || hold.isExpired()) 
            throw new IllegalArgumentException("Hold expired or inactive");
        
        double totalPrice = calculatePrice(ticketCounts, hold.seatNumbers.size());
        
        String bookingId = "BK_" + (++bookingCounter);
        BookingRecord booking = new BookingRecord(bookingId, eventId, customerEmail, 
                                                  ticketCounts, totalPrice, hold.seatNumbers);
        
        hold.status = HoldStatus.CONFIRMED;
        hold.seatNumbers.forEach(n -> event.seats.get(n).status = SeatStatus.BOOKED);
        
        event.bookings.put(bookingId, booking);
        allBookings.put(bookingId, booking);
        
        processWaitingList(eventId);
        return booking;
    }
    
    public double cancelBooking(String bookingId) {
        BookingRecord booking = allBookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found");
        if (booking.status == BookingStatus.CANCELLED) 
            throw new IllegalArgumentException("Booking already cancelled");
        
        Event event = events.get(booking.eventId);
        long daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), event.eventDate);
        
        double refund = (daysUntil > 30) ? booking.totalPrice : 
                       (daysUntil > 7) ? booking.totalPrice * 0.5 : 0;
        
        booking.cancel();
        booking.seatNumbers.forEach(n -> {
            if (event.seats.get(n).status == SeatStatus.BOOKED) {
                event.seats.get(n).status = SeatStatus.AVAILABLE;
            }
        });
        
        processWaitingList(booking.eventId);
        return refund;
    }
    
    private void processWaitingList(String eventId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        while (!event.waitingList.isEmpty()) {
            WaitingListEntry entry = event.waitingList.peek();
            if (event.getAvailableSeats() >= entry.seatsRequested) {
                event.waitingList.poll();
                SeatHold hold = findAndHoldSeats(eventId, entry.seatsRequested, 
                                                 entry.customerEmail, entry.ticketCounts);
                if (hold != null) {
                    reserveSeats(eventId, hold.holdId, entry.customerEmail, entry.ticketCounts);
                }
            } else {
                break;
            }
        }
    }
    
    public int getWaitingListPosition(String eventId, String customerEmail) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        
        int pos = 0;
        for (WaitingListEntry e : event.waitingList) {
            if (e.customerEmail.equals(customerEmail)) return pos + 1;
            pos++;
        }
        return -1;
    }
    
    public int getAvailableSeats(String eventId) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found");
        return event.getAvailableSeats();
    }
    
    public BookingRecord getBooking(String bookingId) {
        return allBookings.get(bookingId);
    }
    
    private double calculatePrice(Map<TicketType, Integer> ticketCounts, int totalSeats) {
        double price = ticketCounts.entrySet().stream()
            .mapToDouble(e -> e.getKey().getBasePrice() * e.getValue())
            .sum();
        if (totalSeats >= 10) price *= 0.95;
        return price;
    }
}
