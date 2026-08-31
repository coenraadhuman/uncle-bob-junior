public class TicketTier {
    private final String name;
    private final long basePriceInCents;
    
    public static final TicketTier ADULT = new TicketTier("Adult", 1000);
    public static final TicketTier CHILD = new TicketTier("Child", 500);
    public static final TicketTier SENIOR = new TicketTier("Senior", 700);
    public static final TicketTier STUDENT = new TicketTier("Student", 600);
    
    private TicketTier(String name, long basePriceInCents) {
        this.name = name;
        this.basePriceInCents = basePriceInCents;
    }
    
    public long getBasePriceInCents() {
        return basePriceInCents;
    }
    
    public String getName() {
        return name;
    }
}
