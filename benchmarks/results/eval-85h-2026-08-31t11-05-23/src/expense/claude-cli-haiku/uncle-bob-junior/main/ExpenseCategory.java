import java.util.stream.Collectors;

public enum ExpenseCategory {
    TRAVEL(500),
    MEALS(150),
    EQUIPMENT(1000);
    
    private final int monthlyCapEuros;
    
    ExpenseCategory(int monthlyCapEuros) {
        this.monthlyCapEuros = monthlyCapEuros;
    }
    
    public int monthlyCapEuros() {
        return monthlyCapEuros;
    }
}
