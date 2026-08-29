import java.time.*;
import java.util.*;

class SequentialIds implements IdSource {
    private long counter = 0;

    @Override
    public String next() {
        return "ID-" + (++counter);
    }
}
