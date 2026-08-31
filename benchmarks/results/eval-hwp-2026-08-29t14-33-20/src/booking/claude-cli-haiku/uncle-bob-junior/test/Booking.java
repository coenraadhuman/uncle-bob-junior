public record Booking(String bookingId, List<Integer> seatNumbers, List<TicketType> ticketTypes,
                     double totalPrice, LocalDateTime bookingDateTime, LocalDateTime eventDateTime) {}
