class Seat {
    private final String id;
    private final String section;
    private final int row;
    private final int number;
    private SeatStatus status;

    Seat(String id, String section, int row, int number) {
        this.id = id;
        this.section = section;
        this.row = row;
        this.number = number;
        this.status = SeatStatus.AVAILABLE;
    }

    String id() {
        return id;
    }

    SeatStatus status() {
        return status;
    }

    void setStatus(SeatStatus status) {
        this.status = status;
    }

    boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }
}
