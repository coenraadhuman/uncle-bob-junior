class LineItem {
    String description;
    double price;
    int quantity;
    
    LineItem(String description, double price, int quantity) {
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
    
    double getTotal() {
        return price * quantity;
    }
}
