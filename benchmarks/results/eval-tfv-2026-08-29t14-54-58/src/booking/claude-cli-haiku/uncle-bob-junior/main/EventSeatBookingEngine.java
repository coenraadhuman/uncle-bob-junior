import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class EventSeatBookingEngine {
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_PERCENT = 5.0;
    private static final int FULL_REFUND_DAYS = 30;
    private static final int PARTIAL_REFUND_DAYS = 7;
    private static final double PARTIAL_REFUND_PERCENT = 50.0;
    
    private final ConcurrentHashMap<String, Event> events;
    
    public EventSeatBookingEngine() {
        this.events = new ConcurrentHashMap<>();
    }
    
    public String createEvent(String eventId, String name, LocalDateTime date, List<Seat> seats) {
        events.putIfAbsent(eventId, new Event(eventId, name, date, new ArrayList<>(seats)));
        return eventId;
    }
    
    public String holdSeats(String eventId, String email, List<Integer> seatIndices) {
        Event event = events.get(eventId);
        if (event == null) return null;
        
        List<Seat> selectedSeats = findAvailableSeats(event, seatIndices);
        
        if (selectedSeats.isEmpty()) {
            String entryId = generateId("WAITLIST");
            addToWaitlist(event, entryId, email, seatIndices.size());
            return null;
        }
        
        String holdId = generateId("HOLD");
        SeatHold hold = new SeatHold(holdId, email, selectedSeats);
        event.getHolds().put(holdId, hold);
        selectedSeats.forEach(s -> s.setStatus(Seat.SeatStatus.HELD));
        
        return holdId;
    }
    
    public String confirmBooking(String eventId, String holdId, List<TicketType> ticketTypes) {
        Event event = events.get(eventId);
        if (event == null) return null;
        
        SeatHold hold = event.getHolds().get(holdId);
        if (hold == null || hold.isExpired()) {
            return null;
        }
        
        List<Seat> seats = hold.getSeats();
        seats.forEach(s -> s.setStatus(Seat.SeatStatus.BOOKED));
        
        double totalPrice = calculatePrice(ticketTypes, seats.size());
        String bookingId = generateId("BOOKING");
        Booking booking = new Booking(bookingId, hold.getEmail(), seats, ticketTypes, totalPrice, event.getDate());
        event.getBookings().add(booking);
        event.getHolds().remove(holdId);
        
        processWaitlist(event);
        
        return bookingId;
    }
    
    public void releaseHold(String eventId, String holdId) {
        Event event = events.get(eventId);
        if (event == null) return;
        
        SeatHold hold = event.getHolds().remove(holdId);
        if (hold != null) {
            hold.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.AVAILABLE));
            processWaitlist(event);
        }
    }
    
    public double cancelBooking(String eventId, String bookingId) {
        Event event = events.get(eventId);
        if (event == null) return 0;
        
        Booking booking = event.getBookings().stream()
            .filter(b -> b.getBookingId().equals(bookingId))
            .findFirst()
            .orElse(null);
        
        if (booking == null) return 0;
        
        event.getBookings().remove(booking);
        booking.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.AVAILABLE));
        
        double refund = calculateRefund(booking.getTotalPrice(), booking.getEventDate());
        processWaitlist(event);
        
        return refund;
    }
    
    public int getAvailableSeats(String eventId) {
        Event event = events.get(eventId);
        return event != null ? event.countAvailableSeats() : 0;
    }
    
    public List<Booking> getEventBookings(String eventId) {
        Event event = events.get(eventId);
        return event != null ? new ArrayList<>(event.getBookings()) : List.of();
    }
    
    private List<Seat> findAvailableSeats(Event event, List<Integer> seatIndices) {
        List<Seat> available = new ArrayList<>();
        
        for (int index : seatIndices) {
            if (index >= 0 && index < event.getSeats().size()) {
                Seat seat = event.getSeats().get(index);
                if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
                    available.add(seat);
                }
            }
        }
        
        return available.size() == seatIndices.size() ? available : List.of();
    }
    
    private double calculatePrice(List<TicketType> ticketTypes, int seatCount) {
        double basePrice = ticketTypes.stream()
            .mapToDouble(TicketType::getPriceEur)
            .sum();
        
        if (seatCount >= GROUP_DISCOUNT_THRESHOLD) {
            basePrice *= (1 - GROUP_DISCOUNT_PERCENT / 100.0);
        }
        
        return basePrice;
    }
    
    private double calculateRefund(double bookingPrice, LocalDateTime eventDate) {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDate);
        
        if (daysUntilEvent > FULL_REFUND_DAYS) {
            return bookingPrice;
        } else if (daysUntilEvent > PARTIAL_REFUND_DAYS) {
            return bookingPrice * (PARTIAL_REFUND_PERCENT / 100.0);
        }
        return 0;
    }
    
    private void addToWaitlist(Event event, String entryId, String email, int seatCount) {
        List<TicketType> defaultTypes = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            defaultTypes.add(TicketType.ADULT);
        }
        WaitlistEntry entry = new WaitlistEntry(entryId, email, seatCount, defaultTypes);
        event.getWaitlist().offer(entry);
    }
    
    private void processWaitlist(Event event) {
        while (!event.getWaitlist().isEmpty() && event.countAvailableSeats() > 0) {
            WaitlistEntry entry = event.getWaitlist().peek();
            List<Integer> availableIndices = findAvailableSeatIndices(event, entry.getSeatCount());
            
            if (availableIndices.size() >= entry.getSeatCount()) {
                event.getWaitlist().poll();
                holdSeats(event.getEventId(), entry.getEmail(),
                    availableIndices.subList(0, entry.getSeatCount()));
            } else {
                break;
            }
        }
    }
    
    private List<Integer> findAvailableSeatIndices(Event event, int count) {
        List<Integer> indices = new ArrayList<>();
        List<Seat> seats = event.getSeats();
        
        for (int i = 0; i < seats.size() && indices.size() < count; i++) {
            if (seats.get(i).getStatus() == Seat.SeatStatus.AVAILABLE) {
                indices.add(i);
            }
        }
        
        return indices;
    }
    
    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
