import java.time.*;
import java.util.*;
import java.util.concurrent.*;

class EventSeatBookingEngine {
    private static final double GROUP_DISCOUNT = 0.95;
    private static final int GROUP_THRESHOLD = 10;
    private static final int HOLD_DURATION_SECONDS = 900;
    
    private final Map<String, Event> events;
    private final Map<String, SeatHold> activeHolds;
    private final Map<String, Booking> bookings;
    private final Map<String, Queue<WaitListEntry>> waitLists;
    private final Map<String, Integer> availableSeats;
    
    EventSeatBookingEngine() {
        this.events = new ConcurrentHashMap<>();
        this.activeHolds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitLists = new ConcurrentHashMap<>();
        this.availableSeats = new ConcurrentHashMap<>();
    }
    
    void addEvent(Event event) {
        events.put(event.eventId(), event);
        availableSeats.put(event.eventId(), event.totalSeats());
        waitLists.put(event.eventId(), new ConcurrentLinkedQueue<>());
    }
    
    BookingResult holdSeats(String eventId, Map<TicketTier, Integer> tierCounts) {
        validateTierCounts(tierCounts);
        Event event = events.get(eventId);
        if (event == null) {
            return new BookingResult.Failure("Event not found");
        }
        
        int requestedSeats = tierCounts.values().stream().mapToInt(Integer::intValue).sum();
        cleanExpiredHolds(eventId);
        
        if (availableSeats.get(eventId) < requestedSeats) {
            addToWaitList(eventId, requestedSeats, tierCounts);
            return new BookingResult.Failure("No available seats, added to waiting list");
        }
        
        List<Ticket> tickets = createTickets(event, tierCounts);
        String holdId = generateId("hold");
        SeatHold hold = new SeatHold(holdId, tickets, Instant.now());
        
        activeHolds.put(holdId, hold);
        availableSeats.put(eventId, availableSeats.get(eventId) - requestedSeats);
        
        double totalPrice = calculateTotalPrice(tickets, requestedSeats);
        return new BookingResult.Success(holdId, totalPrice);
    }
    
    BookingResult confirmHold(String eventId, String holdId) {
        SeatHold hold = activeHolds.get(holdId);
        if (hold == null) {
            return new BookingResult.Failure("Hold not found");
        }
        
        if (hold.isExpired(Instant.now())) {
            activeHolds.remove(holdId);
            availableSeats.put(eventId, availableSeats.get(eventId) + hold.seatCount());
            return new BookingResult.Failure("Hold has expired");
        }
        
        Event event = events.get(eventId);
        String bookingId = generateId("book");
        double totalPrice = calculateTotalPrice(hold.tickets(), hold.seatCount());
        Booking booking = new Booking(bookingId, eventId, hold.tickets(), 
                                      event.eventDate(), totalPrice);
        
        bookings.put(bookingId, booking);
        activeHolds.remove(holdId);
        return new BookingResult.Success(bookingId, totalPrice);
    }
    
    void releaseHold(String eventId, String holdId) {
        SeatHold hold = activeHolds.remove(holdId);
        if (hold != null) {
            availableSeats.put(eventId, availableSeats.get(eventId) + hold.seatCount());
            processWaitList(eventId);
        }
    }
    
    BookingResult cancelBooking(String bookingId) {
        Booking booking = bookings.remove(bookingId);
        if (booking == null) {
            return new BookingResult.Failure("Booking not found");
        }
        
        double refundAmount = booking.refundAmount(LocalDate.now());
        String eventId = booking.eventId();
        availableSeats.put(eventId, availableSeats.get(eventId) + booking.seatCount());
        processWaitList(eventId);
        
        return new BookingResult.Success(bookingId, refundAmount);
    }
    
    Optional<Booking> getBooking(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }
    
    private void cleanExpiredHolds(String eventId) {
        Instant now = Instant.now();
        List<String> expiredHoldIds = activeHolds.entrySet().stream()
            .filter(e -> e.getValue().isExpired(now))
            .map(Map.Entry::getKey)
            .toList();
        
        for (String holdId : expiredHoldIds) {
            releaseHold(eventId, holdId);
        }
    }
    
    private void processWaitList(String eventId) {
        Queue<WaitListEntry> waitList = waitLists.get(eventId);
        if (waitList == null || waitList.isEmpty()) {
            return;
        }
        
        while (!waitList.isEmpty()) {
            WaitListEntry entry = waitList.peek();
            if (availableSeats.get(eventId) >= entry.seatCount()) {
                waitList.poll();
                bookWaitListEntry(eventId, entry);
            } else {
                break;
            }
        }
    }
    
    private void bookWaitListEntry(String eventId, WaitListEntry entry) {
        Event event = events.get(eventId);
        List<Ticket> tickets = createTickets(event, entry.tierCounts());
        
        String bookingId = generateId("book");
        double totalPrice = calculateTotalPrice(tickets, entry.seatCount());
        Booking booking = new Booking(bookingId, eventId, tickets, 
                                      event.eventDate(), totalPrice);
        
        bookings.put(bookingId, booking);
        availableSeats.put(eventId, availableSeats.get(eventId) - entry.seatCount());
    }
    
    private void addToWaitList(String eventId, int seatCount, Map<TicketTier, Integer> tierCounts) {
        WaitListEntry entry = new WaitListEntry(seatCount, tierCounts);
        waitLists.get(eventId).offer(entry);
    }
    
    private List<Ticket> createTickets(Event event, Map<TicketTier, Integer> tierCounts) {
        List<Ticket> tickets = new ArrayList<>();
        for (Map.Entry<TicketTier, Integer> entry : tierCounts.entrySet()) {
            double price = event.priceFor(entry.getKey());
            for (int i = 0; i < entry.getValue(); i++) {
                tickets.add(new Ticket(entry.getKey(), price));
            }
        }
        return tickets;
    }
    
    private double calculateTotalPrice(List<Ticket> tickets, int seatCount) {
        double subtotal = tickets.stream().mapToDouble(Ticket::price).sum();
        if (seatCount >= GROUP_THRESHOLD) {
            return subtotal * GROUP_DISCOUNT;
        }
        return subtotal;
    }
    
    private void validateTierCounts(Map<TicketTier, Integer> tierCounts) {
        for (Integer count : tierCounts.values()) {
            if (count <= 0) {
                throw new IllegalArgumentException("Ticket counts must be positive");
            }
        }
    }
    
    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    int availableSeatsFor(String eventId) {
        return availableSeats.getOrDefault(eventId, 0);
    }
    
    int waitListSizeFor(String eventId) {
        Queue<WaitListEntry> waitList = waitLists.get(eventId);
        return waitList != null ? waitList.size() : 0;
    }
}
