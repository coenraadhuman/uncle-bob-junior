// Promptfoo assert that judges generated code with habit-hooks
// (https://github.com/habit-hooks/habit-hooks): an independent, third-party
// smell detector, so the ruleset is vetted by a ruler this repo did not write.
//
// The production code blocks are written to a temp directory as real Java
// files (test blocks excluded, same as the ships-tests judge) and scanned
// with the java + generic plugins. Reported as a penalty, never a gate:
// habit-hooks draws its own lines (functions over 12 lines, files over 200),
// which are stricter than the ruleset's — the score compares arms, it does
// not define compliance.
//
// benchmarks/export-results.js reuses scanReply() to write each answer's full
// habit-hooks report into benchmarks/results/.
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const { productionBlocks, extractCode } = require('./promptfoo-metrics');

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

// Java files must be named for habit-hooks to pick the java plugin's rules up;
// name each block after its first declared type, or Snippet<N> as a fallback.
function javaFileName(code, index) {
  const declared = code.match(/\b(?:class|interface|enum|record)\s+(\w+)/);
  return `${declared ? declared[1] : `Snippet${index + 1}`}.java`;
}

// A fenced block without a top-level type (a bare method, say) is not a valid
// compilation unit; wrap it so habit-hooks' Java parser can read it. Multiple
// same-named files across blocks get an index suffix instead of overwriting.
function asJavaUnit(code, index) {
  if (/\b(?:class|interface|enum|record)\s+\w+/.test(code)) return code;
  return `class Snippet${index + 1} {\n${code}\n}\n`;
}

function writeScanDir(blocks) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-habit-hooks-'));
  fs.mkdirSync(path.join(dir, '.habit-hooks'));
  fs.writeFileSync(path.join(dir, '.habit-hooks', 'config.toml'), 'plugins = ["java", "generic"]\n');
  const used = new Set();
  blocks.forEach((block, index) => {
    const unit = asJavaUnit(block.code, index);
    let name = javaFileName(unit, index);
    if (used.has(name)) name = name.replace(/\.java$/, `-${index + 1}.java`);
    used.add(name);
    fs.writeFileSync(path.join(dir, name), unit);
  });
  return dir;
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

// Scan one model reply. Returns { skipped } without the CLI, otherwise
// { report, issues, total }: the verbatim habit-hooks output plus the parsed
// per-rule breakdown.
function scanReply(output) {
  const blocks = productionBlocks(String(output || ''));
  if (blocks.length === 0) blocks.push(extractCode(String(output || '')));
  const dir = writeScanDir(blocks);
  try {
    const report = runHabitHooks(dir);
    if (report === null) return { skipped: true };
    const issues = parseIssues(report);
    const total = issues.reduce((sum, issue) => sum + issue.count, 0);
    return { skipped: false, report, issues, total };
  } finally {
    fs.rmSync(dir, { recursive: true, force: true });
  }
}

function describe(issues) {
  return issues
    .map((issue) => `${issue.rule}(${issue.count})${issue.locations.length ? ` at ${issue.locations.join(', ')}` : ''}`)
    .join('; ');
}

// `incomplete-run` reports a sensor that could not finish (usually a code
// fragment its parser cannot read) — a scan artifact, not a smell in the code.
const SCAN_ARTIFACT_RULES = new Set(['incomplete-run']);

function realSmells(issues) {
  return issues.filter((issue) => !SCAN_ARTIFACT_RULES.has(issue.rule));
}

module.exports = (output) => {
  const scan = scanReply(output);
  if (scan.skipped) return { pass: true, score: 1, reason: 'skipped: habit-hooks not on PATH' };
  const smells = realSmells(scan.issues);
  const total = smells.reduce((sum, issue) => sum + issue.count, 0);
  const artifactNote = smells.length === scan.issues.length ? '' : ' (scan artifacts excluded)';
  if (total === 0) return { pass: true, score: 1, reason: `habit-hooks: clean${artifactNote}` };
  return {
    pass: true,
    score: Math.max(0, 1 - total / WORST_SMELL_COUNT),
    reason: `habit-hooks: ${total} smell(s) — ${describe(smells)}${artifactNote}`,
  };
};
module.exports.parseIssues = parseIssues;
module.exports.javaFileName = javaFileName;
module.exports.scanReply = scanReply;
module.exports.realSmells = realSmells;
