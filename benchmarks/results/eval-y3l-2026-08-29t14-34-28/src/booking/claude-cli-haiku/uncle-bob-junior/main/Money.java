class Money {
    private final double euros;

    Money(double euros) {
        if (euros < 0) throw new IllegalArgumentException("Negative money");
        this.euros = euros;
    }

    Money add(Money other) {
        return new Money(euros + other.euros);
    }

    Money discounted(double percent) {
        return new Money(euros * (100.0 - percent) / 100.0);
    }

    double amount() {
        return euros;
    }

    @Override
    public String toString() {
        return String.format("€%.2f", euros);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        return Math.abs(euros - ((Money) o).euros) < 0.01;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(euros);
    }
}
