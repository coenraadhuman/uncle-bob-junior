class Seat {
    private final int seatId;
    private SeatStatus status;
    
    enum SeatStatus {
        AVAILABLE, HELD, BOOKED
    }
    
    public Seat(int seatId) {
        this.seatId = seatId;
        this.status = SeatStatus.AVAILABLE;
    }
    
    public int getSeatId() {
        return seatId;
    }
    
    public SeatStatus getStatus() {
        return status;
    }
    
    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
