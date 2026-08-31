public class IdGenerator {
    private int counter = 0;
    
    public synchronized String nextId() {
        return "ID_" + (++counter);
    }
}
