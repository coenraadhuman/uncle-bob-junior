import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class EventSeatBookingEngine {
    private final Map<String, Seat> seats = new ConcurrentHashMap<>();
    private final Map<String, Hold> holds = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final Queue<WaitlistEntry> waitlist = new ConcurrentLinkedQueue<>();
    private final LocalDateTime eventDate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public EventSeatBookingEngine(int totalSeats, LocalDateTime eventDate) {
        this.eventDate = eventDate;
        for (int i = 1; i <= totalSeats; i++) {
            seats.put("S" + i, new Seat("S" + i));
        }
        scheduler.scheduleAtFixedRate(this::expireOldHolds, 1, 1, TimeUnit.MINUTES);
    }
    
    private void expireOldHolds() {
        LocalDateTime now = LocalDateTime.now();
        holds.values().stream()
            .filter(h -> h.isExpired(now) && !h.isConfirmed())
            .map(Hold::getBookingId)
            .collect(Collectors.toList())
            .forEach(this::releaseHold);
    }
    
    public synchronized String holdSeats(Map<TicketType, Integer> ticketQuantities) {
        int totalNeeded = ticketQuantities.values().stream().mapToInt(Integer::intValue).sum();
        
        List<String> available = seats.values().stream()
            .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
            .map(Seat::getId)
            .limit(totalNeeded)
            .collect(Collectors.toList());
        
        if (available.size() < totalNeeded) {
            return null;
        }
        
        String bookingId = UUID.randomUUID().toString();
        Hold hold = new Hold(bookingId, available, LocalDateTime.now());
        available.forEach(id -> seats.get(id).setStatus(SeatStatus.HELD));
        holds.put(bookingId, hold);
        
        return bookingId;
    }
    
    public synchronized boolean confirmHold(String bookingId, Map<TicketType, Integer> ticketQuantities) {
        Hold hold = holds.get(bookingId);
        if (hold == null || hold.isExpired(LocalDateTime.now())) {
            return false;
        }
        
        int total = ticketQuantities.values().stream().mapToInt(Integer::intValue).sum();
        double price = calculatePrice(ticketQuantities, total >= 10);
        
        Booking booking = new Booking(bookingId, hold.getSeatIds(), price, LocalDateTime.now(), eventDate);
        hold.getSeatIds().forEach(id -> seats.get(id).setStatus(SeatStatus.BOOKED));
        
        hold.confirm();
        bookings.put(bookingId, booking);
        processWaitlist();
        
        return true;
    }
    
    public synchronized void releaseHold(String bookingId) {
        Hold hold = holds.remove(bookingId);
        if (hold != null) {
            hold.getSeatIds().forEach(id -> {
                Seat seat = seats.get(id);
                if (seat.getStatus() == SeatStatus.HELD) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                }
            });
            processWaitlist();
        }
    }
    
    public synchronized double cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.isCancelled()) {
            return 0;
        }
        
        booking.cancel();
        booking.getSeatIds().forEach(id -> seats.get(id).setStatus(SeatStatus.AVAILABLE));
        double refund = booking.calculateRefund(LocalDateTime.now());
        processWaitlist();
        
        return refund;
    }
    
    public synchronized void addToWaitlist(String customerId, Map<TicketType, Integer> ticketQuantities) {
        waitlist.offer(new WaitlistEntry(customerId, ticketQuantities, LocalDateTime.now()));
    }
    
    private synchronized void processWaitlist() {
        WaitlistEntry entry;
        while ((entry = waitlist.peek()) != null) {
            String bookingId = holdSeats(entry.getTicketQuantities());
            if (bookingId != null && confirmHold(bookingId, entry.getTicketQuantities())) {
                waitlist.poll();
            } else {
                if (bookingId != null) {
                    releaseHold(bookingId);
                }
                break;
            }
        }
    }
    
    private double calculatePrice(Map<TicketType, Integer> ticketQuantities, boolean groupDiscount) {
        double total = ticketQuantities.entrySet().stream()
            .mapToDouble(e -> e.getKey().getBasePrice() * e.getValue())
            .sum();
        return groupDiscount ? total * 0.95 : total;
    }
    
    public int getAvailableSeats() {
        return (int) seats.values().stream()
            .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
            .count();
    }
    
    public int getWaitlistSize() {
        return waitlist.size();
    }
    
    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
    
    public static void main(String[] args) throws InterruptedException {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        EventSeatBookingEngine engine = new EventSeatBookingEngine(100, eventDate);
        
        Map<TicketType, Integer> tickets = Map.of(
            TicketType.ADULT, 2,
            TicketType.CHILD, 1
        );
        
        String holdId = engine.holdSeats(tickets);
        System.out.println("Hold created: " + holdId);
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        if (engine.confirmHold(holdId, tickets)) {
            Booking booking = engine.getBooking(holdId);
            System.out.println("Booking confirmed. Total: €" + booking.getTotalPrice());
            System.out.println("Seats booked: " + booking.getSeatIds());
        }
        
        Map<TicketType, Integer> groupTickets = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            groupTickets.put(TicketType.ADULT, 10);
        }
        String groupHold = engine.holdSeats(groupTickets);
        if (groupHold != null && engine.confirmHold(groupHold, groupTickets)) {
            Booking groupBooking = engine.getBooking(groupHold);
            System.out.println("\nGroup booking (10+ seats): €" + groupBooking.getTotalPrice() + " (5% discount applied)");
        }
        
        double refund = engine.cancelBooking(holdId);
        System.out.println("\nCancellation refund: €" + refund);
        System.out.println("Available seats after cancellation: " + engine.getAvailableSeats());
        
        engine.shutdown();
    }
}
