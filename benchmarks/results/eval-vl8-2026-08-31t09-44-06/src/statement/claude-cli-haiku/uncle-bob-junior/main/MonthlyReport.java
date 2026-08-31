import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

class MonthlyReport {
    private final YearMonth month;
    private final Map<Category, BigDecimal> totals;
    private final List<Transaction> flaggedTransactions;
    
    MonthlyReport(YearMonth month, Map<Category, BigDecimal> totals, 
                  List<Transaction> flaggedTransactions) {
        this.month = month;
        this.totals = new EnumMap<>(totals);
        this.flaggedTransactions = new ArrayList<>(flaggedTransactions);
    }
    
    YearMonth month() { return month; }
    Map<Category, BigDecimal> totals() { return new EnumMap<>(totals); }
    List<Transaction> flaggedTransactions() { return new ArrayList<>(flaggedTransactions); }
}
