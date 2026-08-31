import java.io.*;
import java.util.*;

public class BankStatementAnalyzer {
    public static void main(String[] args) throws IOException {
        CurrencyConverter converter = new CurrencyConverter();
        TransactionCategorizer categorizer = new TransactionCategorizer();
        BankStatementParser parser = new BankStatementParser(converter, categorizer);

        List<Transaction> transactions = parser.parse("statement.txt");
        BankStatementReport report = new BankStatementReport(transactions);
        report.print();
    }
}
