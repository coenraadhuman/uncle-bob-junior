#!/usr/bin/env node
// Unit tests for the showcase-site content generator: MDX emission from a
// fixture run directory, MDX escaping of model-generated text, findings
// rendering, newest-run selection, stale-page cleanup, and the checklist on
// the landing page. Rendering MDX to HTML is Docusaurus's job (website/),
// deliberately not exercised here.

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const { buildSite, newestRunId, checklistItems, mdxEscape, hitSmells } = require('../benchmarks/build-site.js');

const RUN_DATA = {
  evalId: 'eval-FIX-2026-08-29T10:00:00',
  rows: [
    {
      task: 'order', model: 'claude-cli:haiku', arm: 'baseline (no ruleset)',
      prompt: 'Process an order with <VAT> rules.',
      score: 0.8,
      gates: { validCode: true, shipsTests: false, correct: true },
      habitPass: false,
      smellCounts: { 'oversized-function': 2, 'too-many-parameters': 0, 'unused-import': 0 },
    },
    {
      task: 'order', model: 'claude-cli:haiku', arm: 'uncle-bob-junior',
      prompt: 'Process an order with <VAT> rules.',
      score: 1,
      gates: { validCode: true, shipsTests: true, correct: true },
      habitPass: true,
      smellCounts: { 'oversized-function': 0, 'too-many-parameters': 0, 'unused-import': 0 },
    },
  ],
  means: [
    { key: 'claude-cli:haiku / baseline (no ruleset)', mean: 0.8, n: 1 },
    { key: 'claude-cli:haiku / uncle-bob-junior', mean: 1, n: 1 },
  ],
};

const HABIT_HOOKS_REPORT = [
  '── oversized-function (2 issues) ──',
  '',
  'Functions over 12 lines carry more than one responsibility.',
  '',
  'OrderProcessor.java:5',
  'OrderProcessor.java:30',
].join('\n');

// A run directory shaped like export-results.js writes it.
function writeFixtureRun(resultsDir, runId, data = RUN_DATA) {
  const runDir = path.join(resultsDir, runId);
  const armDir = (arm) => path.join(runDir, 'src', 'order', 'claude-cli-haiku', arm);
  fs.mkdirSync(path.join(armDir('baseline-no-ruleset'), 'main'), { recursive: true });
  fs.mkdirSync(path.join(armDir('uncle-bob-junior'), 'main'), { recursive: true });
  fs.mkdirSync(path.join(armDir('uncle-bob-junior'), 'test'), { recursive: true });
  fs.mkdirSync(path.join(runDir, 'habit-hooks'), { recursive: true });
  fs.writeFileSync(path.join(armDir('baseline-no-ruleset'), 'main', 'OrderProcessor.java'),
    'public class OrderProcessor { List<Item> items; }');
  fs.writeFileSync(path.join(armDir('uncle-bob-junior'), 'main', 'Order.java'), 'public record Order() {}');
  fs.writeFileSync(path.join(armDir('uncle-bob-junior'), 'test', 'OrderTest.java'), 'class OrderTest {}');
  fs.writeFileSync(path.join(runDir, 'habit-hooks', 'order-claude-cli-haiku-baseline-no-ruleset.md'), HABIT_HOOKS_REPORT);
  fs.writeFileSync(path.join(runDir, 'report.json'), JSON.stringify(data));
  return runDir;
}

function inTempDirs(run) {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-site-results-'));
  const siteDocsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-site-docs-'));
  try {
    return run(resultsDir, siteDocsDir);
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
    fs.rmSync(siteDocsDir, { recursive: true, force: true });
  }
}

test('buildSite emits the landing, scoreboard, and task MDX pages', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    buildSite('eval-FIX-2026-08-29T10:00:00', { resultsDir, siteDocsDir });
    for (const file of ['index.mdx', path.join('benchmark', 'scoreboard.mdx'), path.join('benchmark', 'order.mdx')]) {
      assert.ok(fs.existsSync(path.join(siteDocsDir, file)), `${file} missing`);
    }
  });
});

test('task page carries both arms as tabs with fenced code and findings at their lines', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    buildSite('eval-FIX-2026-08-29T10:00:00', { resultsDir, siteDocsDir });
    const page = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'order.mdx'), 'utf8');
    assert.ok(page.includes("import Tabs from '@theme/Tabs'"), 'uses the Docusaurus Tabs component');
    assert.ok(page.includes('label="baseline (no ruleset) · 0.80"') && page.includes('label="uncle-bob-junior · 1.00"'), 'both arms present with scores');
    assert.ok(page.includes('````java\npublic class OrderProcessor { List<Item> items; }\n````'), 'code stays verbatim inside four-backtick fences');
    assert.ok(page.includes('Process an order with &lt;VAT&gt; rules.'), 'task prompt is MDX-escaped');
    assert.ok(page.includes('`oversized-function` at line 5'), 'findings rendered at their lines');
    assert.ok(page.includes('OrderTest.java'), 'shipped tests are on the page');
    assert.ok(page.includes('*No tests shipped.*'), 'the shipless arm says so');
  });
});

test('scoreboard page charts the means with mermaid and keeps only hit smells as columns', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    buildSite('eval-FIX-2026-08-29T10:00:00', { resultsDir, siteDocsDir });
    const page = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'scoreboard.mdx'), 'utf8');
    assert.ok(page.includes('xychart-beta'), 'mermaid chart present');
    assert.ok(page.includes('bar [0.800, 1.000]'), 'bars carry the means');
    assert.ok(page.includes('| oversized-function |'), 'hit smell is a column');
    assert.ok(!page.includes('unused-import'), 'unhit smells stay out');
    assert.ok(page.includes('[order](order)'), 'task links to its page');
  });
});

test('newest run is picked by directory timestamp, ignoring runs without report.json', () => {
  inTempDirs((resultsDir) => {
    writeFixtureRun(resultsDir, 'eval-zzz-2026-08-28t09-00-00');
    writeFixtureRun(resultsDir, 'eval-aaa-2026-08-29t10-00-00');
    fs.mkdirSync(path.join(resultsDir, 'eval-new-2026-08-30t10-00-00')); // no report.json
    assert.equal(newestRunId(resultsDir), 'eval-aaa-2026-08-29t10-00-00');
  });
});

test('a rebuild wipes task pages from an earlier run but keeps hand-written docs', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    fs.writeFileSync(path.join(siteDocsDir, 'notes.md'), 'hand-written');
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    buildSite(undefined, { resultsDir, siteDocsDir });
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'order.mdx')));
    const emailOnly = { ...RUN_DATA, rows: RUN_DATA.rows.map((row) => ({ ...row, task: 'email' })) };
    writeFixtureRun(resultsDir, 'eval-new-2026-08-30t10-00-00', emailOnly);
    buildSite(undefined, { resultsDir, siteDocsDir });
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'email.mdx')), 'new task page written');
    assert.ok(!fs.existsSync(path.join(siteDocsDir, 'benchmark', 'order.mdx')), 'stale task page removed');
    assert.equal(fs.readFileSync(path.join(siteDocsDir, 'notes.md'), 'utf8'), 'hand-written');
  });
});

test('buildSite without an id builds the newest run, and a missing report.json is a clear error', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    assert.ok(buildSite(undefined, { resultsDir, siteDocsDir }));
    assert.throws(() => buildSite('eval-gone-2026-01-01t00-00-00', { resultsDir, siteDocsDir }), /re-export/);
  });
});

test('landing page carries the checklist from SKILL.md', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    buildSite(undefined, { resultsDir, siteDocsDir });
    const page = fs.readFileSync(path.join(siteDocsDir, 'index.mdx'), 'utf8');
    assert.ok(page.includes('**One job each.**'), 'first checklist item rendered');
    assert.ok(page.includes('**Libraries over wheels.**'), 'last checklist item rendered');
    assert.ok(page.includes('duty to search'), 'the search duty ships to the site');
  });
});

test('sensor droppings inside main/ are not rendered as source files', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    const runDir = writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00');
    fs.mkdirSync(path.join(runDir, 'src', 'order', 'claude-cli-haiku', 'baseline-no-ruleset', 'main', '.ruff_cache'));
    buildSite(undefined, { resultsDir, siteDocsDir });
    const page = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'order.mdx'), 'utf8');
    assert.ok(!page.includes('.ruff_cache'), 'cache directories must not appear on the page');
  });
});

test('repeated rows from a --repeat run collapse to one tab per arm', () => {
  inTempDirs((resultsDir, siteDocsDir) => {
    const repeated = { ...RUN_DATA, rows: [...RUN_DATA.rows, ...RUN_DATA.rows] };
    writeFixtureRun(resultsDir, 'eval-fix-2026-08-29t10-00-00', repeated);
    buildSite(undefined, { resultsDir, siteDocsDir });
    const page = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'order.mdx'), 'utf8');
    assert.equal(page.match(/value="uncle-bob-junior"/g).length, 1, 'one tab per arm despite repetitions');
  });
});

test('helpers: checklist extraction, MDX escaping, hit smells', () => {
  const items = checklistItems('---\nname: x\n---\n## The checklist\n\nintro\n1. **A.** uses `code`\n2. plain\n\n## Rules\n3. not me');
  assert.deepEqual(items, ['**A.** uses `code`', 'plain']);
  assert.equal(mdxEscape('<a> & {expr}'), '&lt;a&gt; &amp; &#123;expr&#125;');
  assert.deepEqual(hitSmells(RUN_DATA.rows), ['oversized-function']);
});
