import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

class CurrencyConverter {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    
    static double toEur(double amount, String currency) {
        return switch (currency) {
            case "EUR" -> amount;
            case "USD" -> amount * USD_TO_EUR;
            case "GBP" -> amount * GBP_TO_EUR;
            default -> amount;
        };
    }
}
