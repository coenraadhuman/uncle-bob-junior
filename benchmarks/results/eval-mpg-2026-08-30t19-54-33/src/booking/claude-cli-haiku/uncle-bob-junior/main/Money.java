// Money.java
public final class Money {
    private final long amountInCents;

    public Money(long amountInCents) {
        if (amountInCents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amountInCents = amountInCents;
    }

    public Money add(Money other) {
        return new Money(this.amountInCents + other.amountInCents);
    }

    public Money multiply(double factor) {
        return new Money(Math.round(this.amountInCents * factor));
    }

    public Money subtract(Money other) {
        if (other.amountInCents > this.amountInCents) {
            throw new IllegalArgumentException("Cannot subtract more than available");
        }
        return new Money(this.amountInCents - other.amountInCents);
    }

    public long cents() {
        return amountInCents;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        Money other = (Money) o;
        return amountInCents == other.amountInCents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amountInCents);
    }
}
