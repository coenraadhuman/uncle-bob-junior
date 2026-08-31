import java.time.LocalDate;

class BankStatementAnalyzerTest {
    public static void main(String[] args) {
        testParseTransaction();
        testCurrencyConversion();
        testCategorization();
        testSuspiciousLargeTransaction();
        System.out.println("All tests passed!");
    }
    
    static void testParseTransaction() {
        Transaction t = Transaction.parse("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        assert t.date().equals(LocalDate.of(2026, 1, 15));
        assert t.description().equals("ALBERT HEIJN");
        assert t.amountEur() == -23.95;
        assert t.category().equals("groceries");
    }
    
    static void testCurrencyConversion() {
        Transaction usd = Transaction.parse("2026-01-15;SHOP;-100.00;USD");
        assert Math.abs(usd.amountEur() - (-92.0)) < 0.01;
        
        Transaction gbp = Transaction.parse("2026-01-15;SHOP;-100.00;GBP");
        assert Math.abs(gbp.amountEur() - (-117.0)) < 0.01;
    }
    
    static void testCategorization() {
        assert Transaction.parse("2026-01-15;SALARY PAYMENT;1000;EUR").category().equals("salary");
        assert Transaction.parse("2026-01-15;MONTHLY RENT;500;EUR").category().equals("rent");
        assert Transaction.parse("2026-01-15;ALBERT HEIJN;-50;EUR").category().equals("groceries");
        assert Transaction.parse("2026-01-15;GAS BILL;-30;EUR").category().equals("other");
    }
    
    static void testSuspiciousLargeTransaction() {
        Transaction large = Transaction.parse("2026-01-15;BIG PURCHASE;-2500;EUR");
        assert large.isSuspiciouslyLarge();
        
        Transaction normal = Transaction.parse("2026-01-15;SMALL PURCHASE;-500;EUR");
        assert !normal.isSuspiciouslyLarge();
    }
}
