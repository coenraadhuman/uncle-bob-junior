#!/usr/bin/env node

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

test('root npm test runs the full suite', () => {
  const packageJson = JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8'));
  assert.match(packageJson.scripts.test, /node --test tests\/\*\.test\.js/);
});

test('CI runs the rule-copy, version, and test gates', () => {
  const workflow = fs.readFileSync(path.join(root, '.github', 'workflows', 'test.yml'), 'utf8');
  assert.match(workflow, /node scripts\/check-rule-copies\.js/);
  assert.match(workflow, /node scripts\/check-versions\.js/);
  assert.match(workflow, /npm test/);
});
