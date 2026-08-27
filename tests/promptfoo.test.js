// Unit tests for the promptfoo harness: the arm prompt functions, the metric
// asserts, and the config's file references. No API, no network.
const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const baselineArm = require('../benchmarks/arms/baseline.js');
const ubjArm = require('../benchmarks/arms/uncle-bob-junior.js');
const { DELIVERY_INSTRUCTION } = require('../benchmarks/arms/delivery.js');
const metrics = require('../benchmarks/promptfoo-metrics.js');

const TASK = 'Write a Java method that validates email addresses.';

const CLEAN_JAVA_REPLY = [
  'Here is the solution:',
  '```java',
  'public final class EmailValidator {',
  '    private static final int MAX_LENGTH = 254;',
  '    public static boolean isValid(String email) {',
  '        if (email == null || email.length() > MAX_LENGTH) {',
  '            return false;',
  '        }',
  '        return email.contains("@");',
  '    }',
  '}',
  '```',
  'And the tests:',
  '```java',
  'class EmailValidatorTest {',
  '    @Test',
  '    void rejectsNull() { assertFalse(EmailValidator.isValid(null)); }',
  '}',
  '```',
].join('\n');

test('baseline arm sends only the task with the delivery contract', () => {
  const messages = baselineArm({ vars: { task: TASK } });
  assert.equal(messages.length, 1);
  assert.equal(messages[0].role, 'user');
  assert.ok(messages[0].content.includes(TASK));
  assert.ok(messages[0].content.includes(DELIVERY_INSTRUCTION));
});

test('uncle-bob-junior arm prepends the SKILL.md body as system prompt', () => {
  const messages = ubjArm({ vars: { task: TASK } });
  assert.equal(messages.length, 2);
  assert.equal(messages[0].role, 'system');
  assert.ok(messages[0].content.includes('meticulous senior developer'));
  assert.ok(!messages[0].content.startsWith('---'), 'frontmatter must be stripped');
  assert.equal(messages[1].role, 'user');
  assert.ok(messages[1].content.includes(TASK));
  assert.ok(messages[1].content.includes(DELIVERY_INSTRUCTION));
});

test('the delivery contract demands fenced Java and forbids file edits', () => {
  assert.ok(DELIVERY_INSTRUCTION.includes('Reply with the complete solution as Java code'));
  assert.ok(DELIVERY_INSTRUCTION.includes('Do not create or edit files'));
});

const DIRTY_JAVA_REPLY = [
  '```java',
  'public class OrderHandler {',
  '    private int retries;',
  '    public String process(String[] items) {',
  '        String receipt = "";',
  '        double total = 0;',
  '        for (String item : items) {',
  '            if (item != null) {',
  '                if (item.length() > 3) {',
  '                    if (item.contains(":")) {',
  '                        double amount = Double.parseDouble(item.split(":")[1]);',
  '                        if (amount > 250) {',
  '                            total += amount * 0.175;',
  '                        } else {',
  '                            total += amount * 0.21;',
  '                        }',
  '                    }',
  '                }',
  '            }',
  '        }',
  '        if (total > 99) {',
  '            total = total * 0.9;',
  '        }',
  '        receipt = "Total: " + total;',
  '        retries = 3;',
  '        receipt += " retries=" + retries;',
  '        receipt += " tax included";',
  '        receipt += " thank you";',
  '        return receipt;',
  '    }',
  '}',
  '```',
].join('\n');

test('production code excludes test blocks and non-code fences', () => {
  const production = metrics.productionCode(CLEAN_JAVA_REPLY);
  assert.ok(production.code.includes('class EmailValidator'));
  assert.ok(!production.code.includes('EmailValidatorTest'), 'test blocks are not production code');
  assert.equal(metrics.productionCode('```bash\nmvn test\n```'), null, 'non-code fences are not production code');
});

test('shipsTests gates on the whole reply, not just production code', () => {
  const shipped = metrics.shipsTests(CLEAN_JAVA_REPLY);
  assert.equal(shipped.pass, true);
  assert.equal(shipped.score, 1);
  const untested = CLEAN_JAVA_REPLY.split('And the tests:')[0];
  const missing = metrics.shipsTests(untested);
  assert.equal(missing.pass, false, 'changed behavior without its test is unfinished');
  assert.equal(missing.score, 0);
});

test('promptfooconfig.yaml references only files and metric exports that exist', () => {
  const config = fs.readFileSync(path.join(root, 'benchmarks', 'promptfooconfig.yaml'), 'utf8');
  for (const match of config.matchAll(/file:\/\/([\w./-]+?)(?::(\w+))?(?=\s|$)/gm)) {
    const [, relPath, exportName] = match;
    const absPath = path.join(root, 'benchmarks', relPath);
    assert.ok(fs.existsSync(absPath), `${relPath} referenced by promptfooconfig.yaml must exist`);
    if (exportName) {
      assert.equal(typeof require(absPath)[exportName], 'function', `${relPath} must export ${exportName}`);
    }
  }
});

const ClaudeCliProvider = require('../benchmarks/providers/claude-cli.js');
const { parsePromptMessages } = require('../benchmarks/providers/claude-cli.js');

test('provider parses arm message arrays into system append and user prompt', () => {
  const rendered = JSON.stringify(ubjArm({ vars: { task: TASK } }));
  const { system, user } = parsePromptMessages(rendered);
  assert.ok(system.includes('meticulous senior developer'));
  assert.ok(user.includes(TASK));

  const baselineRendered = JSON.stringify(baselineArm({ vars: { task: TASK } }));
  const baselineParsed = parsePromptMessages(baselineRendered);
  assert.equal(baselineParsed.system, null, 'baseline arm must not get a system append');
  assert.ok(baselineParsed.user.includes(TASK));
});

test('provider treats a plain-string prompt as the user prompt', () => {
  const parsed = parsePromptMessages('just a bare prompt');
  assert.equal(parsed.system, null);
  assert.equal(parsed.user, 'just a bare prompt');
});

test('provider id is model-scoped so promptfoo can tell the columns apart', () => {
  const provider = new ClaudeCliProvider({ config: { model: 'sonnet' }, label: 'sonnet' });
  assert.equal(provider.id(), 'claude-cli:sonnet');
  assert.equal(new ClaudeCliProvider({}).id(), 'claude-cli:haiku');
});

const { spawnSync } = require('child_process');
const habitHooksAssert = require('../benchmarks/habit-hooks-assert.js');
const { parseIssues } = habitHooksAssert;
const { splitJavaTypes, fileNameFor, codeFiles: extractCodeFiles, pluginsFor } = require('../benchmarks/extract-files.js');
const hasHabitHooks = spawnSync('habit-hooks', ['--version'], { encoding: 'utf8' }).status === 0;

test('parseIssues reads rule names and counts from habit-hooks output', () => {
  const report = [
    '── oversized-function (2 issues) ──',
    'prose about the rule',
    'OrderHandler.java:3',
    '── swallowed-exception (1 issue) ──',
    'OrderHandler.java:13',
  ].join('\n');
  assert.deepEqual(parseIssues(report), [
    { rule: 'oversized-function', count: 2, locations: ['OrderHandler.java:3'] },
    { rule: 'swallowed-exception', count: 1, locations: ['OrderHandler.java:13'] },
  ]);
  assert.deepEqual(parseIssues('✅ Habit Hooks: automated checks passed.'), []);
});

test('extracted files are named after their declared type per language', () => {
  assert.equal(fileNameFor('public final class EmailValidator {\n}', 'java', 0), 'EmailValidator.java');
  assert.equal(fileNameFor('interface Store { void save(); }', 'java', 3), 'Store.java');
  assert.equal(fileNameFor('int x = 1;', 'java', 2), 'Snippet3.java');
  assert.equal(fileNameFor('def parse_row(row):\n    return row', 'py', 0), 'parse_row.py');
});

test('habit-hooks fails dirty code and passes clean code', { skip: !hasHabitHooks }, () => {
  const dirty = habitHooksAssert(DIRTY_JAVA_REPLY);
  assert.equal(dirty.pass, false, 'enforced smells fail the run, mirroring habit-hooks exit 1');
  assert.ok(dirty.score < 1, `26-line method must cost score: ${dirty.reason}`);
  assert.ok(/FAILED/.test(dirty.reason), dirty.reason);
  assert.ok(/oversized-function/.test(dirty.reason), dirty.reason);

  const clean = habitHooksAssert(CLEAN_JAVA_REPLY);
  assert.equal(clean.pass, true);
  assert.equal(clean.score, 1, clean.reason);
  assert.equal(clean.reason, 'habit-hooks passed: clean');
});

const os = require('os');
const exporter = require('../benchmarks/export-results.js');

const FIXTURE_EVAL = {
  evalId: 'eval-FIX-2026-08-27T12:00:00',
  results: {
    results: [
      {
        prompt: { label: 'baseline (no ruleset)' },
        provider: { label: 'haiku' },
        testCase: { description: 'email' },
        response: { output: DIRTY_JAVA_REPLY },
        gradingResult: {
          score: 0.4,
          componentResults: [
            { assertion: { metric: 'habit_hooks' }, pass: true, score: 0.5, reason: 'habit-hooks: 3 smell(s) — oversized-function(3)' },
            { assertion: { metric: 'ships_tests' }, pass: false, score: 0, reason: 'no tests shipped' },
            { assertion: { metric: 'correct' }, pass: true, score: 1, reason: 'ok' },
          ],
        },
      },
      {
        prompt: { label: 'uncle-bob-junior' },
        provider: { label: 'haiku' },
        testCase: { description: 'email' },
        response: { output: CLEAN_JAVA_REPLY },
        gradingResult: {
          score: 1,
          componentResults: [
            { assertion: { metric: 'habit_hooks' }, pass: true, score: 1, reason: 'habit-hooks: clean' },
            { assertion: { metric: 'ships_tests' }, pass: true, score: 1, reason: 'ships tests' },
            { assertion: { metric: 'correct' }, pass: true, score: 1, reason: 'ok' },
          ],
        },
      },
    ],
  },
};

test('exporter flattens eval JSON into per-arm rows', () => {
  const rows = exporter.resultRows(FIXTURE_EVAL);
  assert.equal(rows.length, 2);
  assert.equal(rows[0].arm, 'baseline (no ruleset)');
  assert.equal(rows[0].task, 'email');
  assert.ok(rows[0].output.includes('OrderHandler'));
  assert.equal(rows[1].score, 1);
});

test('report.md carries the scoreboard and per-arm means', () => {
  const rows = exporter.resultRows(FIXTURE_EVAL);
  const report = exporter.buildReport(FIXTURE_EVAL.evalId, rows);
  assert.ok(report.includes('| email | haiku | baseline (no ruleset) | 0.40 | pass |'), report);
  assert.ok(report.includes('habit-hooks: clean'));
  assert.ok(report.includes('**haiku / uncle-bob-junior**: 1.000 (n=1)'));
  assert.ok(report.includes('**haiku / baseline (no ruleset)**: 0.400 (n=1)'));
});

test('writeRunArtifacts lays out src, habit-hooks reports, and report.md', () => {
  const runDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-export-test-'));
  try {
    const rows = exporter.resultRows(FIXTURE_EVAL);
    const fakeScan = (dir, plugins) => ({ skipped: false, report: `fake report for ${dir} with ${plugins.join('+')}\n`, issues: [], total: 0 });
    exporter.writeRunArtifacts(FIXTURE_EVAL.evalId, rows, runDir, { scan: fakeScan });

    assert.ok(fs.existsSync(path.join(runDir, 'report.md')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'haiku', 'uncle-bob-junior', 'main', 'EmailValidator.java')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'haiku', 'uncle-bob-junior', 'test', 'EmailValidatorTest.java')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'haiku', 'baseline-no-ruleset', 'main', 'OrderHandler.java')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'haiku', 'baseline-no-ruleset', 'reply.md')));
    const report = fs.readFileSync(path.join(runDir, 'habit-hooks', 'email-haiku-uncle-bob-junior.md'), 'utf8');
    assert.ok(report.startsWith('fake report'));
  } finally {
    fs.rmSync(runDir, { recursive: true, force: true });
  }
});

test('slug makes filesystem-safe names', () => {
  assert.equal(exporter.slug('baseline (no ruleset)'), 'baseline-no-ruleset');
  assert.equal(exporter.slug('eval-9FT-2026-08-27T12:20:27'), 'eval-9ft-2026-08-27t12-20-27');
});

test('scan files are one per fenced block, wrapped when no type is declared', { skip: !hasHabitHooks }, () => {
  const twoBlocks = [
    '```java',
    'public boolean isValid(String email) {',
    '    return email != null && email.contains("@");',
    '}',
    '```',
    '```java',
    'public class Checker {',
    '    public void run() {}',
    '}',
    '```',
  ].join('\n');
  const scan = habitHooksAssert.scanReply(twoBlocks);
  assert.equal(scan.skipped, false);
  assert.ok(scan.report, 'a bare-method block must still produce a parseable scan');
});

test('scan artifacts like incomplete-run never cost smell score', () => {
  const filtered = habitHooksAssert.realSmells([
    { rule: 'incomplete-run', count: 1, locations: [] },
    { rule: 'oversized-function', count: 2, locations: ['A.java:3'] },
  ]);
  assert.deepEqual(filtered.map((issue) => issue.rule), ['oversized-function']);
});

test('exportRun accepts the bare results array the afterAll hook receives', () => {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-hook-test-'));
  try {
    const fakeScan = () => ({ skipped: false, report: 'fake\n', issues: [], total: 0 });
    const runDir = exporter.exportRun(FIXTURE_EVAL.evalId, FIXTURE_EVAL.results.results, { resultsDir, scan: fakeScan });
    assert.ok(runDir.endsWith(exporter.slug(FIXTURE_EVAL.evalId)));
    assert.ok(fs.existsSync(path.join(runDir, 'report.md')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'haiku', 'uncle-bob-junior', 'main', 'EmailValidator.java')));
    assert.equal(exporter.exportRun('eval-empty', [], { resultsDir }), null, 'nothing to write means no dir');
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
  }
});

test('extension hook only acts on afterAll', async () => {
  const { extensionHook } = require('../benchmarks/promptfoo-extension.js');
  await extensionHook('beforeAll', {});
  await extensionHook('afterEach', { results: undefined });
  await extensionHook('afterAll', { evalId: 'eval-empty', results: [] });
});

// One fixture per documented habit-hooks rule that fires on Java, so the
// benchmark's judge provably covers the catch list it stipulates:
// https://github.com/habit-hooks/habit-hooks#what-it-catches
const CATCH_LIST_FIXTURES = {
  'oversized-function': `\`\`\`java\npublic class Big {\n    public int work(int x) {\n${'        x = x + 1;\n'.repeat(20)}        return x;\n    }\n}\n\`\`\``,
  'too-many-parameters': '```java\npublic class Params {\n    public int add(int a, int b, int c, int d, int e2, int f2, int g2) {\n        return a + b + c + d + e2 + f2 + g2;\n    }\n}\n```',
  'high-complexity': '```java\npublic class Branchy {\n    public int decide(int x) {\n        if (x == 1) return 1;\n        if (x == 2) return 2;\n        if (x == 3) return 3;\n        if (x == 4) return 4;\n        if (x == 5) return 5;\n        if (x == 6) return 6;\n        if (x == 7) return 7;\n        if (x == 8) return 8;\n        if (x == 9) return 9;\n        if (x == 10) return 10;\n        if (x == 11) return 11;\n        return 0;\n    }\n}\n```',
  'unused-variable': '```java\npublic class Unused {\n    public int calc(int x) {\n        int leftover = x * 2;\n        return x + 1;\n    }\n}\n```',
  'unused-import': '```java\nimport java.util.List;\n\npublic class NoImports {\n    public int one() { return 1; }\n}\n```',
};

for (const [rule, fixture] of Object.entries(CATCH_LIST_FIXTURES)) {
  test(`habit-hooks catches ${rule} and fails the run (enforced)`, { skip: !hasHabitHooks }, () => {
    const scan = habitHooksAssert.scanReply(fixture);
    assert.ok(scan.issues.some((issue) => issue.rule === rule), `expected ${rule}, got: ${scan.issues.map((i) => i.rule).join(', ') || 'clean'}`);
    const verdict = habitHooksAssert(fixture);
    assert.equal(verdict.pass, false, `${rule} is enforced and must fail: ${verdict.reason}`);
    assert.ok(verdict.reason.includes(rule), verdict.reason);
  });
}

test('habit-hooks treats swallowed-exception as suggested: reported, not failed', { skip: !hasHabitHooks }, () => {
  const fixture = '```java\npublic class Swallow {\n    public int parse(String s) {\n        try {\n            return Integer.parseInt(s);\n        } catch (Exception e) {}\n        return 0;\n    }\n}\n```';
  const scan = habitHooksAssert.scanReply(fixture);
  assert.ok(scan.issues.some((issue) => issue.rule === 'swallowed-exception'), scan.issues.map((i) => i.rule).join(','));
  const verdict = habitHooksAssert(fixture);
  assert.equal(verdict.pass, true, `suggested smells are advisory: ${verdict.reason}`);
  assert.ok(verdict.score < 1, 'but they still cost score');
  assert.ok(/passed — 1 suggested/.test(verdict.reason), verdict.reason);
});

test('enforced/suggested split follows the documented catch list', () => {
  const issues = [
    { rule: 'oversized-function', count: 1, locations: [] },
    { rule: 'swallowed-exception', count: 2, locations: [] },
    { rule: 'duplicated-code', count: 1, locations: [] },
    { rule: 'incomplete-run', count: 1, locations: [] },
    { rule: 'brand-new-rule', count: 1, locations: [] },
  ];
  assert.deepEqual(
    habitHooksAssert.enforcedSmells(issues).map((issue) => issue.rule),
    ['oversized-function', 'brand-new-rule'],
    'unknown rules count as enforced, suggested and artifacts do not',
  );
});

test('re-exporting a run wipes stale files from earlier layouts', () => {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-reexport-test-'));
  try {
    const fakeScan = () => ({ skipped: false, report: 'fake\n', issues: [], total: 0 });
    const runDir = path.join(resultsDir, exporter.slug(FIXTURE_EVAL.evalId));
    fs.mkdirSync(runDir, { recursive: true });
    fs.writeFileSync(path.join(runDir, 'stale-old-layout.md'), 'left over');
    exporter.exportRun(FIXTURE_EVAL.evalId, FIXTURE_EVAL.results.results, { resultsDir, scan: fakeScan });
    assert.ok(!fs.existsSync(path.join(runDir, 'stale-old-layout.md')), 'stale files must be gone');
    assert.ok(fs.existsSync(path.join(runDir, 'report.md')));
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
  }
});

const MULTI_CLASS_JAVA = [
  'package shop;',
  '',
  'import java.util.List;',
  'import java.math.BigDecimal;',
  'import java.util.Map;',
  '',
  '/** A line item. */',
  'public class LineItem {',
  '    private final BigDecimal price;',
  '    public LineItem(BigDecimal price) { this.price = price; }',
  '}',
  '',
  '@FunctionalInterface',
  'interface Pricer {',
  '    BigDecimal price(List<LineItem> items);',
  '}',
  '',
  'class OrderProcessor {',
  '    private static final String NOTE = "has { and } inside";',
  '    public String receipt(List<LineItem> items) { return NOTE; }',
  '}',
].join('\n');

test('splitJavaTypes splits top-level types and attributes imports to their users', () => {
  const units = splitJavaTypes(MULTI_CLASS_JAVA);
  assert.deepEqual(units.map((unit) => unit.name), ['LineItem', 'Pricer', 'OrderProcessor']);
  const byName = Object.fromEntries(units.map((unit) => [unit.name, unit.code]));
  assert.ok(byName.LineItem.includes('import java.math.BigDecimal;'));
  assert.ok(!byName.LineItem.includes('import java.util.List;'), 'LineItem never uses List');
  assert.ok(byName.OrderProcessor.includes('import java.util.List;'));
  assert.ok(byName.LineItem.includes('import java.util.Map;'), 'the unused import survives, once, in the first unit');
  assert.ok(!byName.Pricer.includes('java.util.Map'), 'and only in the first unit');
  for (const unit of units) assert.ok(unit.code.startsWith('package shop;'), 'every unit keeps the package');
  assert.ok(byName.Pricer.includes('@FunctionalInterface'), 'annotations move with their type');
});

test('splitJavaTypes leaves single-type and nested-type code alone', () => {
  assert.equal(splitJavaTypes('public class Only {\n    class Inner {}\n}\n'), null, 'nested types are not split points');
  assert.equal(splitJavaTypes('int x = 1;'), null);
});

test('codeFiles splits multi-class java blocks and keeps other languages whole', () => {
  const files = extractCodeFiles([
    { lang: 'java', code: MULTI_CLASS_JAVA },
    { lang: 'python', code: 'def parse_row(row):\n    return row\n' },
  ]);
  assert.deepEqual(files.map((file) => file.name), ['LineItem.java', 'Pricer.java', 'OrderProcessor.java', 'parse_row.py']);
});

test('pluginsFor maps fence languages to habit-hooks plugins plus generic', () => {
  assert.deepEqual(pluginsFor([{ lang: 'java' }, { lang: 'python' }, { lang: 'java' }]), ['java', 'python', 'generic']);
  assert.deepEqual(pluginsFor([{ lang: '' }]), ['generic']);
});

test('exported main/ contains one file per top-level class', () => {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-split-export-'));
  try {
    const row = {
      prompt: { label: 'uncle-bob-junior' },
      provider: { label: 'haiku' },
      testCase: { description: 'order' },
      response: { output: '```java\n' + MULTI_CLASS_JAVA + '\n```' },
      gradingResult: { score: 1, componentResults: [] },
    };
    const fakeScan = () => ({ skipped: false, report: 'fake\n', issues: [], total: 0 });
    const runDir = exporter.exportRun('eval-split', [row], { resultsDir, scan: fakeScan });
    const mainDir = path.join(runDir, 'src', 'order', 'haiku', 'uncle-bob-junior', 'main');
    assert.deepEqual(fs.readdirSync(mainDir).sort(), ['LineItem.java', 'OrderProcessor.java', 'Pricer.java']);
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
  }
});

test('scanDir judges files in place and leaves no config droppings', { skip: !hasHabitHooks }, () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-scandir-'));
  try {
    fs.writeFileSync(path.join(dir, 'NoImports.java'), 'import java.util.List;\n\npublic class NoImports {\n    public int one() { return 1; }\n}\n');
    const scan = habitHooksAssert.scanDir(dir, ['java', 'generic']);
    assert.equal(scan.skipped, false);
    assert.ok(scan.issues.some((issue) => issue.rule === 'unused-import'), scan.report);
    assert.ok(!fs.existsSync(path.join(dir, '.habit-hooks')), 'config is cleaned up after the scan');
  } finally {
    fs.rmSync(dir, { recursive: true, force: true });
  }
});
