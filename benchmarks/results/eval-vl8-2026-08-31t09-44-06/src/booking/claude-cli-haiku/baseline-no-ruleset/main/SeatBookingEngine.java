import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SeatBookingEngine {
    
    enum TicketType {
        ADULT(10000), CHILD(5000), SENIOR(7500), STUDENT(6000);
        
        private final int basePrice;
        
        TicketType(int basePrice) {
            this.basePrice = basePrice;
        }
        
        public int getBasePrice() {
            return basePrice;
        }
    }
    
    static class Ticket {
        private final TicketType type;
        private final int price;
        
        Ticket(TicketType type, int price) {
            this.type = type;
            this.price = price;
        }
        
        public TicketType getType() {
            return type;
        }
        
        public int getPrice() {
            return price;
        }
    }
    
    static class SeatHold {
        private final String holdId;
        private final String eventId;
        private final int seatId;
        private final Ticket ticket;
        private final LocalDateTime expiresAt;
        
        SeatHold(String holdId, String eventId, int seatId, Ticket ticket, LocalDateTime expiresAt) {
            this.holdId = holdId;
            this.eventId = eventId;
            this.seatId = seatId;
            this.ticket = ticket;
            this.expiresAt = expiresAt;
        }
        
        public String getHoldId() { return holdId; }
        public String getEventId() { return eventId; }
        public int getSeatId() { return seatId; }
        public Ticket getTicket() { return ticket; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    }
    
    static class Booking {
        private final String bookingId;
        private final String eventId;
        private final int seatId;
        private final Ticket ticket;
        private final LocalDateTime bookedAt;
        private BookingStatus status;
        
        enum BookingStatus { CONFIRMED, CANCELLED }
        
        Booking(String bookingId, String eventId, int seatId, Ticket ticket) {
            this.bookingId = bookingId;
            this.eventId = eventId;
            this.seatId = seatId;
            this.ticket = ticket;
            this.bookedAt = LocalDateTime.now();
            this.status = BookingStatus.CONFIRMED;
        }
        
        public String getBookingId() { return bookingId; }
        public String getEventId() { return eventId; }
        public int getSeatId() { return seatId; }
        public Ticket getTicket() { return ticket; }
        public BookingStatus getStatus() { return status; }
        public void cancel() { this.status = BookingStatus.CANCELLED; }
    }
    
    static class WaitlistEntry {
        private final String entryId;
        private final List<TicketType> ticketTypes;
        private final LocalDateTime createdAt;
        
        WaitlistEntry(String entryId, List<TicketType> ticketTypes) {
            this.entryId = entryId;
            this.ticketTypes = new ArrayList<>(ticketTypes);
            this.createdAt = LocalDateTime.now();
        }
        
        public String getEntryId() { return entryId; }
        public List<TicketType> getTicketTypes() { return ticketTypes; }
    }
    
    static class Event {
        private final String eventId;
        private final int capacity;
        private final LocalDateTime eventDate;
        
        Event(String eventId, int capacity, LocalDateTime eventDate) {
            this.eventId = eventId;
            this.capacity = capacity;
            this.eventDate = eventDate;
        }
        
        public String getEventId() { return eventId; }
        public int getCapacity() { return capacity; }
        public LocalDateTime getEventDate() { return eventDate; }
    }
    
    static class ReservationResult {
        private final String reservationId;
        private final List<Integer> seatIds;
        private final int totalPrice;
        private final boolean success;
        private final String message;
        
        ReservationResult(String reservationId, List<Integer> seatIds, int totalPrice, 
                         boolean success, String message) {
            this.reservationId = reservationId;
            this.seatIds = new ArrayList<>(seatIds);
            this.totalPrice = totalPrice;
            this.success = success;
            this.message = message;
        }
        
        public String getReservationId() { return reservationId; }
        public List<Integer> getSeatIds() { return seatIds; }
        public int getTotalPrice() { return totalPrice; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    static class CancellationRefund {
        private final int amount;
        private final String message;
        
        CancellationRefund(int amount, String message) {
            this.amount = amount;
            this.message = message;
        }
        
        public int getAmount() { return amount; }
        public String getMessage() { return message; }
    }
    
    private final Map<String, Event> events;
    private final Map<String, SeatHold> holds;
    private final Map<String, Booking> bookings;
    private final Map<String, Queue<WaitlistEntry>> waitlists;
    private final Map<String, Set<Integer>> eventSeats;
    private final ScheduledExecutorService scheduler;
    private final Object lock = new Object();
    
    public SeatBookingEngine() {
        this.events = new ConcurrentHashMap<>();
        this.holds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitlists = new ConcurrentHashMap<>();
        this.eventSeats = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        startHoldExpirationScheduler();
    }
    
    public void createEvent(String eventId, int capacity, LocalDateTime eventDate) {
        events.put(eventId, new Event(eventId, capacity, eventDate));
        Set<Integer> seats = ConcurrentHashMap.newKeySet();
        for (int i = 1; i <= capacity; i++) {
            seats.add(i);
        }
        eventSeats.put(eventId, seats);
        waitlists.put(eventId, new LinkedList<>());
    }
    
    public ReservationResult holdSeats(String eventId, List<TicketType> ticketTypes) {
        synchronized (lock) {
            if (!events.containsKey(eventId)) {
                return new ReservationResult("", Collections.emptyList(), 0, false, 
                    "Event not found");
            }
            
            List<Integer> availableSeats = getAvailableSeats(eventId);
            
            if (availableSeats.size() < ticketTypes.size()) {
                String entryId = UUID.randomUUID().toString();
                waitlists.get(eventId).offer(new WaitlistEntry(entryId, ticketTypes));
                return new ReservationResult(entryId, Collections.emptyList(), 0, false, 
                    "No seats available, added to waitlist");
            }
            
            String reservationId = UUID.randomUUID().toString();
            List<Integer> heldSeats = new ArrayList<>();
            int totalPrice = 0;
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
            boolean isGroupBooking = ticketTypes.size() >= 10;
            
            for (int i = 0; i < ticketTypes.size(); i++) {
                int seatId = availableSeats.get(i);
                TicketType type = ticketTypes.get(i);
                int price = type.getBasePrice();
                
                if (isGroupBooking) {
                    price = (int) (price * 0.95);
                }
                
                Ticket ticket = new Ticket(type, price);
                String holdId = reservationId + "_" + i;
                holds.put(holdId, new SeatHold(holdId, eventId, seatId, ticket, expiresAt));
                heldSeats.add(seatId);
                totalPrice += price;
            }
            
            return new ReservationResult(reservationId, heldSeats, totalPrice, true, 
                "Seats held for 15 minutes");
        }
    }
    
    public boolean confirmReservation(String reservationId) {
        synchronized (lock) {
            List<SeatHold> reservation = holds.values().stream()
                .filter(h -> h.getHoldId().startsWith(reservationId + "_"))
                .collect(Collectors.toList());
            
            if (reservation.isEmpty()) {
                return false;
            }
            
            for (SeatHold hold : reservation) {
                if (hold.isExpired()) {
                    holds.remove(hold.getHoldId());
                    return false;
                }
            }
            
            String eventId = reservation.get(0).getEventId();
            for (SeatHold hold : reservation) {
                String bookingId = UUID.randomUUID().toString();
                bookings.put(bookingId, new Booking(bookingId, eventId, hold.getSeatId(), 
                    hold.getTicket()));
                holds.remove(hold.getHoldId());
            }
            
            processWaitlist(eventId);
            return true;
        }
    }
    
    public boolean releaseSeats(String reservationId) {
        synchronized (lock) {
            List<SeatHold> toRelease = holds.values().stream()
                .filter(h -> h.getHoldId().startsWith(reservationId + "_"))
                .collect(Collectors.toList());
            
            if (toRelease.isEmpty()) {
                return false;
            }
            
            String eventId = toRelease.get(0).getEventId();
            toRelease.forEach(hold -> holds.remove(hold.getHoldId()));
            processWaitlist(eventId);
            
            return true;
        }
    }
    
    public CancellationRefund cancelBooking(String bookingId) {
        synchronized (lock) {
            Booking booking = bookings.get(bookingId);
            if (booking == null || booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                return new CancellationRefund(0, "Booking not found or already cancelled");
            }
            
            Event event = events.get(booking.getEventId());
            if (event == null) {
                return new CancellationRefund(0, "Event not found");
            }
            
            long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), 
                event.getEventDate());
            int refund = calculateRefund(booking.getTicket().getPrice(), daysUntilEvent);
            
            booking.cancel();
            processWaitlist(booking.getEventId());
            
            return new CancellationRefund(refund, "Booking cancelled. Refund: " + refund);
        }
    }
    
    private int calculateRefund(int price, long daysUntilEvent) {
        if (daysUntilEvent > 30) {
            return price;
        } else if (daysUntilEvent >= 7) {
            return price / 2;
        } else {
            return 0;
        }
    }
    
    private void processWaitlist(String eventId) {
        Queue<WaitlistEntry> waitlist = waitlists.get(eventId);
        if (waitlist == null || waitlist.isEmpty()) {
            return;
        }
        
        while (!waitlist.isEmpty()) {
            List<Integer> availableSeats = getAvailableSeats(eventId);
            WaitlistEntry entry = waitlist.peek();
            
            if (availableSeats.size() >= entry.getTicketTypes().size()) {
                waitlist.poll();
                ReservationResult result = holdSeats(eventId, entry.getTicketTypes());
                if (result.isSuccess()) {
                    confirmReservation(result.getReservationId());
                }
            } else {
                break;
            }
        }
    }
    
    private List<Integer> getAvailableSeats(String eventId) {
        Set<Integer> booked = bookings.values().stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED && 
                b.getEventId().equals(eventId))
            .map(Booking::getSeatId)
            .collect(Collectors.toSet());
        
        Set<Integer> held = holds.values().stream()
            .filter(h -> !h.isExpired() && h.getEventId().equals(eventId))
            .map(SeatHold::getSeatId)
            .collect(Collectors.toSet());
        
        Set<Integer> allSeats = eventSeats.get(eventId);
        if (allSeats == null) {
            return Collections.emptyList();
        }
        
        return allSeats.stream()
            .filter(s -> !booked.contains(s) && !held.contains(s))
            .sorted()
            .collect(Collectors.toList());
    }
    
    private void startHoldExpirationScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (lock) {
                List<String> expiredHolds = holds.values().stream()
                    .filter(SeatHold::isExpired)
                    .map(SeatHold::getHoldId)
                    .collect(Collectors.toList());
                
                Set<String> affectedEvents = new HashSet<>();
                for (String holdId : expiredHolds) {
                    SeatHold hold = holds.remove(holdId);
                    if (hold != null) {
                        affectedEvents.add(hold.getEventId());
                    }
                }
                
                for (String eventId : affectedEvents) {
                    processWaitlist(eventId);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    public static void main(String[] args) throws InterruptedException {
        SeatBookingEngine engine = new SeatBookingEngine();
        
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("event_001", 50, eventDate);
        
        List<TicketType> tickets = Arrays.asList(
            TicketType.ADULT, TicketType.ADULT, TicketType.CHILD, TicketType.STUDENT
        );
        
        ReservationResult reservation = engine.holdSeats("event_001", tickets);
        System.out.println("Reservation successful: " + reservation.isSuccess());
        System.out.println("Seats: " + reservation.getSeatIds());
        System.out.println("Total price: " + reservation.getTotalPrice());
        
        if (reservation.isSuccess()) {
            boolean confirmed = engine.confirmReservation(reservation.getReservationId());
            System.out.println("Confirmed: " + confirmed);
            
            String bookingId = engine.bookings.values().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .map(Booking::getBookingId)
                .findFirst()
                .orElse(null);
            
            if (bookingId != null) {
                CancellationRefund refund = engine.cancelBooking(bookingId);
                System.out.println("Refund: " + refund.getAmount() + " cents");
                System.out.println("Message: " + refund.getMessage());
            }
        }
    }
}
