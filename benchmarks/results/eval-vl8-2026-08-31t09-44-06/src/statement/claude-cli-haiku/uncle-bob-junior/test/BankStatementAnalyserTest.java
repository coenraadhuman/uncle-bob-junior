import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

class BankStatementAnalyserTest {
    public static void main(String[] args) throws IOException {
        runTests();
        System.out.println("\n--- Running analysis ---");
        BankStatementAnalyser analyser = new BankStatementAnalyser();
        analyser.analyse("statement.txt");
    }
    
    static void runTests() {
        testCategories();
        testCurrencies();
        testFlagging();
        System.out.println("All tests passed.\n");
    }
    
    static void testCategories() {
        Categorizer c = new Categorizer();
        assert c.categorise("Monthly salary") == Category.SALARY;
        assert c.categorise("Flat rent") == Category.RENT;
        assert c.categorise("Albert Heijn") == Category.GROCERIES;
        assert c.categorise("Parking") == Category.OTHER;
    }
    
    static void testCurrencies() {
        TransactionParser p = new TransactionParser();
        assert p.parse("2026-01-15;Test;100.00;EUR").amountEur()
            .compareTo(new BigDecimal("100.00")) == 0;
        assert p.parse("2026-01-15;Test;100.00;USD").amountEur()
            .compareTo(new BigDecimal("95.00")) == 0;
        assert p.parse("2026-01-15;Test;100.00;GBP").amountEur()
            .compareTo(new BigDecimal("118.00")) == 0;
    }
    
    static void testFlagging() {
        SuspiciousDetector d = new SuspiciousDetector();
        Transaction small = tx(LocalDate.parse("2026-01-15"), 100.00);
        Transaction large = tx(LocalDate.parse("2026-01-15"), 2500.00);
        assert !d.isLarge(small);
        assert d.isLarge(large);
        
        List<Transaction> same = Arrays.asList(
            tx(LocalDate.parse("2026-01-15"), 50.00),
            tx(LocalDate.parse("2026-01-15"), 50.00)
        );
        assert d.hasDuplicateOnDay(same.get(0), same);
    }
    
    static Transaction tx(LocalDate date, double amount) {
        return new Transaction(date, "test", new BigDecimal(amount), Category.OTHER);
    }
}
