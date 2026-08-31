class Seat {
    private final String seatNumber;
    private final TicketType ticketType;
    private SeatStatus status;

    enum SeatStatus {
        AVAILABLE, HELD, CONFIRMED
    }

    public Seat(String seatNumber, TicketType ticketType) {
        this.seatNumber = seatNumber;
        this.ticketType = ticketType;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
