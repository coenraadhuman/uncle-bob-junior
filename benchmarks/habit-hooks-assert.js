// Promptfoo assert that judges generated code with habit-hooks
// (https://github.com/habit-hooks/habit-hooks): an independent, third-party
// smell detector, so the ruleset is vetted by a ruler this repo did not write.
//
// The production code blocks are extracted to real source files (one file
// per top-level Java type; test blocks excluded, same as the ships-tests
// judge) and scanned with the plugins matching the languages present. The
// verdict mirrors habit-hooks' own semantics: enforced smells fail,
// suggested smells are advisory, and the 0..1 score keeps the comparison
// granular beyond the binary verdict.
//
// benchmarks/export-results.js reuses scanReply() to write each answer's full
// habit-hooks report into benchmarks/results/.
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const { productionBlocks, extractCode } = require('./promptfoo-metrics');
const { codeFiles, pluginsFor } = require('./extract-files');

const SCAN_TIMEOUT_MS = 60_000;
// Score falls linearly to 0 at this many reported smells.
const WORST_SMELL_COUNT = 6;

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
  }
}

// Scan one model reply: extract the production code to real files in a temp
// dir (same extraction the results exporter writes) and judge those.
function scanReply(output) {
  const blocks = productionBlocks(String(output || ''));
  if (blocks.length === 0) blocks.push(extractCode(String(output || '')));
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-habit-hooks-'));
  try {
    for (const file of codeFiles(blocks)) {
      fs.writeFileSync(path.join(dir, file.name), file.content);
    }
    return scanDir(dir, pluginsFor(blocks));
  } finally {
    fs.rmSync(dir, { recursive: true, force: true });
  }
}

function describe(issues) {
  return issues
    .map((issue) => `${issue.rule}(${issue.count})${issue.locations.length ? ` at ${issue.locations.join(', ')}` : ''}`)
    .join('; ');
}

// `incomplete-run` and `parse-error` report a sensor that could not read a
// code fragment — scan artifacts of judging fenced snippets, not smells in
// the code (the correctness gate owns "the code is broken").
const SCAN_ARTIFACT_RULES = new Set(['incomplete-run', 'parse-error']);

// habit-hooks' documented "suggested" tier (advisory, exit 0). Everything
// else it reports is enforced and fails its run — see
// https://github.com/habit-hooks/habit-hooks#what-it-catches. An unknown new
// rule counts as enforced, so it surfaces instead of hiding.
const SUGGESTED_RULES = new Set([
  'warning-comment',
  'explicit-any',
  'non-null-assertion',
  'non-essential-comment',
  'duplicated-code',
  'swallowed-exception',
]);

function realSmells(issues) {
  return issues.filter((issue) => !SCAN_ARTIFACT_RULES.has(issue.rule));
}

function enforcedSmells(issues) {
  return realSmells(issues).filter((issue) => !SUGGESTED_RULES.has(issue.rule));
}

// pass/fail mirrors habit-hooks' own verdict: enforced smells fail the run,
// suggested smells are coached but non-blocking. The score stays granular
// (all real smells count) so the arms compare beyond the binary verdict.
module.exports = (output) => {
  const scan = scanReply(output);
  if (scan.skipped) return { pass: true, score: 1, reason: 'skipped: habit-hooks not on PATH' };
  const smells = realSmells(scan.issues);
  const enforced = enforcedSmells(scan.issues);
  const total = smells.reduce((sum, issue) => sum + issue.count, 0);
  const artifactNote = smells.length === scan.issues.length ? '' : ' (scan artifacts excluded)';
  if (total === 0) return { pass: true, score: 1, reason: `habit-hooks passed: clean${artifactNote}` };
  const score = Math.max(0, 1 - total / WORST_SMELL_COUNT);
  if (enforced.length === 0) {
    return { pass: true, score, reason: `habit-hooks passed — ${total} suggested smell(s): ${describe(smells)}${artifactNote}` };
  }
  return { pass: false, score, reason: `habit-hooks FAILED: ${total} smell(s) — ${describe(smells)}${artifactNote}` };
};
module.exports.parseIssues = parseIssues;
module.exports.scanReply = scanReply;
module.exports.scanDir = scanDir;
module.exports.realSmells = realSmells;
module.exports.enforcedSmells = enforcedSmells;
