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
const { parseIssues, javaFileName } = habitHooksAssert;
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

test('java scan files are named after their declared type', () => {
  assert.equal(javaFileName('public final class EmailValidator {\n}', 0), 'EmailValidator.java');
  assert.equal(javaFileName('interface Store { void save(); }', 3), 'Store.java');
  assert.equal(javaFileName('int x = 1;', 2), 'Snippet3.java');
});

test('habit-hooks judges dirty code down and clean code as clean', { skip: !hasHabitHooks }, () => {
  const dirty = habitHooksAssert(DIRTY_JAVA_REPLY);
  assert.equal(dirty.pass, true, 'penalty, never a gate');
  assert.ok(dirty.score < 1, `26-line method must cost score: ${dirty.reason}`);
  assert.ok(/oversized-function/.test(dirty.reason), dirty.reason);

  const clean = habitHooksAssert(CLEAN_JAVA_REPLY);
  assert.equal(clean.score, 1, clean.reason);
  assert.equal(clean.reason, 'habit-hooks: clean');
});

const os = require('os');
const exporter = require('../benchmarks/export-results.js');

const FIXTURE_EVAL = {
  evalId: 'eval-FIX-2026-08-27T12:00:00',
  results: {
    results: [
      {
        prompt: { label: 'baseline (no ruleset)' },
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
  assert.ok(report.includes('| email | baseline (no ruleset) | 0.40 |'));
  assert.ok(report.includes('habit-hooks: clean'));
  assert.ok(report.includes('**uncle-bob-junior**: 1.000 (n=1)'));
  assert.ok(report.includes('**baseline (no ruleset)**: 0.400 (n=1)'));
});

test('writeRunArtifacts lays out src, habit-hooks reports, and report.md', () => {
  const runDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-export-test-'));
  try {
    const rows = exporter.resultRows(FIXTURE_EVAL);
    const fakeScan = (output) => ({ skipped: false, report: `fake report for ${output.length} chars\n`, issues: [], total: 0 });
    exporter.writeRunArtifacts(FIXTURE_EVAL.evalId, rows, runDir, { scan: fakeScan });

    assert.ok(fs.existsSync(path.join(runDir, 'report.md')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'uncle-bob-junior', 'EmailValidator.java')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'uncle-bob-junior', 'EmailValidatorTest.java')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'baseline-no-ruleset', 'OrderHandler.java')));
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'baseline-no-ruleset', 'reply.md')));
    const report = fs.readFileSync(path.join(runDir, 'habit-hooks', 'email-uncle-bob-junior.md'), 'utf8');
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
    assert.ok(fs.existsSync(path.join(runDir, 'src', 'email', 'uncle-bob-junior', 'EmailValidator.java')));
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
