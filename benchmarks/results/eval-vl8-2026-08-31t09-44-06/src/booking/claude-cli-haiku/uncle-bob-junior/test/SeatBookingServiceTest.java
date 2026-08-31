import java.math.BigDecimal;
import java.time.*;
import java.util.*;

// ============ TESTS ============

class SeatBookingServiceTest {
    private SeatBookingService service;
    private LocalDate eventDate;
    private Map<SeatType, Money> pricing;
    
    void setUp() {
        eventDate = LocalDate.now().plusDays(60);
        pricing = Map.of(
            SeatType.ADULT, new Money(new BigDecimal("50.00")),
            SeatType.CHILD, new Money(new BigDecimal("25.00")),
            SeatType.SENIOR, new Money(new BigDecimal("35.00")),
            SeatType.STUDENT, new Money(new BigDecimal("30.00"))
        );
        
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            SeatType type = SeatType.values()[(i - 1) % 4];
            seats.add(new Seat(new SeatLocation("A", 1, i), type));
        }
        service = new SeatBookingService(eventDate, seats, pricing);
    }
    
    void testCreateHold() {
        setUp();
        Map<SeatType, Integer> req = Map.of(SeatType.ADULT, 2);
        Hold hold = service.createHold("cust1", req);
        
        assert hold.id() != null;
        assert hold.customerId().equals("cust1");
        assert hold.seats().size() == 2;
        System.out.println("✓ createHold");
    }
    
    void testConfirmHold() {
        setUp();
        Map<SeatType, Integer> req = Map.of(SeatType.ADULT, 2);
        Hold hold = service.createHold("cust1", req);
        Booking booking = service.confirmHold(hold.id());
        
        assert booking.totalPrice().amount.equals(new BigDecimal("100.00"));
        System.out.println("✓ confirmHold");
    }
    
    void testGroupDiscount() {
        setUp();
        Map<SeatType, Integer> req = Map.of(SeatType.ADULT, 10);
        Hold hold = service.createHold("cust1", req);
        Booking booking = service.confirmHold(hold.id());
        
        assert booking.totalPrice().amount.equals(new BigDecimal("475.00"));
        System.out.println("✓ groupDiscount");
    }
    
    void testReleaseHold() {
        setUp();
        int before = service.availableSeatCount();
        Map<SeatType, Integer> req = Map.of(SeatType.ADULT, 5);
        Hold hold = service.createHold("cust1", req);
        service.releaseHold(hold.id());
        int after = service.availableSeatCount();
        
        assert after > before;
        System.out.println("✓ releaseHold");
    }
    
    void testWaitlist() {
        setUp();
        for (int i = 0; i < 20; i++) {
            Map<SeatType, Integer> req = Map.of(SeatType.ADULT, 1);
            Hold hold = service.createHold("c" + i, req);
            service.confirmHold(hold.id());
        }
        
        try {
            service.createHold("waitlist", Map.of(SeatType.ADULT, 1));
        } catch (InsufficientSeatsException e) {
            assert service.waitlistSize() == 1;
            System.out.println("✓ waitlist");
        }
    }
    
    void testRefund100Percent() {
        setUp();
        LocalDate eventIn60Days = LocalDate.now().plusDays(60);
        Booking booking = new Booking("b1", "c1", eventIn60Days,
            List.of(new Seat(new SeatLocation("A", 1, 1), SeatType.ADULT)),
            new Money(new BigDecimal("100.00")));
        
        assert booking.calculateRefund().amount.equals(new BigDecimal("100.00"));
        System.out.println("✓ refund100");
    }
    
    void testRefund50Percent() {
        setUp();
        LocalDate eventIn15Days = LocalDate.now().plusDays(15);
        Booking booking = new Booking("b1", "c1", eventIn15Days,
            List.of(new Seat(new SeatLocation("A", 1, 1), SeatType.ADULT)),
            new Money(new BigDecimal("100.00")));
        
        assert booking.calculateRefund().amount.equals(new BigDecimal("50.00"));
        System.out.println("✓ refund50");
    }
    
    void testRefund0Percent() {
        setUp();
        LocalDate eventIn3Days = LocalDate.now().plusDays(3);
        Booking booking = new Booking("b1", "c1", eventIn3Days,
            List.of(new Seat(new SeatLocation("A", 1, 1), SeatType.ADULT)),
            new Money(new BigDecimal("100.00")));
        
        assert booking.calculateRefund().amount.equals(BigDecimal.ZERO);
        System.out.println("✓ refund0");
    }
    
    void testCancelBooking() {
        setUp();
        Map<SeatType, Integer> req = Map.of(SeatType.ADULT, 2);
        Hold hold = service.createHold("cust1", req);
        Booking booking = service.confirmHold(hold.id());
        Money refund = service.cancelBooking(booking.id());
        
        assert refund.amount.compareTo(BigDecimal.ZERO) > 0;
        assert service.availableSeatCount() > 0;
        System.out.println("✓ cancelBooking");
    }
    
    public static void main(String[] args) {
        SeatBookingServiceTest test = new SeatBookingServiceTest();
        test.testCreateHold();
        test.testConfirmHold();
        test.testGroupDiscount();
        test.testReleaseHold();
        test.testWaitlist();
        test.testRefund100Percent();
        test.testRefund50Percent();
        test.testRefund0Percent();
        test.testCancelBooking();
        System.out.println("\nAll tests passed.");
    }
}
