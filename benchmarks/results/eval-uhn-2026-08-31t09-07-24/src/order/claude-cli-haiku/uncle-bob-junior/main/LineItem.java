record LineItem(String description, double unitPrice, int quantity) {
    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    double subtotal() {
        return unitPrice * quantity;
    }
}
