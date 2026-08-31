import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

record Transaction(
    LocalDate date,
    String description,
    double amount,
    String currency,
    double amountEur,
    String category
) {}
