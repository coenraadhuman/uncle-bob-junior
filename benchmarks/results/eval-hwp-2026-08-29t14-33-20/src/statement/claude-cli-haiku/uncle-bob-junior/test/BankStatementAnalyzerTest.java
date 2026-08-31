import java.time.*;
import java.util.*;

class BankStatementAnalyzerTest {
    public static void main(String[] args) {
        testTransactionParsing();
        testCurrencyConversion();
        testCategorization();
        testLargeAmountFlag();
        testDuplicateAmountFlag();
        System.out.println("✓ All tests passed");
    }
    
    private static void testTransactionParsing() {
        var t = Transaction.parse("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        assert t.date().equals(LocalDate.of(2026, 1, 15));
        assert t.description().equals("ALBERT HEIJN");
        assert t.amount() == -23.95;
        assert t.currency().equals("EUR");
        assert Math.abs(t.amountEur() - (-23.95)) < 0.01;
    }
    
    private static void testCurrencyConversion() {
        var eur = Transaction.parse("2026-01-01;test;100;EUR");
        var usd = Transaction.parse("2026-01-01;test;100;USD");
        var gbp = Transaction.parse("2026-01-01;test;100;GBP");
        
        assert Math.abs(eur.amountEur() - 100) < 0.01;
        assert Math.abs(usd.amountEur() - 92) < 0.01;
        assert Math.abs(gbp.amountEur() - 117) < 0.01;
    }
    
    private static void testCategorization() {
        assert Transaction.parse("2026-01-01;Monthly Salary;100;EUR").category().equals("salary");
        assert Transaction.parse("2026-01-01;Rent Payment;-800;EUR").category().equals("rent");
        assert Transaction.parse("2026-01-01;ALBERT HEIJN;-50;EUR").category().equals("groceries");
        assert Transaction.parse("2026-01-01;Mystery;100;EUR").category().equals("other");
    }
    
    private static void testLargeAmountFlag() {
        var normal = Transaction.parse("2026-01-01;normal;1500;EUR");
        var large = Transaction.parse("2026-01-01;large;2500;EUR");
        
        var flagged = new SuspiciousFlagService().flagAll(List.of(normal, large));
        
        assert !flagged.get(0).suspicious();
        assert flagged.get(1).suspicious();
    }
    
    private static void testDuplicateAmountFlag() {
        var t1 = Transaction.parse("2026-01-01;shop1;-50;EUR");
        var t2 = Transaction.parse("2026-01-01;shop2;-50;EUR");
        var t3 = Transaction.parse("2026-01-02;shop3;-50;EUR");
        
        var flagged = new SuspiciousFlagService().flagAll(List.of(t1, t2, t3));
        
        assert flagged.get(0).suspicious();
        assert flagged.get(1).suspicious();
        assert !flagged.get(2).suspicious();
    }
}
