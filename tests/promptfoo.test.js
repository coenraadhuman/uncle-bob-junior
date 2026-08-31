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
  assert.ok(DELIVERY_INSTRUCTION.includes('written in the language the task asks for'), 'contract must not hardcode one language (it once forced Java onto the Python/C# tasks)');
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

for (const configName of ['promptfooconfig.yaml', 'promptfooconfig.gameoflife.yaml']) {
  test(`${configName} references only files and metric exports that exist`, () => {
    const config = fs.readFileSync(path.join(root, 'benchmarks', configName), 'utf8');
    for (const match of config.matchAll(/file:\/\/([\w./-]+?)(?::(\w+))?(?=[\s"]|$)/gm)) {
      const [, relPath, exportName] = match;
      const absPath = path.join(root, 'benchmarks', relPath);
      assert.ok(fs.existsSync(absPath), `${relPath} referenced by ${configName} must exist`);
      if (exportName) {
        assert.equal(typeof require(absPath)[exportName], 'function', `${relPath} must export ${exportName}`);
      }
    }
  });
}

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

test('provider runs the CLI without tools so a generation cannot hang on running its own answer', () => {
  const { cliArgsFor } = ClaudeCliProvider;
  const args = cliArgsFor('the task', 'fable', 'the ruleset');
  const toolsFlag = args.indexOf('--tools');
  assert.ok(toolsFlag !== -1 && args[toolsFlag + 1] === '', 'all tools disabled: single-shot text generation');
  assert.ok(args.includes('--safe-mode'));
  assert.deepEqual(args.slice(-2), ['--append-system-prompt', 'the ruleset']);
  assert.ok(!cliArgsFor('the task', 'fable', null).includes('--append-system-prompt'), 'baseline gets no system append');
});

test('provider surfaces CLI-level failures instead of scoring empty replies', () => {
  const { interpretReply } = ClaudeCliProvider;
  const ok = interpretReply({ subtype: 'success', is_error: false, result: 'the reply', total_cost_usd: 0.2, duration_ms: 1000 });
  assert.deepEqual(ok, { text: 'the reply', costUsd: 0.2, durationMs: 1000 });

  assert.match(interpretReply({ is_error: true, result: 'limit reached' }).error, /is_error: limit reached/);
  assert.match(interpretReply({ subtype: 'error_during_execution', result: '' }).error, /error_during_execution/);
  assert.match(interpretReply({ subtype: 'success', result: '   ' }).error, /empty result/);
});

test('provider id is model-scoped so promptfoo can tell the columns apart', () => {
  const provider = new ClaudeCliProvider({ config: { model: 'sonnet' }, label: 'sonnet' });
  assert.equal(provider.id(), 'claude-cli:sonnet');
  assert.equal(new ClaudeCliProvider({}).id(), 'claude-cli:haiku');
});

test('provider carries the arm in its label so the promptfoo graphs can tell the arms apart', () => {
  const { ARM_SEPARATOR } = ClaudeCliProvider;
  const provider = new ClaudeCliProvider({ config: { model: 'sonnet', arm: 'uncle-bob-junior' } });
  assert.equal(provider.id(), `claude-cli:sonnet${ARM_SEPARATOR}uncle-bob-junior`);
  assert.equal(provider.label, provider.id(), 'promptfoo displays the instance label');
  assert.equal(new ClaudeCliProvider({ config: { model: 'sonnet' } }).id(), 'claude-cli:sonnet', 'no arm, no suffix');
});

const { spawnSync } = require('child_process');
const habitHooksAssert = require('../benchmarks/habit-hooks-assert.js');
const { parseIssues } = habitHooksAssert;
const { splitJavaTypes, fileNameFor, codeFiles: extractCodeFiles, pluginsFor, isTestFile, productionFiles } = require('../benchmarks/extract-files.js');
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

test('each smell metric fails on dirty code and passes on clean code', { skip: !hasHabitHooks }, () => {
  const dirty = habitHooksAssert.oversizedFunction(DIRTY_JAVA_REPLY);
  assert.equal(dirty.pass, false, `any occurrence fails that smell's metric: ${dirty.reason}`);
  assert.ok(dirty.score < 1, dirty.reason);
  assert.ok(/oversized-function/.test(dirty.reason), dirty.reason);
  assert.equal(habitHooksAssert.unusedImport(DIRTY_JAVA_REPLY).pass, true, 'unrelated smells stay green');

  const clean = habitHooksAssert.oversizedFunction(CLEAN_JAVA_REPLY);
  assert.equal(clean.pass, true);
  assert.equal(clean.score, 1, clean.reason);
  assert.equal(habitHooksAssert.validCode(CLEAN_JAVA_REPLY).pass, true);
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
            { assertion: { metric: 'valid_code' }, pass: true, score: 1, reason: '1 source file(s) extracted' },
            { assertion: { metric: 'hh:oversized-function' }, pass: false, score: 0.25, reason: '3 oversized-function at OrderHandler.java:3' },
            { assertion: { metric: 'hh:unused-import' }, pass: true, score: 1, reason: 'no unused-import' },
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
            { assertion: { metric: 'valid_code' }, pass: true, score: 1, reason: '1 source file(s) extracted' },
            { assertion: { metric: 'hh:oversized-function' }, pass: true, score: 1, reason: 'no oversized-function' },
            { assertion: { metric: 'hh:unused-import' }, pass: true, score: 1, reason: 'no unused-import' },
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

test('exporter strips the arm suffix graph-friendly providers carry', () => {
  const { ARM_SEPARATOR } = ClaudeCliProvider;
  const [row] = exporter.resultRows([{
    prompt: { label: 'uncle-bob-junior' },
    provider: { label: `claude-cli:haiku${ARM_SEPARATOR}uncle-bob-junior` },
    testCase: { description: 'email' },
    response: { output: 'x' },
    gradingResult: { score: 1, componentResults: [] },
  }]);
  assert.equal(row.model, 'claude-cli:haiku');
  assert.equal(row.arm, 'uncle-bob-junior');
});

// One count cell per catch-list smell, in header order.
function smellCells(counts) {
  return exporter.REPORT_SMELL_COLUMNS
    .map((rule) => counts[rule] ?? 0)
    .join(' | ');
}

test('report.md carries one occurrence-count column per smell plus per-arm means', () => {
  const rows = exporter.resultRows(FIXTURE_EVAL);
  const report = exporter.buildReport(FIXTURE_EVAL.evalId, rows);
  const header = `| task | model | arm | score | valid code | habit-hooks | ${exporter.REPORT_SMELL_COLUMNS.join(' | ')} | ships tests | correct |`;
  assert.ok(report.includes(header), report);
  assert.ok(report.includes(`| email | haiku | baseline (no ruleset) | 0.40 | YES | FAIL | ${smellCells({ 'oversized-function': 3 })} | NO | YES |`), report);
  assert.ok(report.includes(`| email | haiku | uncle-bob-junior | 1.00 | YES | PASS | ${smellCells({})} | YES | YES |`), report);
  assert.ok(report.includes('**haiku / uncle-bob-junior**: 1.000 (n=1)'));
  assert.ok(report.includes('**haiku / baseline (no ruleset)**: 0.400 (n=1)'));
});

test('report.md leads with a compact table of only the smells the run hit', () => {
  const rows = exporter.resultRows(FIXTURE_EVAL);
  const report = exporter.buildReport(FIXTURE_EVAL.evalId, rows);
  const compact = report.indexOf('## Smells with hits');
  const full = report.indexOf('## Full smell breakdown');
  assert.ok(compact >= 0 && full > compact, 'compact table comes before the full one');
  const compactSection = report.slice(compact, full);
  assert.ok(compactSection.includes('| task | model | arm | score | valid code | habit-hooks | oversized-function | ships tests | correct |'), compactSection);
  assert.ok(compactSection.includes('| email | haiku | baseline (no ruleset) | 0.40 | YES | FAIL | 3 | NO | YES |'), compactSection);
  assert.ok(compactSection.includes('| email | haiku | uncle-bob-junior | 1.00 | YES | PASS | 0 | YES | YES |'), compactSection);
  assert.ok(!compactSection.includes('unused-import'), 'smells nothing hit stay out of the compact table');
});

test('report.md charts the mean score per model and arm', () => {
  const report = exporter.buildReport(FIXTURE_EVAL.evalId, exporter.resultRows(FIXTURE_EVAL));
  assert.ok(report.includes('xychart-beta'), report);
  assert.ok(report.includes('x-axis ["haiku / baseline (no ruleset)", "haiku / uncle-bob-junior"]'), report);
  assert.ok(report.includes('bar [0.400, 1.000]'), report);
});

test('cells pass on aggregate score against a threshold that keeps gates fatal', () => {
  const yaml = fs.readFileSync(path.join(__dirname, '..', 'benchmarks', 'promptfooconfig.yaml'), 'utf8');
  const threshold = Number(yaml.match(/^\s*threshold:\s*([\d.]+)/m)?.[1]);
  const totalWeight = 16;
  const gateWeight = 2;
  assert.ok(threshold > (totalWeight - gateWeight) / totalWeight, 'a failed gate must sink the cell');
  assert.ok(threshold < (totalWeight - 1) / totalWeight, 'one fully failed smell must not sink the cell');
});

test('a clean run leaves the compact table with no smell columns', () => {
  const cleanEval = {
    ...FIXTURE_EVAL,
    results: { results: FIXTURE_EVAL.results.results.slice(1) },
  };
  const report = exporter.buildReport(cleanEval.evalId, exporter.resultRows(cleanEval));
  const compactSection = report.slice(report.indexOf('## Smells with hits'), report.indexOf('## Full smell breakdown'));
  assert.ok(compactSection.includes('| task | model | arm | score | valid code | habit-hooks | ships tests | correct |'), compactSection);
  assert.ok(compactSection.includes('| email | haiku | uncle-bob-junior | 1.00 | YES | PASS | YES | YES |'), compactSection);
});

test('report smell columns cover the enforced-then-suggested catch list', () => {
  assert.equal(exporter.REPORT_SMELL_COLUMNS.length, 24);
  assert.equal(exporter.REPORT_SMELL_COLUMNS[0], 'oversized-function');
  assert.equal(exporter.ENFORCED_SMELL_COLUMNS.length, 18);
  assert.equal(exporter.SUGGESTED_SMELL_COLUMNS.length, 6);
  assert.deepEqual(
    exporter.REPORT_SMELL_COLUMNS,
    [...exporter.ENFORCED_SMELL_COLUMNS, ...exporter.SUGGESTED_SMELL_COLUMNS],
  );
});

test('legacy exports without per-smell metrics get n/a smell cells', () => {
  const legacyEval = {
    evalId: 'eval-LEG-2026-08-27T12:00:00',
    results: {
      results: [{
        prompt: { label: 'baseline (no ruleset)' },
        provider: { label: 'haiku' },
        testCase: { description: 'email' },
        response: { output: DIRTY_JAVA_REPLY },
        gradingResult: {
          score: 0.5,
          componentResults: [
            { assertion: { metric: 'habit_hooks' }, pass: false, score: 0.5, reason: '2 issues' },
          ],
        },
      }],
    },
  };
  const report = exporter.buildReport(legacyEval.evalId, exporter.resultRows(legacyEval));
  const naCells = exporter.REPORT_SMELL_COLUMNS.map(() => 'n/a').join(' | ');
  assert.ok(report.includes(`| FAIL | ${naCells} |`), report);
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

test('snippet blocks are excluded: only valid compilation units get judged', { skip: !hasHabitHooks }, () => {
  const snippetAndClass = [
    '```java',
    'Order order = new Order();',
    'System.out.println(order);',
    '```',
    '```java',
    'public class Checker {',
    '    public void run() {}',
    '}',
    '```',
  ].join('\n');
  const scan = habitHooksAssert.scanReply(snippetAndClass);
  assert.equal(scan.fileCount, 1, 'the statements-only block is a snippet and must be excluded');
  assert.ok(!scan.issues.some((issue) => issue.rule === 'incomplete-run'), scan.report);
  assert.equal(habitHooksAssert.validCode(snippetAndClass).pass, true);

  const snippetOnly = '```java\nSystem.out.println("just output");\n```';
  const verdict = habitHooksAssert.validCode(snippetOnly);
  assert.equal(verdict.pass, false, 'a reply with no valid compilation unit fails valid_code');
  assert.equal(habitHooksAssert.oversizedFunction(snippetOnly).pass, true, 'smell metrics do not double-punish');
});

test('scan artifacts have no metric, so they never cost score', () => {
  const rules = [...habitHooksAssert.ENFORCED_RULES, ...habitHooksAssert.SUGGESTED_RULES];
  assert.ok(!rules.includes('incomplete-run'));
  assert.ok(!rules.includes('parse-error'));
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

test('extension hook rebuilds the site after a successful export, and site failures stay non-fatal', async () => {
  const { extensionHook } = require('../benchmarks/promptfoo-extension.js');
  const calls = [];
  await extensionHook('afterAll', { evalId: 'eval-x', results: [] }, {
    doExport: () => '/tmp/run-dir',
    doBuildSite: (evalId) => { calls.push(evalId); return '/tmp/docs'; },
  });
  assert.deepEqual(calls, ['eval-x']);

  await extensionHook('afterAll', { evalId: 'eval-x', results: [] }, {
    doExport: () => '/tmp/run-dir',
    doBuildSite: () => { throw new Error('boom'); },
  }); // must not throw

  const skipped = [];
  await extensionHook('afterAll', { evalId: 'eval-x', results: [] }, {
    doExport: () => null,
    doBuildSite: () => skipped.push('built'),
  });
  assert.deepEqual(skipped, [], 'no export, no site rebuild');
});

test('report.json carries rows with gates and smell counts plus means', () => {
  const data = exporter.runData(FIXTURE_EVAL.evalId, exporter.resultRows(FIXTURE_EVAL));
  assert.equal(data.evalId, FIXTURE_EVAL.evalId);
  const [baseline, ruleset] = data.rows;
  assert.equal(baseline.arm, 'baseline (no ruleset)');
  assert.equal(baseline.gates.shipsTests, false);
  assert.equal(baseline.gates.correct, true);
  assert.equal(baseline.habitPass, false);
  assert.equal(baseline.smellCounts['oversized-function'], 3);
  assert.equal(baseline.smellCounts['unused-import'], 0);
  assert.equal(ruleset.habitPass, true);
  assert.deepEqual(data.means.map((m) => m.mean), [0.4, 1]);
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
  test(`habit-hooks catches ${rule} on its own metric`, { skip: !hasHabitHooks }, () => {
    const scan = habitHooksAssert.scanReply(fixture);
    assert.ok(scan.issues.some((issue) => issue.rule === rule), `expected ${rule}, got: ${scan.issues.map((i) => i.rule).join(', ') || 'clean'}`);
    const camelRule = rule.replace(/-(\w)/g, (_, c) => c.toUpperCase());
    const verdict = habitHooksAssert[camelRule](fixture);
    assert.equal(verdict.pass, false, `${rule} must fail its own metric: ${verdict.reason}`);
    assert.ok(verdict.reason.includes(rule), verdict.reason);
  });
}

test('swallowed-exception fails its own metric too; its tier is the weight', { skip: !hasHabitHooks }, () => {
  const fixture = '```java\npublic class Swallow {\n    public int parse(String s) {\n        try {\n            return Integer.parseInt(s);\n        } catch (Exception e) {}\n        return 0;\n    }\n}\n```';
  const verdict = habitHooksAssert.swallowedException(fixture);
  assert.equal(verdict.pass, false, verdict.reason);
  assert.ok(/1 swallowed-exception/.test(verdict.reason), verdict.reason);
});

test('config weights: every smell has a metric, suggested at half the enforced weight', () => {
  const config = fs.readFileSync(path.join(root, 'benchmarks', 'promptfooconfig.yaml'), 'utf8');
  for (const rule of habitHooksAssert.ENFORCED_RULES) {
    const line = config.split('\n').find((l) => l.includes(`hh:${rule}`));
    assert.ok(line, `enforced ${rule} must be a config metric`);
    assert.ok(line.includes('weight: 1'), `${rule} weighs 1: ${line}`);
  }
  for (const rule of habitHooksAssert.SUGGESTED_RULES) {
    const line = config.split('\n').find((l) => l.includes(`hh:${rule}`));
    assert.ok(line, `suggested ${rule} must be a config metric`);
    assert.ok(line.includes('weight: 0.5'), `${rule} weighs 0.5: ${line}`);
  }
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

test('splitJavaTypes attributes wildcard imports to the units that use the package', () => {
  const units = splitJavaTypes([
    'import java.util.*;',
    'import java.io.*;',
    'import com.example.legacy.*;',
    '',
    'public class Store {',
    '    private final Map<String, String> entries = new HashMap<>();',
    '}',
    '',
    'class Config {',
    '    private final int port = 8080;',
    '}',
  ].join('\n'));
  const byName = Object.fromEntries(units.map((unit) => [unit.name, unit.code]));
  assert.ok(byName.Store.includes('import java.util.*;'), 'Store uses Map/HashMap');
  assert.ok(!byName.Config.includes('import java.util.*;'), 'Config uses nothing from java.util');
  assert.ok(byName.Store.includes('import java.io.*;'), 'a known wildcard nothing uses survives once, in the first unit');
  assert.ok(!byName.Config.includes('import java.io.*;'));
  assert.ok(byName.Config.includes('import com.example.legacy.*;'), 'unknown packages still go to every unit');
});

test('python and C# test code is recognized as tests, not production', () => {
  const { isTestBlock, shipsTests } = require('../benchmarks/promptfoo-metrics.js');
  assert.equal(isTestBlock('import pytest\n\ndef test_sum():\n    assert analyse([]) == {}'), true, 'pytest module is a test block');
  assert.equal(isTestBlock('using Xunit;\n\npublic class ExpenseTests {\n    [Fact]\n    public void RejectsNegative() { }\n}'), true, 'xUnit class is a test block');
  assert.equal(isTestBlock('def analyse(lines):\n    return {}'), false, 'plain python is production');

  const csharpReply = '```csharp\npublic class Processor { }\n```\n\n```csharp\nusing Xunit;\npublic class ProcessorTests { [Fact] public void Works() { Assert.Equal(1, 1); } }\n```';
  assert.equal(shipsTests(csharpReply).pass, true, 'C# xUnit tests count as shipped tests');
  assert.equal(isTestFile({ name: 'test_analyser.py', content: 'def test_x(): pass' }), true, 'pytest file name marks a test file');
  assert.equal(isTestFile({ name: 'Anything.cs', content: '[Fact]\npublic void X() {}' }), true, 'xUnit attribute marks a test file');
});

test('C# blocks get .cs files named after their type and only the generic plugin', () => {
  const files = extractCodeFiles([{ lang: 'csharp', code: 'using System;\n\npublic class ExpenseProcessor { }' }]);
  assert.deepEqual(files.map((f) => f.name), ['ExpenseProcessor.cs']);
  assert.deepEqual(pluginsFor([{ lang: 'csharp' }]), ['generic'], 'no habit-hooks C# plugin: generic only');
  assert.deepEqual(pluginsFor([{ lang: 'python' }]), ['python', 'generic']);
});

test('a single mixed file (implementation plus tests) stays production instead of vanishing', () => {
  const blocks = [{
    lang: 'python',
    code: 'import pytest\n\ndef analyse(lines):\n    return {}\n\ndef test_analyse_empty():\n    assert analyse([]) == {}\n',
  }];
  assert.deepEqual(productionFiles(blocks).map((f) => f.name), ['analyse.py'],
    'filtering must never leave the judge with nothing to scan');
});

test('a mixed single-file reply exports to main/, not an empty directory', () => {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-mixed-export-'));
  try {
    const row = {
      prompt: { label: 'uncle-bob-junior' },
      provider: { label: 'haiku' },
      testCase: { description: 'logscan' },
      response: { output: '```python\nimport pytest\n\ndef analyse(lines):\n    return {}\n\ndef test_analyse_empty():\n    assert analyse([]) == {}\n```' },
      gradingResult: { score: 1, componentResults: [] },
    };
    const fakeScan = () => ({ skipped: false, report: 'fake\n', issues: [], total: 0 });
    const runDir = exporter.exportRun('eval-mixed', [row], { resultsDir, scan: fakeScan });
    const armDir = path.join(runDir, 'src', 'logscan', 'haiku', 'uncle-bob-junior');
    assert.deepEqual(fs.readdirSync(path.join(armDir, 'main')), ['analyse.py']);
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
  }
});

test('scanDir leaves no sensor cache behind in the scanned directory', { skip: !hasHabitHooks }, () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-ruff-cache-'));
  try {
    fs.writeFileSync(path.join(dir, 'analyser.py'), 'import os\n\ndef run():\n    return 1\n');
    habitHooksAssert.scanDir(dir, ['python']);
    assert.deepEqual(fs.readdirSync(dir), ['analyser.py'], 'only the scanned source may remain');
  } finally {
    fs.rmSync(dir, { recursive: true, force: true });
  }
});

test('a test class inside a production block is filtered from the judged files', () => {
  const blocks = [{
    lang: 'java',
    code: [
      'public class Analyzer {',
      '    public int run() { return 1; }',
      '}',
      '',
      'class AnalyzerTest {',
      '    static void runTests() { assert new Analyzer().run() == 1; }',
      '}',
    ].join('\n'),
  }];
  assert.deepEqual(extractCodeFiles(blocks).map((f) => f.name), ['Analyzer.java', 'AnalyzerTest.java']);
  assert.deepEqual(productionFiles(blocks).map((f) => f.name), ['Analyzer.java']);
  assert.equal(isTestFile({ name: 'Anything.java', content: 'import org.junit.Test;' }), true, 'junit marks a test file regardless of name');
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

test('a test class inside a production block is exported to test/, not main/', () => {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-stray-test-export-'));
  try {
    const row = {
      prompt: { label: 'uncle-bob-junior' },
      provider: { label: 'haiku' },
      testCase: { description: 'statement' },
      response: { output: '```java\npublic class Analyzer {\n    public int run() { return 1; }\n}\n\nclass AnalyzerTest {\n    static void runTests() { }\n}\n```' },
      gradingResult: { score: 1, componentResults: [] },
    };
    const fakeScan = () => ({ skipped: false, report: 'fake\n', issues: [], total: 0 });
    const runDir = exporter.exportRun('eval-stray', [row], { resultsDir, scan: fakeScan });
    const armDir = path.join(runDir, 'src', 'statement', 'haiku', 'uncle-bob-junior');
    assert.deepEqual(fs.readdirSync(path.join(armDir, 'main')), ['Analyzer.java']);
    assert.deepEqual(fs.readdirSync(path.join(armDir, 'test')), ['AnalyzerTest.java']);
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
  }
});

const gameoflife = require('../benchmarks/gameoflife-examples.js');

const GAMEOFLIFE_ROW = (model, arm, output) => ({
  prompt: { label: arm },
  provider: { label: model },
  testCase: { description: gameoflife.GAME_OF_LIFE_TASK },
  response: { output },
});

const GAMEOFLIFE_EVAL_RESULTS = [
  GAMEOFLIFE_ROW('haiku', 'baseline (no ruleset)', '# Game of Life\n```java\npublic class Gol {}\n```'),
  GAMEOFLIFE_ROW('haiku', 'uncle-bob-junior', '# Game of Life\n```java\npublic class GameOfLife {}\n```'),
  GAMEOFLIFE_ROW('fable', 'uncle-bob-junior', '```java\npublic final class Life {}\n```'),
  { ...GAMEOFLIFE_ROW('sonnet', 'uncle-bob-junior', 'unrelated reply'), testCase: { description: 'email' } },
  GAMEOFLIFE_ROW('sonnet', 'uncle-bob-junior', '   '),
];

test('gameoflife export stores one reply.md per model and arm, gameoflife rows only', () => {
  const examplesDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-examples-test-'));
  try {
    const written = gameoflife.exportExamples(GAMEOFLIFE_EVAL_RESULTS, { examplesDir });
    assert.deepEqual(written, [
      path.join(examplesDir, 'haiku', 'baseline-no-ruleset', 'reply.md'),
      path.join(examplesDir, 'haiku', 'uncle-bob-junior', 'reply.md'),
      path.join(examplesDir, 'fable', 'uncle-bob-junior', 'reply.md'),
    ]);
    assert.ok(fs.readFileSync(written[0], 'utf8').includes('class Gol'), 'baseline reply survives next to the ruleset reply');
    assert.ok(fs.readFileSync(written[1], 'utf8').includes('class GameOfLife'), 'arms of one model must not overwrite each other');
    assert.deepEqual(fs.readdirSync(path.join(examplesDir, 'haiku', 'uncle-bob-junior')), ['reply.md'], 'reply.md is the only artifact');
    assert.ok(!fs.existsSync(path.join(examplesDir, 'sonnet')), 'other tasks and blank replies are skipped');
  } finally {
    fs.rmSync(examplesDir, { recursive: true, force: true });
  }
});

test('gameoflife export accepts a full eval export JSON too', () => {
  const examplesDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-examples-json-'));
  try {
    const written = gameoflife.exportExamples({ results: { results: GAMEOFLIFE_EVAL_RESULTS } }, { examplesDir });
    assert.equal(written.length, 3);
  } finally {
    fs.rmSync(examplesDir, { recursive: true, force: true });
  }
});

test('gameoflife extension hook only acts on afterAll and never fails the eval', async () => {
  await gameoflife.extensionHook('beforeAll', {});
  await gameoflife.extensionHook('afterEach', { results: undefined });
  // afterAll with unusable results must be swallowed, not thrown.
  await gameoflife.extensionHook('afterAll', {});
});

test('gameoflife config is standalone: both arms, examples export, no judges', () => {
  const config = fs.readFileSync(path.join(root, 'benchmarks', 'promptfooconfig.gameoflife.yaml'), 'utf8');
  assert.ok(config.includes('gameoflife-examples.js:extensionHook'), 'exports replies to examples/');
  assert.ok(!config.includes('promptfoo-extension.js'), 'must not export to results/');
  assert.ok(!config.includes('defaultTest'), 'no judges: the replies are the deliverable');
  assert.ok(config.includes('arms/uncle-bob-junior.js'));
  assert.ok(config.includes('arms/baseline.js'), 'baseline arm generates the comparison reply');
  assert.ok(config.includes(`description: ${gameoflife.GAME_OF_LIFE_TASK}\n`), 'task description must match the exporter filter');
  for (const requirement of ["Conway's Game of Life", 'Maven', 'pom.xml', 'terminal', 'stdout']) {
    assert.ok(config.includes(requirement), `task must demand: ${requirement}`);
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
