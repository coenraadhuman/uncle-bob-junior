#!/usr/bin/env node
// Re-judges every stored benchmark run from its raw replies:
//
//   node benchmarks/reprocess-results.js
//
// Judges and extraction improve over time; the stored reply.md files are the
// ground truth, everything else in a run directory is derived. Reprocessing
// re-extracts the sources, re-runs habit-hooks, re-checks the gates, and
// recomputes the scores with the current weights, then rewrites the run's
// artifacts (report.json, report.md, src/, habit-hooks/) in place. A run
// directory with no reply.md at all has nothing to re-judge and is removed.
const fs = require('fs');
const path = require('path');

const { scanReply, ENFORCED_RULES, SUGGESTED_RULES } = require('./habit-hooks-assert');
const { shipsTests } = require('./promptfoo-metrics');
const correctness = require('./correctness');
const { writeRunArtifacts } = require('./export-results');

const RESULTS_DIR = path.join(__dirname, 'results');
const GAME_OF_LIFE_RESULTS_DIR = path.join(__dirname, 'game-of-life-results');
const CONFIG_PATHS = [
  path.join(__dirname, 'promptfooconfig.yaml'),
  path.join(__dirname, 'promptfooconfig.gameoflife.yaml'),
];

// The same weights promptfooconfig.yaml gives the eval-time asserts.
const GATE_WEIGHT = 2;
const ENFORCED_WEIGHT = 1;
const SUGGESTED_WEIGHT = 0.5;
// Per rule, the score falls linearly to 0 at this many occurrences.
const WORST_PER_RULE = 4;

// task description -> full task prompt, from the current config. Older runs
// predate the prompt field in report.json; the correctness gate routes on
// this text.
function taskPrompts() {
  const prompts = {};
  for (const configPath of CONFIG_PATHS) {
    if (!fs.existsSync(configPath)) continue;
    const yaml = fs.readFileSync(configPath, 'utf8');
    for (const match of yaml.matchAll(/- description: (\S+)\s*\n\s*vars: \{ task: ("(?:[^"\\]|\\.)*") \}/g)) {
      prompts[match[1]] = JSON.parse(match[2]);
    }
  }
  return prompts;
}

// Directory names are slugs; recover the display names the reports use.
function deslugModel(slug) {
  return slug.replace(/^claude-cli-/, 'claude-cli:');
}

function deslugArm(slug) {
  return slug === 'baseline-no-ruleset' ? 'baseline (no ruleset)' : slug;
}

function smellComponents(scan) {
  return [...ENFORCED_RULES, ...SUGGESTED_RULES].map((rule) => {
    if (scan.skipped) return { metric: `hh:${rule}`, pass: true, score: 1, reason: 'skipped: habit-hooks not on PATH' };
    if (scan.fileCount === 0) return { metric: `hh:${rule}`, pass: true, score: 1, reason: 'no valid code (see valid_code)' };
    const issue = scan.issues.find((entry) => entry.rule === rule);
    const count = issue ? issue.count : 0;
    const where = issue?.locations?.length ? ` at ${issue.locations.join(', ')}` : '';
    return {
      metric: `hh:${rule}`,
      pass: count === 0,
      score: Math.max(0, 1 - count / WORST_PER_RULE),
      reason: count === 0 ? `no ${rule}` : `${count} ${rule}${where}`,
    };
  });
}

// The same verdicts the eval-time asserts produce, from the reply alone.
function judgeReply(output, taskPrompt) {
  const scan = scanReply(output);
  const tests = shipsTests(output);
  const correct = correctness(output, { vars: { task: taskPrompt || '' } });
  return [
    {
      metric: 'valid_code',
      pass: scan.skipped || scan.fileCount > 0,
      score: scan.skipped || scan.fileCount > 0 ? 1 : 0,
      reason: scan.skipped ? 'skipped: habit-hooks not on PATH' : `${scan.fileCount} source file(s) extracted`,
    },
    { metric: 'ships_tests', pass: tests.pass, score: tests.score, reason: tests.reason },
    { metric: 'correct', pass: correct.pass, score: correct.score, reason: correct.reason },
    ...smellComponents(scan),
  ];
}

function weightOf(metric) {
  if (metric.startsWith('hh:')) {
    return SUGGESTED_RULES.includes(metric.slice(3)) ? SUGGESTED_WEIGHT : ENFORCED_WEIGHT;
  }
  return GATE_WEIGHT;
}

function scoreOf(components) {
  const totalWeight = components.reduce((sum, c) => sum + weightOf(c.metric), 0);
  const totalScore = components.reduce((sum, c) => sum + weightOf(c.metric) * c.score, 0);
  return totalWeight > 0 ? totalScore / totalWeight : 0;
}

function readJsonSafe(filePath) {
  try {
    return JSON.parse(fs.readFileSync(filePath, 'utf8'));
  } catch (error) {
    return null;
  }
}

// Every stored reply in a run directory: src/<task>/<model>/<arm>/reply.md.
function storedReplies(runDir) {
  const srcDir = path.join(runDir, 'src');
  if (!fs.existsSync(srcDir)) return [];
  const replies = [];
  for (const task of fs.readdirSync(srcDir)) {
    const taskDir = path.join(srcDir, task);
    if (!fs.statSync(taskDir).isDirectory()) continue;
    for (const model of fs.readdirSync(taskDir)) {
      const modelDir = path.join(taskDir, model);
      if (!fs.statSync(modelDir).isDirectory()) continue;
      for (const arm of fs.readdirSync(modelDir)) {
        const replyPath = path.join(modelDir, arm, 'reply.md');
        if (fs.existsSync(replyPath)) replies.push({ task, model, arm, replyPath });
      }
    }
  }
  return replies;
}

// A stored report.json row carries the exact display names and the prompt;
// fall back to de-slugged names and the current config's prompt without one.
function rowFor(reply, stored, prompts) {
  const output = fs.readFileSync(reply.replyPath, 'utf8');
  const storedRow = (stored?.rows || []).find((row) =>
    row.task === reply.task
    && row.model.replace(/[^a-z0-9]+/gi, '-').toLowerCase() === reply.model
    && row.arm.replace(/[^a-z0-9]+/gi, '-').replace(/^-+|-+$/g, '').toLowerCase() === reply.arm);
  const prompt = storedRow?.prompt || prompts[reply.task] || '';
  const components = judgeReply(output, prompt);
  return {
    task: storedRow?.task || reply.task,
    model: storedRow?.model || deslugModel(reply.model),
    arm: storedRow?.arm || deslugArm(reply.arm),
    prompt,
    output,
    score: scoreOf(components),
    components,
  };
}

function evalIdOf(runDir, stored) {
  if (stored?.evalId) return stored.evalId;
  const header = (() => {
    try {
      return fs.readFileSync(path.join(runDir, 'report.md'), 'utf8').match(/^# Benchmark run (\S+)/)?.[1];
    } catch (error) {
      return null;
    }
  })();
  return header || path.basename(runDir);
}

// Re-judge one run in place. Returns 'reprocessed' or 'removed'.
function reprocessRun(runDir, prompts) {
  const replies = storedReplies(runDir);
  if (replies.length === 0) {
    fs.rmSync(runDir, { recursive: true, force: true });
    return 'removed';
  }
  const stored = readJsonSafe(path.join(runDir, 'report.json'));
  const rows = replies.map((reply) => rowFor(reply, stored, prompts));
  writeRunArtifacts(evalIdOf(runDir, stored), rows, runDir);
  return 'reprocessed';
}

// Re-judge every stored run, regular and Game of Life alike.
function reprocessAll() {
  const prompts = taskPrompts();
  for (const resultsDir of [RESULTS_DIR, GAME_OF_LIFE_RESULTS_DIR]) {
    if (!fs.existsSync(resultsDir)) continue;
    const runs = fs.readdirSync(resultsDir).filter((name) => fs.statSync(path.join(resultsDir, name)).isDirectory());
    for (const name of runs) {
      const outcome = reprocessRun(path.join(resultsDir, name), prompts);
      console.log(`${outcome}: ${name}`);
    }
  }
}

if (require.main === module) reprocessAll();

module.exports = { judgeReply, scoreOf, reprocessRun, reprocessAll, taskPrompts, storedReplies };
