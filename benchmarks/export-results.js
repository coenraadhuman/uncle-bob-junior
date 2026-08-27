#!/usr/bin/env node
// Export a promptfoo eval into benchmarks/results/<eval-id>/ so run outcomes
// live next to the repo instead of only inside promptfoo's local database:
//
//   node benchmarks/export-results.js [evalId]   # defaults to the newest eval
//
// Each run directory contains:
//   report.md                          scoreboard per arm and task
//   src/<task>/<arm>/<File>.java       every generated code block, verbatim
//   src/<task>/<arm>/reply.md          the full model reply
//   habit-hooks/<task>-<arm>.md        the full habit-hooks report per answer
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const { fencedBlocks } = require('./promptfoo-metrics');
const { javaFileName, scanReply } = require('./habit-hooks-assert');

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
    arm: r.prompt?.label || r.provider?.label || 'unknown-arm',
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

function buildReport(evalId, rows) {
  const lines = [
    `# Benchmark run ${evalId}`,
    '',
    'Judges: habit-hooks (independent smell report, penalty), ships_tests and',
    'correct (gates). Higher score = cleaner. Generated code and full',
    'habit-hooks reports sit next to this file in `src/` and `habit-hooks/`.',
    '',
    '| task | arm | score | habit-hooks | ships tests | correct |',
    '|------|-----|------:|-------------|:-----------:|:-------:|',
  ];
  const armTotals = new Map();
  for (const row of rows) {
    const metric = (name) => row.components.find((c) => c.metric === name);
    const habit = metric('habit_hooks');
    lines.push(
      `| ${row.task} | ${row.arm} | ${row.score.toFixed(2)} | ${habit ? habit.reason : 'n/a'} | ` +
      `${metric('ships_tests')?.pass ? 'yes' : 'NO'} | ${metric('correct')?.pass ? 'YES' : 'NO'} |`,
    );
    const totals = armTotals.get(row.arm) || { sum: 0, n: 0 };
    totals.sum += row.score;
    totals.n += 1;
    armTotals.set(row.arm, totals);
  }
  lines.push('', '## Mean score per arm', '');
  for (const [arm, totals] of armTotals) {
    lines.push(`- **${arm}**: ${(totals.sum / totals.n).toFixed(3)} (n=${totals.n})`);
  }
  return lines.join('\n') + '\n';
}

// The scan option exists so tests can inject a fake instead of the real CLI.
function writeRunArtifacts(evalId, rows, runDir, { scan = scanReply } = {}) {
  fs.mkdirSync(path.join(runDir, 'habit-hooks'), { recursive: true });
  for (const row of rows) {
    const armDir = path.join(runDir, 'src', slug(row.task), slug(row.arm));
    fs.mkdirSync(armDir, { recursive: true });
    fs.writeFileSync(path.join(armDir, 'reply.md'), row.output);
    fencedBlocks(row.output).forEach((block, index) => {
      fs.writeFileSync(path.join(armDir, javaFileName(block.code, index)), block.code);
    });

    const result = scan(row.output);
    const report = result.skipped ? 'skipped: habit-hooks not on PATH\n' : result.report;
    fs.writeFileSync(path.join(runDir, 'habit-hooks', `${slug(row.task)}-${slug(row.arm)}.md`), report);
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

module.exports = { slug, resultRows, buildReport, writeRunArtifacts, exportRun };
