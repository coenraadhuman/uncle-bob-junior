// Promptfoo asserts that judge generated code with habit-hooks
// (https://github.com/habit-hooks/habit-hooks): an independent, third-party
// smell detector, so the ruleset is vetted by a ruler this repo did not write.
//
// Each smell from habit-hooks' documented catch list is its own assert and
// metric: zero occurrences passes, any occurrence fails, and the config
// weights suggested smells at half an enforced smell. All rule asserts share
// one memoized scan per reply, so twelve metrics cost one habit-hooks run.
//
// The production code is extracted to real source files (one per top-level
// Java type; snippets and test blocks excluded) and scanned with the plugins
// matching the languages present. A reply with no valid compilation unit
// fails the valid_code gate — the benchmark wants valid code.
const crypto = require('crypto');
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const { productionBlocks, extractCode } = require('./promptfoo-metrics');
const { productionFiles, pluginsFor } = require('./extract-files');

const SCAN_TIMEOUT_MS = 60_000;
// Per rule, the score falls linearly to 0 at this many occurrences.
const WORST_PER_RULE = 4;

// The catch list (https://github.com/habit-hooks/habit-hooks#what-it-catches),
// restricted to rules that can fire on this benchmark's languages. The tier
// decides the assert's weight in promptfooconfig.yaml, not its pass rule:
// any occurrence of any smell fails its own metric.
const ENFORCED_RULES = [
  'oversized-function',
  'too-many-parameters',
  'high-complexity',
  'deep-nesting',
  'oversized-file',
  'unused-variable',
  'unused-import',
  'unused-class-member',
];
const SUGGESTED_RULES = [
  'swallowed-exception',
  'duplicated-code',
  'warning-comment',
  'non-essential-comment',
];

// Every finding section opens with `── <rule> (N issues) ──` and closes with
// the offending `File.java:line` locations.
function parseIssues(stdout) {
  const issues = [];
  let current = null;
  for (const line of String(stdout || '').split('\n')) {
    const header = line.match(/^── ([\w-]+) \((\d+) issues?\) ──/);
    if (header) {
      current = { rule: header[1], count: Number.parseInt(header[2], 10), locations: [] };
      issues.push(current);
      continue;
    }
    if (current && /^\S+:\d+$/.test(line.trim())) current.locations.push(line.trim());
    else if (current && /^\S+\.\w+$/.test(line.trim()) && !line.includes(' ')) current.locations.push(line.trim());
  }
  return issues;
}

// Exit 0 = clean, exit 1 = issues found (both carry the report on stdout);
// anything else is a real failure.
function runHabitHooks(dir) {
  try {
    return execFileSync('habit-hooks', ['--all', '--no-snooze'], {
      encoding: 'utf8',
      cwd: dir,
      timeout: SCAN_TIMEOUT_MS,
      stdio: ['ignore', 'pipe', 'pipe'], // sensor warnings stay out of the eval output
    });
  } catch (error) {
    if (error.code === 'ENOENT') return null;
    if (error.status === 1 && typeof error.stdout === 'string') return error.stdout;
    throw error;
  }
}

// Scan a directory of extracted source files with the plugins matching the
// languages present. The config is written for the scan and removed after,
// so exported results directories stay free of tool droppings. Returns
// { skipped } without the CLI, otherwise { report, issues, total }.
function scanDir(dir, plugins) {
  const configDir = path.join(dir, '.habit-hooks');
  fs.mkdirSync(configDir, { recursive: true });
  fs.writeFileSync(path.join(configDir, 'config.toml'), `plugins = ${JSON.stringify(plugins)}\n`);
  try {
    const report = runHabitHooks(dir);
    if (report === null) return { skipped: true };
    const issues = parseIssues(report);
    const total = issues.reduce((sum, issue) => sum + issue.count, 0);
    return { skipped: false, report, issues, total };
  } finally {
    fs.rmSync(configDir, { recursive: true, force: true });
    // The python plugin's ruff sensor drops a cache dir next to the scanned
    // files; exported run dirs must hold only the generated sources.
    fs.rmSync(path.join(dir, '.ruff_cache'), { recursive: true, force: true });
  }
}

// Scan one model reply: extract the production code to real files in a temp
// dir (same extraction the results exporter writes) and judge those.
// fileCount says how many valid source files the reply produced.
function scanReply(output) {
  const blocks = productionBlocks(String(output || ''));
  if (blocks.length === 0) blocks.push(extractCode(String(output || '')));
  const files = productionFiles(blocks);
  if (files.length === 0) {
    return { skipped: false, report: 'no valid code to scan (snippets excluded)\n', issues: [], total: 0, fileCount: 0 };
  }
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-habit-hooks-'));
  try {
    for (const file of files) {
      fs.writeFileSync(path.join(dir, file.name), file.content);
    }
    return { ...scanDir(dir, pluginsFor(blocks)), fileCount: files.length };
  } finally {
    fs.rmSync(dir, { recursive: true, force: true });
  }
}

// All per-rule asserts on the same reply share one scan.
const scanCache = new Map();
const SCAN_CACHE_LIMIT = 512;

function cachedScan(output) {
  const key = crypto.createHash('sha1').update(String(output || '')).digest('hex');
  if (scanCache.has(key)) return scanCache.get(key);
  const scan = scanReply(output);
  if (scanCache.size >= SCAN_CACHE_LIMIT) scanCache.delete(scanCache.keys().next().value);
  scanCache.set(key, scan);
  return scan;
}

// The benchmark judges valid code only: a reply whose java blocks are all
// snippets (no top-level type anywhere) has nothing to judge and fails here.
function validCode(output) {
  const scan = cachedScan(output);
  if (scan.skipped) return { pass: true, score: 1, reason: 'skipped: habit-hooks not on PATH' };
  if (scan.fileCount === 0) {
    return { pass: false, score: 0, reason: 'no valid compilation unit found (snippets are excluded)' };
  }
  return { pass: true, score: 1, reason: `${scan.fileCount} source file(s) extracted` };
}

// One assert per smell: 0 occurrences = pass, anything else fails that
// smell's own metric, with the locations in the reason.
function smellAssert(rule) {
  return (output) => {
    const scan = cachedScan(output);
    if (scan.skipped) return { pass: true, score: 1, reason: 'skipped: habit-hooks not on PATH' };
    if (scan.fileCount === 0) return { pass: true, score: 1, reason: 'no valid code (see valid_code)' };
    const issue = scan.issues.find((entry) => entry.rule === rule);
    if (!issue) return { pass: true, score: 1, reason: `no ${rule}` };
    const where = issue.locations.length ? ` at ${issue.locations.join(', ')}` : '';
    return {
      pass: false,
      score: Math.max(0, 1 - issue.count / WORST_PER_RULE),
      reason: `${issue.count} ${rule}${where}`,
    };
  };
}

const camel = (rule) => rule.replace(/-(\w)/g, (_, c) => c.toUpperCase());

module.exports = { parseIssues, scanReply, scanDir, validCode, smellAssert, ENFORCED_RULES, SUGGESTED_RULES };
for (const rule of [...ENFORCED_RULES, ...SUGGESTED_RULES]) {
  module.exports[camel(rule)] = smellAssert(rule);
}
