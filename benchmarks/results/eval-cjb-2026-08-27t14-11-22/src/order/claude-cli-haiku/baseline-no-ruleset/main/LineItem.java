class LineItem {
    private String description;
    private int quantity;
    private double unitPrice;

    LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    String getDescription() {
        return description;
    }

    int getQuantity() {
        return quantity;
    }

    double getUnitPrice() {
        return unitPrice;
    }

    double getTotal() {
        return quantity * unitPrice;
    }
}
