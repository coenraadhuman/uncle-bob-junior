#!/usr/bin/env node
// Unit test for the correctness benchmark assertion. Feeds known-good and
// known-bad LLM outputs through each Java task checker and asserts the
// expected pass/fail verdict. The email/csv checkers compile and run real
// Java, so those tests skip cleanly when no JDK is on PATH.

const test = require('node:test');
const assert = require('node:assert/strict');
const { execFileSync } = require('node:child_process');
const correctness = require('../benchmarks/correctness');

const hasJdk = (() => {
  try {
    execFileSync('javac', ['-version'], { stdio: 'pipe', timeout: 15_000 });
    return true;
  } catch {
    return false;
  }
})();
const jdkOnly = { skip: !hasJdk && 'no JDK on PATH' };

// Helper: wrap code in a fenced block and call the assertion with task vars.
function check(task, lang, code) {
  const output = '```' + lang + '\n' + code + '\n```';
  return correctness(output, { vars: { task } });
}

const EMAIL_TASK = 'Write a Java method that validates email addresses.';
const CSV_TASK = "Write a Java program that reads sales.csv and prints the sum of the 'amount' column.";
const RETRY_TASK = 'Write a reusable retry helper in Java: it runs an operation, retries it up to a maximum number of attempts when it throws an exception, and waits a fixed delay between attempts.';
const RATELIMIT_TASK = "Add rate limiting to a Java HTTP handler so each client can't make more than a few requests per minute.";
const ORDER_TASK = 'Write Java code that processes an order: validate the line items, compute the total with 21% VAT, apply a 10% discount when the pre-VAT total exceeds 100 euros, and produce a receipt string.';

// --- Email validator (compiled and run) ---

const GOOD_EMAIL_VALIDATOR = `
public class EmailValidator {
    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}`;

test('email: correct validator class passes', jdkOnly, () => {
  const result = check(EMAIL_TASK, 'java', GOOD_EMAIL_VALIDATOR);
  assert.equal(result.pass, true, result.reason);
  assert.equal(result.score, 1);
});

test('email: always-true validator fails', jdkOnly, () => {
  const result = check(EMAIL_TASK, 'java',
    'public class EmailValidator {\n    public static boolean isValid(String email) { return true; }\n}');
  assert.equal(result.pass, false);
  assert.equal(result.score, 0);
});

test('email: bare method without a class still runs', jdkOnly, () => {
  const result = check(EMAIL_TASK, 'java',
    'public static boolean isValid(String email) {\n    return email != null && email.matches("^[^@\\\\s]+@[^@\\\\s]+\\\\.[^@\\\\s]+$");\n}');
  assert.equal(result.pass, true, result.reason);
});

test('email: trailing usage-example block does not break compilation', jdkOnly, () => {
  const output = '```java\n' + GOOD_EMAIL_VALIDATOR + '\n```\n\nUsage:\n\n```java\nSystem.out.println(EmailValidator.isValidEmail("user@example.com"));\n```';
  const result = correctness(output, { vars: { task: EMAIL_TASK } });
  assert.equal(result.pass, true, result.reason);
});

test('email: two alternative implementations of the same class still pass', jdkOnly, () => {
  const brokenAlternative = 'public class EmailValidator {\n    public static boolean isValidEmail(String email) { return true; }\n}';
  const output = '```java\n' + brokenAlternative + '\n```\n\nOr stricter:\n\n```java\n' + GOOD_EMAIL_VALIDATOR + '\n```';
  const result = correctness(output, { vars: { task: EMAIL_TASK } });
  assert.equal(result.pass, true, result.reason);
});

test('email: public validator with private String->boolean helpers is picked correctly', jdkOnly, () => {
  const result = check(EMAIL_TASK, 'java', `
public final class EmailValidator {
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        int at = email.lastIndexOf('@');
        if (at < 1 || at == email.length() - 1) return false;
        return isValidDomain(email.substring(at + 1));
    }

    private static boolean isValidDomain(String domain) {
        return !domain.contains("@") && domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".");
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('email: a separate JUnit test block does not break the executable check', jdkOnly, () => {
  const junitBlock = 'import org.junit.jupiter.api.Test;\nimport static org.junit.jupiter.api.Assertions.assertTrue;\n\nclass EmailValidatorTest {\n    @Test\n    void acceptsValid() { assertTrue(EmailValidator.isValidEmail("a@b.co")); }\n}';
  const output = '```java\n' + GOOD_EMAIL_VALIDATOR + '\n```\n\n```java\n' + junitBlock + '\n```';
  const result = correctness(output, { vars: { task: EMAIL_TASK } });
  assert.equal(result.pass, true, result.reason);
});

test('csv: a separate JUnit test block does not break the executable check', jdkOnly, () => {
  const junitBlock = 'import org.junit.jupiter.api.Test;\n\nclass SalesCsvSumTest {\n    @Test\n    void sums() { }\n}';
  const output = '```java\n' + GOOD_CSV_PROGRAM + '\n```\n\n```java\n' + junitBlock + '\n```';
  const result = correctness(output, { vars: { task: CSV_TASK } });
  assert.equal(result.pass, true, result.reason);
});

test('email: no code block fails', () => {
  const result = correctness('Just use a regex for this.', { vars: { task: EMAIL_TASK } });
  // Unfenced prose is treated as one bare block, which cannot compile.
  assert.equal(result.pass, false);
});

// --- CSV sum (compiled, run against a fixture summing to 351) ---

const GOOD_CSV_PROGRAM = `
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesCsvSum {
    private static final int AMOUNT_COLUMN = 1;

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("sales.csv"));
        double total = lines.stream()
            .skip(1)
            .mapToDouble(line -> Double.parseDouble(line.split(",")[AMOUNT_COLUMN]))
            .sum();
        System.out.println(total);
    }
}`;

test('csv: correct program passes', jdkOnly, () => {
  const result = check(CSV_TASK, 'java', GOOD_CSV_PROGRAM);
  assert.equal(result.pass, true, result.reason);
});

test('csv: program printing a wrong value fails', jdkOnly, () => {
  const result = check(CSV_TASK, 'java',
    'public class SalesCsvSum {\n    public static void main(String[] args) { System.out.println(999); }\n}');
  assert.equal(result.pass, false);
});

test('csv: value containing 351 as substring fails (e.g. 13510)', jdkOnly, () => {
  const result = check(CSV_TASK, 'java',
    'public class SalesCsvSum {\n    public static void main(String[] args) { System.out.println(13510); }\n}');
  assert.equal(result.pass, false);
});

// --- Retry helper (structural) ---

test('retry: loop + catch + sleep passes', () => {
  const result = check(RETRY_TASK, 'java', `
public class Retry {
    public static <T> T retry(java.util.concurrent.Callable<T> operation, int maxAttempts, long delayMs) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                last = e;
                Thread.sleep(delayMs);
            }
        }
        throw last;
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('retry: single attempt without loop or delay fails', () => {
  const result = check(RETRY_TASK, 'java',
    'public class Retry {\n    public static void retry(Runnable op) { op.run(); }\n}');
  assert.equal(result.pass, false);
});

// --- Rate limiter (structural) ---

test('ratelimit: windowed per-client counting passes', () => {
  const result = check(RATELIMIT_TASK, 'java', `
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String clientId) {
        long now = System.currentTimeMillis();
        Window window = windows.compute(clientId, (id, w) -> w == null || w.expired(now) ? new Window(now) : w);
        if (window.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            return false; // reject: 429 Too Many Requests
        }
        return true;
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('ratelimit: no state and no window fails', () => {
  const result = check(RATELIMIT_TASK, 'java',
    'public class Handler {\n    public void handle(Object request) { process(request); }\n}');
  assert.equal(result.pass, false);
});

// --- Order processor (structural) ---

test('order: validation, VAT, discount, receipt passes', () => {
  const result = check(ORDER_TASK, 'java', `
import java.util.List;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD_EUR = 100;

    public String process(List<LineItem> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("order has no items");
        double subtotal = items.stream().mapToDouble(LineItem::price).sum();
        double discounted = subtotal > DISCOUNT_THRESHOLD_EUR ? subtotal * (1 - DISCOUNT_RATE) : subtotal;
        double total = discounted * (1 + VAT_RATE);
        return String.format("Items: %d, Total: %.2f EUR", items.size(), total);
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('order: hardcoded output with no rules fails', () => {
  const result = check(ORDER_TASK, 'java',
    'public class OrderProcessor {\n    public void go(Object o) { save(o); }\n}');
  assert.equal(result.pass, false);
});

// --- Bank statement analyser (structural) ---

const STATEMENT_TASK = "Analyse a bank statement in Java: each line of statement.txt reads 'date;description;amount;currency' (e.g. 2026-01-15;ALBERT HEIJN;-23.95;EUR). Convert USD and GBP amounts to EUR with fixed rates, categorise every transaction as salary, rent, groceries, or other from description keywords, total each category per month, flag transactions above 2000 EUR and repeated identical amounts on the same day as suspicious, and print a month-by-month report.";

test('statement: conversion, categories, suspicion, monthly report passes', () => {
  const result = check(STATEMENT_TASK, 'java', `
import java.time.YearMonth;
import java.util.Map;

public class StatementAnalyser {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    private static final double SUSPICIOUS_THRESHOLD_EUR = 2000;

    public String analyse(java.util.List<String> lines) {
        Map<YearMonth, Map<String, Double>> totals = new java.util.TreeMap<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        StringBuilder report = new StringBuilder();
        for (String line : lines) {
            String category = categorise(line); // salary, rent, groceries, other
            report.append(String.format("%s: %s%n", category, line));
        }
        return report.toString();
    }

    private String categorise(String description) {
        if (description.contains("SALARY")) return "salary";
        if (description.contains("RENT")) return "rent";
        if (description.contains("GROCERIES")) return "groceries";
        return "other";
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('statement: bare file echo with no rules fails', () => {
  const result = check(STATEMENT_TASK, 'java',
    'public class Analyser {\n    public void run(String file) { System.out.println(file); }\n}');
  assert.equal(result.pass, false);
});

// --- Seat booking engine (structural) ---

const BOOKING_TASK = 'Build an event seat booking engine in Java: a seat can be held for 15 minutes and the hold then expires, is confirmed, or is released; tickets are priced in adult, child, senior, and student tiers; bookings of 10 or more seats get a 5% group discount; cancelling refunds 100% more than 30 days before the event, 50% between 30 and 7 days, and nothing later; when the event is sold out, new requests join a waiting list served in order as seats free up.';

test('booking: lifecycle, tiers, refunds, waiting list passes', () => {
  const result = check(BOOKING_TASK, 'java', `
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

public class BookingEngine {
    private static final int HOLD_MINUTES = 15;
    private final Queue<String> waitingList = new ArrayDeque<>();

    public void hold(String seat) { /* held until Instant.now() + HOLD_MINUTES */ }
    public void confirm(String seat) { }
    public void cancel(String seat) { }

    public double price(String tier) {
        return switch (tier) { case "adult" -> 30; case "child" -> 10; case "senior" -> 15; case "student" -> 20; default -> 30; };
    }

    public double refund(double paid, long daysBeforeEvent) {
        if (daysBeforeEvent > 30) return paid;
        if (daysBeforeEvent >= 7) return paid * 0.5;
        return 0;
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('booking: plain seat map with no lifecycle fails', () => {
  const result = check(BOOKING_TASK, 'java',
    'public class Seats {\n    public void book(int seat) { taken[seat] = true; }\n}');
  assert.equal(result.pass, false);
});

// --- Config parser (structural) ---

const CONFIG_TASK = 'Write a Java parser for a small configuration language: sections in square brackets, key=value pairs, # comments and blank lines; values are typed integers, booleans, or durations such as 30s and 5m; malformed lines and unknown keys produce validation errors that name the line number; missing keys fall back to defaults. Parsing yields a typed configuration object, not a map of strings.';

test('config: sections, comments, typing, line errors, defaults passes', () => {
  const result = check(CONFIG_TASK, 'java', `
public class ConfigParser {
    private static final int DEFAULT_PORT = 8080;

    public Config parse(java.util.List<String> lines) {
        String section = "";
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[")) { section = line; continue; }
            if (!line.contains("=")) throw new IllegalArgumentException("Malformed line " + lineNumber + ": " + line);
            int port = Integer.parseInt(line.split("=")[1].trim());
        }
        return new Config(DEFAULT_PORT);
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('config: string map with no typing or errors fails', () => {
  const result = check(CONFIG_TASK, 'java',
    'public class Loader {\n    public java.util.Map<String, String> load(String text) { return java.util.Map.of(); }\n}');
  assert.equal(result.pass, false);
});

// --- Access-log analyser (Python, structural) ---

const LOGSCAN_TASK = "Write a Python program that analyses a web server access log: each line reads 'IP - - [timestamp] \"METHOD path\" status bytes'. Compute request counts per status class (2xx/3xx/4xx/5xx), the five most requested paths, the error rate per hour, and flag any IP with more than 100 requests in a single hour as suspicious; print a readable report.";

test('logscan: parsing, status classes, top paths, hourly rates, threshold passes', () => {
  const result = check(LOGSCAN_TASK, 'python', `
import re
from collections import Counter

SUSPICIOUS_REQUESTS_PER_HOUR = 100
TOP_PATHS = 5

def status_class(status):
    return f"{status // 100}xx"

def analyse(lines):
    paths = Counter()
    for line in lines:
        match = re.match(r'(\\S+) - - \\[(.+?)\\] "(\\S+) (\\S+)" (\\d+) (\\d+)', line)
        if not match:
            continue
        hour = match.group(2)[:14]  # %d/%b/%Y:%H
        paths[match.group(4)] += 1
    return paths.most_common(TOP_PATHS)

def report(top_paths, suspicious_ips):
    print("Top paths:", top_paths)
    print("Suspicious (flagged):", suspicious_ips)
`);
  assert.equal(result.pass, true, result.reason);
});

test('logscan: line echo with no analysis fails', () => {
  const result = check(LOGSCAN_TASK, 'python',
    'def analyse(lines):\n    for line in lines:\n        print(line)\n');
  assert.equal(result.pass, false);
});

// --- Expense processor (C#, structural) ---

const EXPENSE_TASK = 'Write a C# program that processes employee expense claims: validate each claim (positive amount, known category, receipt attached for amounts over 25 euros), enforce per-category monthly caps (travel 500, meals 150, equipment 1000 euros), route claims above 200 euros to manager approval and above 1000 euros to finance approval, and produce a per-employee monthly reimbursement report.';

test('expense: validation, caps, routing, receipts, report passes', () => {
  const result = check(EXPENSE_TASK, 'csharp', `
using System;
using System.Text;

public class ExpenseProcessor
{
    private const decimal TravelCapEuros = 500m;
    private const decimal MealsCapEuros = 150m;
    private const decimal EquipmentCapEuros = 1000m;
    private const decimal ReceiptRequiredAboveEuros = 25m;
    private const decimal ManagerApprovalAboveEuros = 200m;

    public string Process(Claim claim)
    {
        if (claim.Amount <= 0) throw new ArgumentException("Amount must be positive");
        if (claim.Amount > ReceiptRequiredAboveEuros && !claim.HasReceipt) throw new ArgumentException("Receipt required");
        var route = claim.Amount > ManagerApprovalAboveEuros ? "manager" : "auto";
        if (claim.Amount > EquipmentCapEuros) route = "finance";
        var report = new StringBuilder();
        report.AppendLine($"{claim.Employee}: {claim.Amount} ({route})");
        return report.ToString();
    }
}`);
  assert.equal(result.pass, true, result.reason);
});

test('expense: unconditional payout with no rules fails', () => {
  const result = check(EXPENSE_TASK, 'csharp',
    'public class Payer\n{\n    public void Pay(object claim) { Send(claim); }\n}');
  assert.equal(result.pass, false);
});

// --- Task routing ---

test('the non-Java tasks route to their own checkers, not another task', () => {
  assert.match(check(LOGSCAN_TASK, 'python', 'def x():\n    pass').reason, /^Missing:/);
  assert.match(check(EXPENSE_TASK, 'csharp', 'public class X {}').reason, /^Missing:/);
});

test('unknown task skips the gate', () => {
  const result = check('Write a haiku about spring.', 'java', 'class X {}');
  assert.equal(result.pass, true);
  assert.match(result.reason, /skipped/i);
});

test('the harder tasks route to their own checkers, not another task', () => {
  assert.match(check(STATEMENT_TASK, 'java', 'class X {}').reason, /^Missing:/);
  assert.match(check(BOOKING_TASK, 'java', 'class X {}').reason, /^Missing:/);
  assert.match(check(CONFIG_TASK, 'java', 'class X {}').reason, /^Missing:/);
});
