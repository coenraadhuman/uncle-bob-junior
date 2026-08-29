#!/usr/bin/env node
// Export a promptfoo eval into benchmarks/results/<eval-id>/ so run outcomes
// live next to the repo instead of only inside promptfoo's local database:
//
//   node benchmarks/export-results.js [evalId]   # defaults to the newest eval
//
// Each run directory contains:
//   report.md                                    scoreboard per task, model, and arm
//   src/<task>/<model>/<arm>/main/<Type>.java    production code, one file per top-level type
//   src/<task>/<model>/<arm>/test/<Type>.java    the shipped tests, when any
//   src/<task>/<model>/<arm>/reply.md            the full model reply
//   habit-hooks/<task>-<model>-<arm>.md          habit-hooks run on that answer's main/
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const { fencedBlocks, productionBlocks, isTestBlock, extractCode } = require('./promptfoo-metrics');
const { codeFiles, isTestFile, pluginsFor } = require('./extract-files');
const { scanDir } = require('./habit-hooks-assert');
const { ARM_SEPARATOR } = require('./providers/claude-cli');

const RESULTS_DIR = path.join(__dirname, 'results');

function slug(text) {
  return String(text || 'unknown').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

function promptfoo(args) {
  return execFileSync('npx', ['-y', 'promptfoo@latest', ...args], {
    encoding: 'utf8',
    env: { ...process.env, CI: 'true', PROMPTFOO_DISABLE_TELEMETRY: '1' },
    maxBuffer: 64 * 1024 * 1024,
  });
}

function newestEvalId() {
  const listing = promptfoo(['list', 'evals', '-n', '1']);
  const match = listing.match(/eval-[A-Za-z0-9]+-[0-9T:-]+/);
  if (!match) throw new Error('No promptfoo evals found. Run the benchmark first.');
  return match[0];
}

function loadEval(evalId) {
  const tmp = path.join(os.tmpdir(), `ubj-eval-${Date.now()}.json`);
  try {
    promptfoo(['export', 'eval', evalId, '-o', tmp]);
    return JSON.parse(fs.readFileSync(tmp, 'utf8'));
  } finally {
    fs.rmSync(tmp, { force: true });
  }
}

// One flat row per task × arm, everything report.md and the writers need.
// Accepts a promptfoo export (`{results: {results: [...]}}`) or the bare
// results array the afterAll extension hook receives — same row shape.
function resultRows(data) {
  const list = Array.isArray(data) ? data : (data.results?.results || []);
  return list.map((r) => ({
    arm: r.prompt?.label || 'unknown-arm',
    // Providers carry the arm in their label for the promptfoo graphs; the
    // report has its own arm column, so only the model half is kept here.
    model: String(r.provider?.label || r.provider?.id || 'unknown-model').split(ARM_SEPARATOR)[0],
    task: r.testCase?.description || slug(r.vars?.task).slice(0, 40),
    output: String(r.response?.output || ''),
    score: r.gradingResult?.score ?? 0,
    components: (r.gradingResult?.componentResults || []).map((c) => ({
      metric: c.assertion?.metric || 'unknown',
      pass: Boolean(c.pass),
      score: c.score,
      reason: c.reason || '',
    })),
  }));
}

// The full habit-hooks catch list, one report column per smell (enforced
// first, then suggested). Wider than the language-restricted assert lists in
// habit-hooks-assert.js on purpose: the scoreboard shows every smell the
// tool can catch, so a 0 means "scanned, none found" for any language.
const ENFORCED_SMELL_COLUMNS = [
  'oversized-function', 'too-many-parameters', 'high-complexity', 'deep-nesting',
  'oversized-file', 'unused-variable', 'unused-import', 'loose-equality',
  'var-declaration', 'non-const-binding', 'duplicate-import', 'redundant-type-annotation',
  'unused-class-member', 'unused-file', 'unused-export', 'unused-dependency',
  'test-only-dead-code', 'parse-error',
];
const SUGGESTED_SMELL_COLUMNS = [
  'warning-comment', 'explicit-any', 'non-null-assertion', 'non-essential-comment',
  'duplicated-code', 'swallowed-exception',
];
const REPORT_SMELL_COLUMNS = [...ENFORCED_SMELL_COLUMNS, ...SUGGESTED_SMELL_COLUMNS];

// A smell metric's reason is `no <rule>` on pass or `<count> <rule> at
// <locations>` on fail, so the occurrence count is the leading integer.
function smellCount(component) {
  const count = Number.parseInt(component?.reason ?? '', 10);
  return Number.isNaN(count) ? 0 : count;
}

function reportHeaderLines(smellColumns) {
  const header = ['task', 'model', 'arm', 'score', 'valid code', 'habit-hooks', ...smellColumns, 'ships tests', 'correct'];
  const alignment = header.map((column) => {
    if (column === 'score' || smellColumns.includes(column)) return '---:';
    if (['valid code', 'habit-hooks', 'ships tests', 'correct'].includes(column)) return ':---:';
    return '---';
  });
  return [`| ${header.join(' | ')} |`, `| ${alignment.join(' | ')} |`];
}

function rowSmellCount(row, rule) {
  return smellCount(row.components.find((c) => c.metric === `hh:${rule}`));
}

function reportRow(row, smellColumns) {
  const metric = (name) => row.components.find((c) => c.metric === name);
  // One component per smell (hh:*); older exports carry a single
  // habit_hooks component instead, whose reason has no per-smell counts.
  const hasSmellMetrics = row.components.some((c) => c.metric.startsWith('hh:'));
  const habitPass = hasSmellMetrics
    ? row.components.filter((c) => c.metric.startsWith('hh:')).every((c) => c.pass)
    : metric('habit_hooks')?.pass;
  const counts = smellColumns.map((rule) => (hasSmellMetrics ? rowSmellCount(row, rule) : 'n/a'));
  const gate = (name) => (metric(name)?.pass ? 'YES' : 'NO');
  return `| ${row.task} | ${row.model} | ${row.arm} | ${row.score.toFixed(2)} | ` +
    `${metric('valid_code') ? (metric('valid_code').pass ? 'YES' : 'NO') : 'n/a'} | ` +
    `${habitPass === undefined ? 'n/a' : habitPass ? 'PASS' : 'FAIL'} |` +
    `${counts.map((count) => ` ${count} |`).join('')} ` +
    `${gate('ships_tests')} | ${gate('correct')} |`;
}

// The compact table's columns: only the smells at least one row hit.
function hitSmellColumns(rows) {
  return REPORT_SMELL_COLUMNS.filter((rule) => rows.some((row) => rowSmellCount(row, rule) > 0));
}

function scoreboardLines(rows, smellColumns) {
  return [...reportHeaderLines(smellColumns), ...rows.map((row) => reportRow(row, smellColumns))];
}

function meanScores(rows) {
  const armTotals = new Map();
  for (const row of rows) {
    const key = `${row.model} / ${row.arm}`;
    const totals = armTotals.get(key) || { sum: 0, n: 0 };
    totals.sum += row.score;
    totals.n += 1;
    armTotals.set(key, totals);
  }
  return [...armTotals].map(([key, totals]) => ({ key, mean: totals.sum / totals.n, n: totals.n }));
}

function meanScoreLines(rows) {
  return meanScores(rows).map(({ key, mean, n }) => `- **${key}**: ${mean.toFixed(3)} (n=${n})`);
}

// The pass-rate view is binary per cell, so the graded comparison the scores
// carry (0.88 vs 1.00) only shows up in a chart of the means themselves.
function meanScoreChartLines(rows) {
  const means = meanScores(rows);
  return [
    '```mermaid',
    'xychart-beta',
    '    title "Mean score per model and arm"',
    `    x-axis [${means.map(({ key }) => `"${key}"`).join(', ')}]`,
    '    y-axis "mean score" 0 --> 1',
    `    bar [${means.map(({ mean }) => mean.toFixed(3)).join(', ')}]`,
    '```',
  ];
}

function buildReport(evalId, rows) {
  return [
    `# Benchmark run ${evalId}`,
    '',
    'Judges: one habit-hooks metric per code smell (0 occurrences = pass;',
    'suggested smells carry half the weight of enforced ones), plus the',
    'valid_code, ships_tests, and correct gates. Higher score = cleaner.',
    'Each smell column holds the occurrence count (enforced smells first,',
    'then suggested). The first table keeps only the smells with at least',
    'one hit across the run; the second carries the full catch list. File',
    'and line locations live in the `habit-hooks/` reports next to this',
    'file; the generated code sits in `src/`.',
    '',
    '## Smells with hits',
    '',
    ...scoreboardLines(rows, hitSmellColumns(rows)),
    '',
    '## Full smell breakdown',
    '',
    ...scoreboardLines(rows, REPORT_SMELL_COLUMNS),
    '',
    '## Mean score per model and arm',
    '',
    ...meanScoreLines(rows),
    '',
    ...meanScoreChartLines(rows),
  ].join('\n') + '\n';
}

function writeFiles(files, dir) {
  if (files.length === 0) return;
  fs.mkdirSync(dir, { recursive: true });
  for (const file of files) {
    fs.writeFileSync(path.join(dir, file.name), file.content);
  }
}

// The scan option exists so tests can inject a fake instead of the real CLI.
// The run dir is wiped first: it is fully derived from the eval, so a
// re-export must not leave files from an earlier layout behind. habit-hooks
// runs directly on the exported `main/` files — the report's File:line
// references point at files you can open — with the plugins matching the
// languages the answer used.
function writeRunArtifacts(evalId, rows, runDir, { scan = scanDir } = {}) {
  fs.rmSync(runDir, { recursive: true, force: true });
  fs.mkdirSync(path.join(runDir, 'habit-hooks'), { recursive: true });
  for (const row of rows) {
    const armDir = path.join(runDir, 'src', slug(row.task), slug(row.model), slug(row.arm));
    fs.mkdirSync(armDir, { recursive: true });
    fs.writeFileSync(path.join(armDir, 'reply.md'), row.output);

    // Mirror the judge's fallback: an unfenced reply is still one scannable file.
    const production = productionBlocks(row.output);
    if (production.length === 0) production.push(extractCode(row.output));
    const testBlocks = fencedBlocks(row.output).filter((block) => isTestBlock(block.code));
    // A test class kept inside a production block belongs in test/, and the
    // judge scans main/ only — same split scanReply applies. A block holding
    // both production and test types reaches main/ and test/ through
    // different routes, so test/ drops any file main/ already owns.
    const [strayTests, mainFiles] = codeFiles(production).reduce(
      ([tests, main], file) => (isTestFile(file) ? [[...tests, file], main] : [tests, [...main, file]]),
      [[], []],
    );
    const mainNames = new Set(mainFiles.map((file) => file.name));
    const testFiles = [...codeFiles(testBlocks), ...strayTests]
      .filter((file, index, all) => all.findIndex((other) => other.name === file.name) === index)
      .filter((file) => !mainNames.has(file.name));
    writeFiles(mainFiles, path.join(armDir, 'main'));
    writeFiles(testFiles, path.join(armDir, 'test'));

    const result = scan(path.join(armDir, 'main'), pluginsFor(production));
    const report = result.skipped ? 'skipped: habit-hooks not on PATH\n' : result.report;
    fs.writeFileSync(path.join(runDir, 'habit-hooks', `${slug(row.task)}-${slug(row.model)}-${slug(row.arm)}.md`), report);
  }
  fs.writeFileSync(path.join(runDir, 'report.md'), buildReport(evalId, rows));
}

// Export one run's outcomes. `data` is an export JSON or a results array;
// returns the run directory, or null when there was nothing to write.
function exportRun(evalId, data, { resultsDir = RESULTS_DIR, scan } = {}) {
  const rows = resultRows(data);
  if (rows.length === 0) return null;
  const runDir = path.join(resultsDir, slug(evalId));
  writeRunArtifacts(evalId, rows, runDir, scan ? { scan } : {});
  return runDir;
}

function main() {
  const evalId = process.argv[2] || newestEvalId();
  const runDir = exportRun(evalId, loadEval(evalId));
  if (!runDir) {
    console.error(`Eval ${evalId} has no results.`);
    process.exit(1);
  }
  console.log(`wrote ${runDir} (report.md, src/, habit-hooks/)`);
}

if (require.main === module) main();

module.exports = {
  slug, resultRows, buildReport, writeRunArtifacts, exportRun, loadEval, newestEvalId,
  ENFORCED_SMELL_COLUMNS, SUGGESTED_SMELL_COLUMNS, REPORT_SMELL_COLUMNS,
};
