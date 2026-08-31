#!/usr/bin/env node
// Tests for the Stop-hook verification loop: it blocks a finishing turn only
// when uncle-bob-junior is active, the project opted in (.habit-hooks/), the
// scan finds smells, and no block was already issued this stop — and it
// allows the stop silently in every situation it cannot judge.

const test = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync, execFileSync } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const root = path.join(__dirname, '..');

const hasHabitHooks = spawnSync('habit-hooks', ['--version'], { encoding: 'utf8' }).status === 0;
const hasPmd = spawnSync('pmd', ['--version'], { encoding: 'utf8' }).status === 0;
const scanOnly = { skip: !(hasHabitHooks && hasPmd) && 'habit-hooks + pmd not on PATH' };

function runVerify(input, env = {}) {
  return spawnSync(process.execPath, [path.join(root, 'hooks', 'uncle-bob-junior-verify.js')], {
    env: { ...process.env, ...env },
    input: JSON.stringify(input),
    encoding: 'utf8',
  });
}

// A config dir whose state flag says uncle-bob-junior runs in full mode.
function activeConfigDir() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-verify-config-'));
  fs.writeFileSync(path.join(dir, '.uncle-bob-junior-active'), 'full');
  return dir;
}

// A git repo that opted into habit-hooks scanning of java files.
function optedInRepo() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-verify-repo-'));
  const git = (...args) => execFileSync('git', args, { cwd: dir, stdio: 'pipe' });
  git('init', '-q');
  git('config', 'user.email', 'test@example.com');
  git('config', 'user.name', 'test');
  fs.mkdirSync(path.join(dir, '.habit-hooks'));
  fs.writeFileSync(path.join(dir, '.habit-hooks', 'config.toml'), 'plugins = ["java"]\nfiles = ["**/*.java", "!.habit-hooks/**"]\n');
  fs.writeFileSync(path.join(dir, 'README.md'), 'seed\n');
  git('add', '.');
  git('commit', '-q', '-m', 'seed');
  return dir;
}

const OVERSIZED_JAVA = `public class Big {
    public int work(int x) {
${'        x = x + 1;\n'.repeat(20)}        return x;
    }
}
`;

test('a stop that already blocked once is allowed through', () => {
  const result = runVerify({ stop_hook_active: true, cwd: os.tmpdir() }, { CLAUDE_CONFIG_DIR: activeConfigDir() });
  assert.equal(result.status, 0);
  assert.equal(result.stdout, '');
});

test('uncle-bob-junior off means no verification', () => {
  const offConfigDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-verify-off-')); // no state flag = off
  const result = runVerify({ cwd: os.tmpdir() }, { CLAUDE_CONFIG_DIR: offConfigDir });
  assert.equal(result.status, 0);
  assert.equal(result.stdout, '');
});

test('a project without .habit-hooks config has not opted in', () => {
  const plainDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-verify-plain-'));
  const result = runVerify({ cwd: plainDir }, { CLAUDE_CONFIG_DIR: activeConfigDir() });
  assert.equal(result.status, 0);
  assert.equal(result.stdout, '');
});

test('smelly branch changes block the stop with the findings as reason', scanOnly, () => {
  const repo = optedInRepo();
  fs.writeFileSync(path.join(repo, 'Big.java'), OVERSIZED_JAVA);
  const result = runVerify({ cwd: repo }, { CLAUDE_CONFIG_DIR: activeConfigDir() });
  assert.equal(result.status, 0, result.stderr);
  const output = JSON.parse(result.stdout);
  assert.equal(output.decision, 'block');
  assert.match(output.reason, /UNCLE_BOB_JUNIOR VERIFICATION/);
  assert.match(output.reason, /oversized-function/);
  assert.match(output.reason, /Big\.java/);
});

test('a clean branch lets the stop through', scanOnly, () => {
  const repo = optedInRepo();
  const result = runVerify({ cwd: repo }, { CLAUDE_CONFIG_DIR: activeConfigDir() });
  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.stdout, '');
});
