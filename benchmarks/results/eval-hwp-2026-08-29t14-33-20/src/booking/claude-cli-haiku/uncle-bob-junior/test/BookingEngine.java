public class BookingEngine {
    private static final int HOLD_DURATION_MINUTES = 15;
    private static final int MIN_GROUP_SIZE = 10;
    private static final double GROUP_DISCOUNT = 0.05;
    private static final int FULL_REFUND_DAYS = 30;
    private static final int PARTIAL_REFUND_DAYS = 7;
    
    private final Event event;
    private final Map<String, SeatHold> activeHolds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    private final Set<Integer> availableSeats;
    private final Object lock = new Object();
    
    public BookingEngine(Event event) {
        this.event = event;
        this.activeHolds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitlist = new LinkedList<>();
        this.availableSeats = initializeSeats();
    }
    
    private Set<Integer> initializeSeats() {
        Set<Integer> seats = new HashSet<>();
        for (int i = 1; i <= event.capacity(); i++) {
            seats.add(i);
        }
        return seats;
    }
    
    public SeatHold holdSeats(List<TicketType> ticketTypes, LocalDateTime eventDateTime) {
        synchronized (lock) {
            expireOldHolds();
            
            int seatsNeeded = ticketTypes.size();
            if (availableSeats.size() < seatsNeeded) {
                createWaitlistEntry(ticketTypes, eventDateTime);
                throw new InsufficientSeatsException(
                    "Insufficient seats. Request added to waitlist.");
            }
            
            return createHold(ticketTypes);
        }
    }
    
    private void expireOldHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredIds = activeHolds.entrySet().stream()
            .filter(e -> e.getValue().expiresAt().isBefore(now))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        for (String holdId : expiredIds) {
            SeatHold hold = activeHolds.remove(holdId);
            availableSeats.addAll(hold.seatNumbers());
        }
        
        if (!expiredIds.isEmpty()) {
            processWaitlist();
        }
    }
    
    private SeatHold createHold(List<TicketType> ticketTypes) {
        String holdId = UUID.randomUUID().toString();
        List<Integer> seatNumbers = allocateSeats(ticketTypes.size());
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);
        
        SeatHold hold = new SeatHold(holdId, seatNumbers, expiresAt);
        activeHolds.put(holdId, hold);
        
        for (int seatNumber : seatNumbers) {
            availableSeats.remove(seatNumber);
        }
        
        return hold;
    }
    
    private List<Integer> allocateSeats(int count) {
        return availableSeats.stream()
            .limit(count)
            .collect(Collectors.toList());
    }
    
    private void createWaitlistEntry(List<TicketType> ticketTypes, LocalDateTime eventDateTime) {
        String waitlistId = UUID.randomUUID().toString();
        waitlist.offer(new WaitlistEntry(waitlistId, ticketTypes, 
                                        LocalDateTime.now(), eventDateTime));
    }
    
    public Booking confirmHold(String holdId, List<TicketType> ticketTypes, 
                              LocalDateTime eventDateTime) {
        synchronized (lock) {
            expireOldHolds();
            
            SeatHold hold = activeHolds.get(holdId);
            if (hold == null) {
                throw new HoldExpiredException("Hold not found or expired");
            }
            
            String bookingId = UUID.randomUUID().toString();
            double totalPrice = calculatePrice(ticketTypes);
            LocalDateTime bookingDateTime = LocalDateTime.now();
            
            Booking booking = new Booking(bookingId, hold.seatNumbers(), ticketTypes, 
                                         totalPrice, bookingDateTime, eventDateTime);
            bookings.put(bookingId, booking);
            activeHolds.remove(holdId);
            
            return booking;
        }
    }
    
    public void releaseHold(String holdId) {
        synchronized (lock) {
            SeatHold hold = activeHolds.remove(holdId);
            if (hold != null) {
                availableSeats.addAll(hold.seatNumbers());
                processWaitlist();
            }
        }
    }
    
    public double cancelBooking(String bookingId, LocalDateTime eventDateTime) {
        synchronized (lock) {
            Booking booking = bookings.remove(bookingId);
            if (booking == null) {
                throw new BookingNotFoundException("Booking not found");
            }
            
            availableSeats.addAll(booking.seatNumbers());
            double refundFraction = calculateRefundFraction(eventDateTime);
            double refund = booking.totalPrice() * refundFraction;
            
            processWaitlist();
            
            return refund;
        }
    }
    
    private double calculatePrice(List<TicketType> ticketTypes) {
        double subtotal = ticketTypes.stream()
            .mapToDouble(TicketType::getBasePrice)
            .sum();
        
        if (ticketTypes.size() >= MIN_GROUP_SIZE) {
            return subtotal * (1 - GROUP_DISCOUNT);
        }
        return subtotal;
    }
    
    private double calculateRefundFraction(LocalDateTime eventDateTime) {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDateTime);
        
        if (daysUntilEvent > FULL_REFUND_DAYS) {
            return 1.0;
        } else if (daysUntilEvent >= PARTIAL_REFUND_DAYS) {
            return 0.5;
        }
        return 0.0;
    }
    
    private void processWaitlist() {
        while (!waitlist.isEmpty() && availableSeats.size() > 0) {
            WaitlistEntry entry = waitlist.peek();
            if (availableSeats.size() >= entry.ticketTypes().size()) {
                waitlist.poll();
            } else {
                break;
            }
        }
    }
    
    public int availableSeatsCount() {
        synchronized (lock) {
            expireOldHolds();
            return availableSeats.size();
        }
    }
    
    public int waitlistSize() {
        synchronized (lock) {
            return waitlist.size();
        }
    }
}
