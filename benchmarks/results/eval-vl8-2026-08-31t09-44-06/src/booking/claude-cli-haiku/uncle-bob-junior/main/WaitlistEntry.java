import java.util.*;

class WaitlistEntry {
    private final String id;
    private final String customerId;
    private final Map<SeatType, Integer> requirements;
    
    WaitlistEntry(String id, String customerId, Map<SeatType, Integer> requirements) {
        this.id = id;
        this.customerId = customerId;
        this.requirements = Map.copyOf(requirements);
    }
    
    int totalRequested() {
        return requirements.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    String customerId() { return customerId; }
    Map<SeatType, Integer> requirements() { return requirements; }
}
