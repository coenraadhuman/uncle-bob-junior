#!/usr/bin/env node
// Tests for re-judging stored runs from their raw replies: a run with
// replies is rewritten with fresh verdicts and scores, a run without any
// reply.md is removed, and the offline judge mirrors the eval-time asserts.

const test = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const { judgeReply, scoreOf, reprocessRun, taskPrompts } = require('../benchmarks/reprocess-results.js');

const hasHabitHooks = spawnSync('habit-hooks', ['--version'], { encoding: 'utf8' }).status === 0;

const CLEAN_REPLY = [
  '```java',
  'public class EmailValidator {',
  '    public boolean isValid(String address) {',
  '        return address != null && address.contains("@");',
  '    }',
  '}',
  '```',
  '',
  '```java',
  'import org.junit.Test;',
  'public class EmailValidatorTests {',
  '    @Test public void acceptsPlainAddress() { }',
  '}',
  '```',
].join('\n');

test('judgeReply produces the eval-time component set with gates and per-smell metrics', () => {
  const components = judgeReply(CLEAN_REPLY, 'Write a Java method that validates email addresses.');
  const metric = (name) => components.find((c) => c.metric === name);
  assert.equal(metric('ships_tests').pass, true);
  assert.ok(metric('valid_code'));
  assert.ok(metric('correct'));
  assert.ok(metric('hh:oversized-function'), 'one component per catch-list smell');
  const score = scoreOf(components);
  assert.ok(score > 0 && score <= 1, `weighted score in range, got ${score}`);
});

test('a run with replies is re-judged in place; one without any reply.md is removed', { skip: !hasHabitHooks && 'habit-hooks not on PATH' }, () => {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-reprocess-'));
  try {
    const runDir = path.join(resultsDir, 'eval-old-2026-08-27t10-00-00');
    const armDir = path.join(runDir, 'src', 'email', 'claude-cli-haiku', 'uncle-bob-junior');
    fs.mkdirSync(armDir, { recursive: true });
    fs.writeFileSync(path.join(armDir, 'reply.md'), CLEAN_REPLY);

    assert.equal(reprocessRun(runDir, taskPrompts()), 'reprocessed');
    const report = JSON.parse(fs.readFileSync(path.join(runDir, 'report.json'), 'utf8'));
    assert.equal(report.evalId, 'eval-old-2026-08-27t10-00-00', 'eval id recovered from the directory name');
    assert.equal(report.rows.length, 1);
    assert.equal(report.rows[0].model, 'claude-cli:haiku', 'model de-slugged from the directory name');
    assert.equal(report.rows[0].gates.shipsTests, true);
    assert.ok(fs.existsSync(path.join(runDir, 'report.md')), 'report.md rewritten');

    const emptyRun = path.join(resultsDir, 'eval-empty-2026-08-27t11-00-00');
    fs.mkdirSync(path.join(emptyRun, 'src'), { recursive: true });
    fs.writeFileSync(path.join(emptyRun, 'report.md'), 'stale');
    assert.equal(reprocessRun(emptyRun, {}), 'removed');
    assert.ok(!fs.existsSync(emptyRun), 'a run with nothing to re-judge is deleted');
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
  }
});

test('taskPrompts maps every configured task description to its prompt', () => {
  const prompts = taskPrompts();
  assert.ok(prompts.email.includes('email'), 'email task present');
  assert.ok(prompts.logscan.includes('access log'), 'quoted escapes inside the yaml string survive');
  assert.ok(prompts.expense.includes('C#'));
});
