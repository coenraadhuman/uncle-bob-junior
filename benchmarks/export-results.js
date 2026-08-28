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
const { codeFiles, pluginsFor } = require('./extract-files');
const { scanDir } = require('./habit-hooks-assert');

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
    model: r.provider?.label || r.provider?.id || 'unknown-model',
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
    'Judges: one habit-hooks metric per code smell (0 occurrences = pass;',
    'suggested smells carry half the weight of enforced ones), plus the',
    'valid_code, ships_tests, and correct gates. Higher score = cleaner.',
    'Generated code and full habit-hooks reports sit next to this file in',
    '`src/` and `habit-hooks/`.',
    '',
    '| task | model | arm | score | valid code | habit-hooks | smells found | ships tests | correct |',
    '|------|-------|-----|------:|:----------:|:-----------:|--------------|:-----------:|:-------:|',
  ];
  const armTotals = new Map();
  for (const row of rows) {
    const metric = (name) => row.components.find((c) => c.metric === name);
    // One component per smell (hh:*); older exports carry a single
    // habit_hooks component instead, so fall back to it.
    const smells = row.components.filter((c) => c.metric.startsWith('hh:'));
    const legacy = metric('habit_hooks');
    const habitPass = smells.length ? smells.every((c) => c.pass) : legacy?.pass;
    const failing = smells.filter((c) => !c.pass).map((c) => c.reason);
    const detail = smells.length
      ? (failing.length ? failing.join('; ') : 'clean')
      : (legacy ? legacy.reason : 'n/a');
    lines.push(
      `| ${row.task} | ${row.model} | ${row.arm} | ${row.score.toFixed(2)} | ` +
      `${metric('valid_code') ? (metric('valid_code').pass ? 'YES' : 'NO') : 'n/a'} | ` +
      `${habitPass === undefined ? 'n/a' : habitPass ? 'PASS' : 'FAIL'} | ${detail} | ` +
      `${metric('ships_tests')?.pass ? 'YES' : 'NO'} | ${metric('correct')?.pass ? 'YES' : 'NO'} |`,
    );
    const key = `${row.model} / ${row.arm}`;
    const totals = armTotals.get(key) || { sum: 0, n: 0 };
    totals.sum += row.score;
    totals.n += 1;
    armTotals.set(key, totals);
  }
  lines.push('', '## Mean score per model and arm', '');
  for (const [key, totals] of armTotals) {
    lines.push(`- **${key}**: ${(totals.sum / totals.n).toFixed(3)} (n=${totals.n})`);
  }
  return lines.join('\n') + '\n';
}

function writeCodeFiles(blocks, dir) {
  fs.mkdirSync(dir, { recursive: true });
  for (const file of codeFiles(blocks)) {
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
    const tests = fencedBlocks(row.output).filter((block) => isTestBlock(block.code));
    writeCodeFiles(production, path.join(armDir, 'main'));
    if (tests.length) writeCodeFiles(tests, path.join(armDir, 'test'));

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

module.exports = { slug, resultRows, buildReport, writeRunArtifacts, exportRun, loadEval, newestEvalId };
