enum Category {
    TRAVEL(500),
    MEALS(150),
    EQUIPMENT(1000);
    
    private final double monthlyCap;
    
    Category(double monthlyCap) {
        this.monthlyCap = monthlyCap;
    }
    
    public double getMonthlyCap() {
        return monthlyCap;
    }
}
