class LineItem {
    private String productName;
    private double unitPrice;
    private int quantity;
    
    public LineItem(String productName, double unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
    
    public double getLineTotal() {
        return unitPrice * quantity;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
