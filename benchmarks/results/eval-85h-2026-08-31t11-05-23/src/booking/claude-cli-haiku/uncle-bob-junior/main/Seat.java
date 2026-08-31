public class Seat {
    private final String id;
    
    public Seat(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Seat)) return false;
        return id.equals(((Seat) o).id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
