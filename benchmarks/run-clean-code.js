#!/usr/bin/env node
// With/without benchmark runner: the same tasks through a headless Claude Code
// session, once bare (baseline) and once with the uncle-bob-junior ruleset
// appended as system prompt. Scores each answer with clean-code-metrics.js and
// the correctness.js gate, then writes a dated report to results/.
//
//   node run-clean-code.js [--model haiku] [--runs 1] [--tasks email,csv]
//
// Needs the `claude` CLI on PATH and an authenticated Claude Code install.
// `--safe-mode` isolates each run from user CLAUDE.md, hooks, and plugins, so an installed
// uncle-bob-junior plugin cannot leak rules into the baseline arm. Org-level instructions served by the account apply to both arms equally.

const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const { analyze } = require('./clean-code-metrics.js');
const correctness = require('./correctness.js');

const DEFAULT_MODEL = 'haiku';
const DEFAULT_RUNS = 1;
const RUN_TIMEOUT_MS = 300_000;

function parseArgs(argv) {
  const args = { model: DEFAULT_MODEL, runs: DEFAULT_RUNS, tasks: null };
  for (let i = 0; i < argv.length; i++) {
    if (argv[i] === '--model') args.model = argv[++i];
    else if (argv[i] === '--runs') args.runs = Number.parseInt(argv[++i], 10) || DEFAULT_RUNS;
    else if (argv[i] === '--tasks') args.tasks = argv[++i].split(',').map((t) => t.trim());
  }
  return args;
}

function loadRuleset() {
  const skill = fs.readFileSync(path.join(__dirname, '..', 'skills', 'uncle-bob-junior', 'SKILL.md'), 'utf8');
  return skill.replace(/^---[\s\S]*?---\s*/, '');
}

function loadTasks(filter) {
  const { tasks } = JSON.parse(fs.readFileSync(path.join(__dirname, 'tasks.json'), 'utf8'));
  if (!filter) return tasks;
  return tasks.filter((task) => filter.includes(task.id));
}

// Headless sessions sometimes act like agents: they try to write files or ask
// clarifying questions instead of answering, which leaves no code to score.
// Both arms get the same delivery instruction, so the comparison stays fair.
const DELIVERY_INSTRUCTION =
  'Reply with the complete solution as Java code in fenced ```java blocks in your message. ' +
  'Do not create or edit files. Do not ask clarifying questions; make reasonable assumptions and state them briefly.';

function promptFor(task) {
  return `${task.prompt}\n\n${DELIVERY_INSTRUCTION}`;
}

// One headless generation. Returns { text, costUsd, durationMs } or null on failure.
function askClaude(prompt, model, systemAppend) {
  const cliArgs = ['-p', prompt, '--safe-mode', '--model', model, '--output-format', 'json'];
  if (systemAppend) cliArgs.push('--append-system-prompt', systemAppend);
  const emptyCwd = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-bench-'));
  try {
    const raw = execFileSync('claude', cliArgs, {
      encoding: 'utf8',
      timeout: RUN_TIMEOUT_MS,
      cwd: emptyCwd,
      maxBuffer: 16 * 1024 * 1024,
    });
    const reply = JSON.parse(raw);
    return { text: reply.result || '', costUsd: reply.total_cost_usd ?? null, durationMs: reply.duration_ms ?? null };
  } catch (error) {
    console.error(`  run failed: ${String(error.message).slice(0, 200)}`);
    return null;
  } finally {
    fs.rmSync(emptyCwd, { recursive: true, force: true });
  }
}

function fencedBlocks(text) {
  return [...String(text).matchAll(/```(\w*)\r?\n([\s\S]*?)```/g)]
    .map((m) => ({ lang: (m[1] || '').toLowerCase(), code: m[2] }));
}

// The largest fenced block carries the deliverable; fall back to the whole
// reply when the model answered with bare code.
function extractCode(text) {
  const blocks = fencedBlocks(text);
  if (blocks.length === 0) return { lang: '', code: String(text) };
  return blocks.reduce((a, b) => (b.code.length > a.code.length ? b : a));
}

// Smell metrics measure production code only: counting a test's expected-value
// literals as magic numbers would penalize exactly the arm that ships tests.
function isTestBlock(code) {
  return /@Test\b|\borg\.junit\b|\bclass\s+\w*Tests?\b/.test(code);
}

const NON_CODE_LANGS = new Set(['bash', 'sh', 'shell', 'xml', 'json', 'yaml', 'yml', 'properties', 'text', 'txt', 'sql']);

function productionCode(text) {
  const production = fencedBlocks(text)
    .filter((block) => !NON_CODE_LANGS.has(block.lang) && !isTestBlock(block.code));
  if (production.length === 0) return null;
  return {
    lang: (production.find((block) => block.lang) || production[0]).lang,
    code: production.map((block) => block.code).join('\n'),
  };
}

function scoreRun(reply, task) {
  // Test-only or unfenced replies fall back to the largest block, so they
  // still get measured instead of scoring an empty string.
  const { lang, code } = productionCode(reply.text) || extractCode(reply.text);
  const allCode = fencedBlocks(reply.text).map((b) => b.code).join('\n') || code;
  const metrics = analyze(code, lang);
  metrics.hasTests = analyze(allCode, lang).hasTests; // tests often land in a second block
  const gate = correctness(reply.text, { vars: { task: task.prompt } });
  return { ...metrics, correct: gate.pass ? 1 : 0, correctReason: gate.reason, costUsd: reply.costUsd, durationMs: reply.durationMs };
}

function median(values) {
  const sorted = values.filter((v) => v != null).sort((a, b) => a - b);
  if (sorted.length === 0) return null;
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
}

const REPORT_FIELDS = [
  ['loc', 'code LOC'],
  ['maxFunctionLength', 'longest function (lines)'],
  ['longFunctionCount', 'functions > 20 lines'],
  ['maxNestingDepth', 'max nesting depth'],
  ['magicNumberCount', 'magic numbers'],
  ['shortNameCount', 'short names'],
  ['duplicateBlockCount', 'duplicate blocks'],
  ['mutableFieldCount', 'mutable fields'],
  ['setterCount', 'setters'],
  ['hasTests', 'ships tests (share)'],
  ['correct', 'correct (share)'],
  ['costUsd', 'cost (USD)'],
  ['durationMs', 'duration (ms)'],
];

function aggregate(scores) {
  const summary = {};
  for (const [field] of REPORT_FIELDS) {
    const values = scores.map((s) => (typeof s[field] === 'boolean' ? Number(s[field]) : s[field]));
    summary[field] = median(values);
  }
  return summary;
}

function formatValue(value) {
  if (value == null) return 'n/a';
  return Number.isInteger(value) ? String(value) : value.toFixed(3);
}

function renderReport(model, runs, rows) {
  const date = new Date().toISOString().slice(0, 10);
  const lines = [
    `# Clean-code benchmark: with vs without uncle-bob-junior`,
    '',
    `Date: ${date} · model: ${model} · runs per cell: ${runs} · medians reported.`,
    '',
    'Method: same task, same model, headless `claude -p --safe-mode` (no CLAUDE.md, hooks, or plugins).',
    'Baseline arm gets the bare prompt; the uncle-bob-junior arm gets the SKILL.md',
    'ruleset appended as system prompt. Production code (fenced blocks minus test',
    'and non-code blocks) scored by `clean-code-metrics.js`, gated by `correctness.js`;',
    'test code counts only toward "ships tests". Lower is better for every row',
    'except "ships tests" and "correct".',
    '',
  ];

  for (const { task, baseline, ubj } of rows) {
    lines.push(`## ${task.id}`, '', `> ${task.prompt}`, '', '| metric | baseline | uncle-bob-junior |', '|---|--:|--:|');
    for (const [field, label] of REPORT_FIELDS) {
      lines.push(`| ${label} | ${formatValue(baseline[field])} | ${formatValue(ubj[field])} |`);
    }
    lines.push('');
  }

  lines.push('## Summary (median of task medians)', '', '| metric | baseline | uncle-bob-junior |', '|---|--:|--:|');
  const baselineAll = aggregate(rows.map((r) => r.baseline));
  const ubjAll = aggregate(rows.map((r) => r.ubj));
  for (const [field, label] of REPORT_FIELDS) {
    lines.push(`| ${label} | ${formatValue(baselineAll[field])} | ${formatValue(ubjAll[field])} |`);
  }
  lines.push('');
  lines.push('Per-run generated sources: sources.md (side by side) and src/<task>/<arm>-run<N>/ (as files).');
  lines.push('Caveats and how to read these numbers: ../README.md.', '');
  return { date, markdown: lines.join('\n') };
}

// Companion report: every run's generated code verbatim, baseline next to
// ruleset, so the metric deltas can be checked against the actual sources.
function renderSourcesReport(raw) {
  const lines = [
    '# Generated sources: with vs without uncle-bob-junior',
    '',
    'Every fenced code block from each run, exactly as the model wrote it.',
    'Metrics and medians live in the clean-code report next to this file.',
    '',
  ];
  const tasks = [...new Set(raw.map((entry) => entry.task))];
  for (const task of tasks) {
    lines.push(`## ${task}`, '');
    const runs = [...new Set(raw.filter((e) => e.task === task).map((e) => e.run))].sort();
    for (const run of runs) {
      for (const arm of ['baseline', 'ubj']) {
        const entry = raw.find((e) => e.task === task && e.run === run && e.arm === arm);
        if (!entry) continue;
        const armLabel = arm === 'ubj' ? 'uncle-bob-junior' : 'baseline';
        lines.push(`### ${task} · run ${run + 1} · ${armLabel}`, '');
        const blocks = fencedBlocks(entry.replyText || '');
        if (blocks.length === 0) {
          lines.push('_No fenced code in this reply._', '');
          continue;
        }
        for (const block of blocks) {
          // Four-backtick fences survive replies whose code contains ``` itself.
          lines.push('````' + block.lang, block.code.replace(/\n$/, ''), '````', '');
        }
      }
    }
  }
  return lines.join('\n');
}

// The model becomes part of the run directory name, so runs on different
// models on the same day never overwrite each other.
function modelSlug(model) {
  return String(model).replace(/[^\w.-]+/g, '-');
}

// A second run with the same model on the same day gets its own directory
// instead of overwriting the first: <name>, <name>-2, <name>-3, ...
function uniqueRunDir(resultsDir, baseName) {
  let candidate = path.join(resultsDir, baseName);
  for (let suffix = 2; fs.existsSync(candidate); suffix++) {
    candidate = path.join(resultsDir, `${baseName}-${suffix}`);
  }
  return candidate;
}

const BLOCK_EXTENSIONS = { java: 'java', xml: 'xml', json: 'json', bash: 'sh', sh: 'sh', shell: 'sh', yaml: 'yaml', yml: 'yaml', properties: 'properties', sql: 'sql', text: 'txt' };

// A Java block is named after its first declared type; anything else keeps a
// numbered name with its language's extension.
function sourceFileName(block, index) {
  const declaredType = block.code.match(/\b(?:class|interface|enum|record)\s+(\w+)/);
  if (declaredType && (block.lang === 'java' || !block.lang)) return `${declaredType[1]}.java`;
  const extension = BLOCK_EXTENSIONS[block.lang] || (block.lang ? block.lang : 'txt');
  return `block-${index + 1}.${extension}`;
}

// Save one run's generated code as real files: src/<task>/<arm>-run<N>/<file>.
function writeRunSources(runDir, entry) {
  const dir = path.join(runDir, 'src', entry.task, `${entry.arm}-run${entry.run + 1}`);
  fs.mkdirSync(dir, { recursive: true });
  const blocks = fencedBlocks(entry.replyText || '');
  if (blocks.length === 0) {
    fs.writeFileSync(path.join(dir, 'reply.txt'), entry.replyText || '');
    return;
  }
  const usedNames = new Set();
  blocks.forEach((block, index) => {
    let name = sourceFileName(block, index);
    // Alternative implementations redeclare the same type; keep both on disk.
    if (usedNames.has(name)) name = `alt-${index + 1}-${name}`;
    usedNames.add(name);
    fs.writeFileSync(path.join(dir, name), block.code.endsWith('\n') ? block.code : block.code + '\n');
  });
}

function main() {
  const { model, runs, tasks: filter } = parseArgs(process.argv.slice(2));
  const ruleset = loadRuleset();
  const tasks = loadTasks(filter);
  if (tasks.length === 0) {
    console.error('No tasks matched.');
    process.exit(1);
  }

  const runDir = uniqueRunDir(path.join(__dirname, 'results'), `${new Date().toISOString().slice(0, 10)}-${modelSlug(model)}`);
  fs.mkdirSync(runDir, { recursive: true });
  const rawPath = path.join(runDir, 'raw.json');

  const rows = [];
  const raw = [];
  for (const task of tasks) {
    console.log(`task: ${task.id}`);
    const armScores = { baseline: [], ubj: [] };
    for (let run = 0; run < runs; run++) {
      for (const [arm, systemAppend] of [['baseline', null], ['ubj', ruleset]]) {
        console.log(`  ${arm} run ${run + 1}/${runs}...`);
        const reply = askClaude(promptFor(task), model, systemAppend);
        if (!reply) continue;
        const score = scoreRun(reply, task);
        armScores[arm].push(score);
        const entry = { task: task.id, arm, run, ...score, replyText: reply.text };
        raw.push(entry);
        writeRunSources(runDir, entry);
      }
    }
    // Persist after every task, so a killed run keeps its completed replies
    // (rescorable later instead of re-spent).
    fs.writeFileSync(rawPath, JSON.stringify(raw, null, 2));
    if (armScores.baseline.length && armScores.ubj.length) {
      rows.push({ task, baseline: aggregate(armScores.baseline), ubj: aggregate(armScores.ubj) });
    } else {
      console.error(`  skipping ${task.id} in report: an arm produced no successful runs`);
    }
  }

  if (rows.length === 0) {
    console.error('No successful runs; no report written.');
    process.exit(1);
  }

  const { markdown } = renderReport(model, runs, rows);
  const reportPath = path.join(runDir, 'report.md');
  fs.writeFileSync(reportPath, markdown);
  fs.writeFileSync(path.join(runDir, 'sources.md'), renderSourcesReport(raw));
  console.log(`\nwrote ${runDir} (report.md, sources.md, raw.json, src/)`);
}

if (require.main === module) main();

module.exports = { extractCode, scoreRun, median, aggregate, renderReport, renderSourcesReport, writeRunSources, loadTasks };
