// Unit tests for benchmarks/clean-code-metrics.js: the ruler the with/without
// benchmark is scored by, so the ruler itself gets proven here (no API, no network).
const test = require('node:test');
const assert = require('node:assert');
const { analyze } = require('../benchmarks/clean-code-metrics.js');

const DIRTY_JS = `
function process(d, f) {
  var x = 0;
  if (d) {
    if (d.length > 3) {
      for (var q = 0; q < d.length; q++) {
        if (d[q].amount > 250) {
          if (f) {
            x += d[q].amount * 0.175;
          }
        }
      }
    }
  }
  var total1 = 0;
  var rate1 = 0.21;
  total1 = x * rate1;
  console.log(total1);
  var total2 = 0;
  var rate2 = 0.21;
  total2 = x * rate2;
  console.log(total2);
  var total3 = 0;
  var rate3 = 0.21;
  total3 = x * rate3;
  console.log(total3);
  return x;
}
`;

const CLEAN_JS = `
const VAT_RATE = 0.21;
const HIGH_VALUE_THRESHOLD = 250;

function highValueVat(orders) {
  if (!orders) return 0;
  const highValue = orders.filter((order) => order.amount > HIGH_VALUE_THRESHOLD);
  return highValue.reduce((sum, order) => sum + order.amount * VAT_RATE, 0);
}

assert(highValueVat(null) === 0);
`;

const PYTHON_SNIPPET = `
import csv

TAX_RATE = 0.21

def total_amount(path):
    with open(path) as handle:
        rows = csv.DictReader(handle)
        return sum(float(row["amount"]) for row in rows)

def long_function(rows):
    total = 0
    a = 1
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    total += a
    return total
`;

const DIRTY_JAVA = `
public class OrderThing {
    public String process(java.util.List<Item> d, boolean f) {
        String s = "";
        if (d != null) {
            if (!d.isEmpty()) {
                for (Item it : d) {
                    if (it.amount() > 250) {
                        if (f) {
                            s += it.amount() * 0.175;
                        }
                    }
                }
            }
        }
        return s;
    }
}
`;

const CLEAN_JAVA = `
public class VatCalculator {
    private static final double VAT_RATE = 0.21;
    private static final double HIGH_VALUE_THRESHOLD = 250;

    public static double highValueVat(java.util.List<Item> orders) {
        if (orders == null) return 0;
        return orders.stream()
            .filter(order -> order.amount() > HIGH_VALUE_THRESHOLD)
            .mapToDouble(order -> order.amount() * VAT_RATE)
            .sum();
    }
}
`;

test('dirty JS: deep nesting, magic numbers, short names, duplication all detected', () => {
  const m = analyze(DIRTY_JS, 'javascript');
  assert.equal(m.language, 'brace');
  assert.ok(m.maxNestingDepth >= 5, `nesting ${m.maxNestingDepth} should be >= 5`);
  assert.ok(m.deeplyNested);
  assert.ok(m.magicNumberCount >= 2, `magic numbers ${m.magicNumberCount} should be >= 2 (250, 0.175, 0.21)`);
  assert.ok(m.shortNameCount >= 2, `short names ${m.shortNameCount} should count x and q, d and f are params`);
  assert.ok(m.duplicateBlockCount >= 1, `duplicate blocks ${m.duplicateBlockCount} should catch the repeated total/rate stanza`);
  assert.equal(m.longFunctionCount, 1);
  assert.equal(m.hasTests, false);
});

test('clean JS: flat, named, single-purpose code scores near zero', () => {
  const m = analyze(CLEAN_JS, 'javascript');
  assert.equal(m.magicNumberCount, 0, 'named constants must not count as magic');
  assert.equal(m.shortNameCount, 0);
  assert.equal(m.duplicateBlockCount, 0);
  assert.equal(m.longFunctionCount, 0);
  assert.ok(m.maxNestingDepth <= 2);
  assert.equal(m.hasTests, true);
});

test('python: def extents via indentation, UPPER_CASE constants not magic', () => {
  const m = analyze(PYTHON_SNIPPET, 'python');
  assert.equal(m.language, 'python');
  assert.equal(m.functionCount, 2);
  assert.equal(m.longFunctionCount, 1, 'only long_function exceeds the threshold');
  assert.equal(m.magicNumberCount, 0, 'TAX_RATE declaration line is a named constant');
});

test('dirty Java: method detected, nesting and magic numbers counted', () => {
  const m = analyze(DIRTY_JAVA, 'java');
  assert.equal(m.language, 'brace');
  assert.ok(m.functionCount >= 1, `should detect the process method, got ${m.functionCount}`);
  assert.ok(m.maxNestingDepth >= 5, `nesting ${m.maxNestingDepth} should count class+method+ifs`);
  assert.ok(m.magicNumberCount >= 2, `magic numbers ${m.magicNumberCount} should count 250 and 0.175`);
  assert.ok(m.shortNameCount >= 1, `short names ${m.shortNameCount} should count String s`);
});

test('clean Java: final constants are not magic, guard clause keeps nesting sane', () => {
  const m = analyze(CLEAN_JAVA, 'java');
  assert.equal(m.functionCount, 1, 'one method, and stream lambdas without braces are not functions');
  assert.equal(m.magicNumberCount, 0, 'final UPPER_CASE declarations must not count as magic');
  assert.equal(m.shortNameCount, 0);
  assert.equal(m.longFunctionCount, 0);
});

test('Java test signals are recognised', () => {
  assert.equal(analyze('@Test\nvoid rejectsEmptyCart() {\n  assertThrows(IllegalArgumentException.class, () -> processor.process(List.of()));\n}', 'java').hasTests, true);
  assert.equal(analyze('assertEquals(4, add(2, 2));', 'java').hasTests, true);
});

test('python detection works without a language tag', () => {
  assert.equal(analyze('def add(a, b):\n    return a + b', '').language, 'python');
  assert.equal(analyze('function add(a, b) {\n  return a + b;\n}', '').language, 'brace');
});

test('comments and blanks are not code lines', () => {
  const m = analyze('// header\n\n/* block\ncomment */\nconst A = 1;\n', 'javascript');
  assert.equal(m.loc, 1);
});

test('empty input yields zeroed metrics', () => {
  const m = analyze('', 'javascript');
  assert.equal(m.loc, 0);
  assert.equal(m.functionCount, 0);
  assert.equal(m.maxFunctionLength, 0);
});

test('mutable Java fields counted; finals, constants, and signatures are not', () => {
  const m = analyze([
    'public class Cart {',
    '  private static final int MAX_ITEMS = 50;',
    '  private final String ownerId;',
    '  private int itemCount;',
    '  private List<String> items = new ArrayList<>();',
    '  public Cart(String ownerId) { this.ownerId = ownerId; }',
    '  public void addItem(String item) { items.add(item); }',
    '}',
  ].join('\n'), 'java');
  assert.equal(m.mutableFieldCount, 2, 'itemCount and items are mutable; MAX_ITEMS and ownerId are not');
});

test('interface and abstract method signatures are not mutable fields', () => {
  const m = analyze([
    'public interface Store {',
    '  public void save(String key);',
    '  private String load(String key);',
    '}',
  ].join('\n'), 'java');
  assert.equal(m.mutableFieldCount, 0);
});

test('setters counted, non-setter void methods and getters are not', () => {
  const m = analyze([
    'public class Config {',
    '  private int limit;',
    '  public void setLimit(int limit) { this.limit = limit; }',
    '  void setTimeoutMillis(long value) { this.timeout = value; }',
    '  public int getLimit() { return limit; }',
    '  public void reset() { limit = 0; }',
    '  public String settings() { return "n/a"; }',
    '}',
  ].join('\n'), 'java');
  assert.equal(m.setterCount, 2, 'setLimit and setTimeoutMillis are setters; reset/getLimit/settings are not');
});

test('python code reports zero mutability metrics', () => {
  const m = analyze('def add(a, b):\n    return a + b', 'python');
  assert.equal(m.mutableFieldCount, 0);
  assert.equal(m.setterCount, 0);
});
