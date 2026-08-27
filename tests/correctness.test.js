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

// --- Task routing ---

test('unknown task skips the gate', () => {
  const result = check('Write a haiku about spring.', 'java', 'class X {}');
  assert.equal(result.pass, true);
  assert.match(result.reason, /skipped/i);
});
