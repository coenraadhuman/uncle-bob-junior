#!/usr/bin/env node
// The one-command site refresh must run its three stages in order —
// re-judge stored runs, regenerate content, render the static site — and
// surface a skipped render instead of hiding it.

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const { run } = require('../scripts/update-site.js');

test('update-site runs reprocess, content generation, and render in order', () => {
  const calls = [];
  const result = run({
    reprocess: () => calls.push('reprocess'),
    build: () => calls.push('build'),
    render: () => { calls.push('render'); return { rendered: true }; },
    log: () => {},
  });
  assert.deepEqual(calls, ['reprocess', 'build', 'render']);
  assert.equal(result.rendered, true);
});

test('update-site reports a skipped render', () => {
  const logs = [];
  const result = run({
    reprocess: () => {},
    build: () => {},
    render: () => ({ rendered: false, reason: 'website dependencies missing' }),
    log: (line) => logs.push(line),
  });
  assert.equal(result.rendered, false);
  assert.ok(logs.some((line) => line.includes('render skipped')), 'the skip reason is surfaced');
});

test('npm exposes the script as site:update', () => {
  const pkg = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'package.json'), 'utf8'));
  assert.equal(pkg.scripts['site:update'], 'node scripts/update-site.js');
});
