public record WaitlistEntry(String waitlistId, List<TicketType> ticketTypes, 
                           LocalDateTime requestedAt, LocalDateTime eventDateTime) {}
