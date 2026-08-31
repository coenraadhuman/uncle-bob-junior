import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

record DateAmountKey(LocalDate date, double amount) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DateAmountKey k)) return false;
        return date.equals(k.date) && Math.abs(amount - k.amount) < 0.01;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, Math.round(amount * 100));
    }
}
