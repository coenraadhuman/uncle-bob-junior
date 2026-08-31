import java.util.stream.Collectors;

public enum TicketType {
    ADULT(100.0), CHILD(50.0), SENIOR(75.0), STUDENT(60.0);

    private final double price;

    TicketType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}
