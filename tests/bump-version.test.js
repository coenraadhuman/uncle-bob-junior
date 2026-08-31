#!/usr/bin/env node
// Version bumping keyed to plugin content: the payload hash is stable, a
// content change flips it, bumping rewrites every version file and records
// the new hash, and the repo itself must never carry changed content under
// an unbumped version (the canary that automates the discipline).

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const { payloadHash, status, bump, bumpedVersion } = require('../scripts/bump-version.js');

function fixturePlugin() {
  // package.json sits OUTSIDE the plugin dir, like the real repo layout —
  // inside it would be part of the hashed payload and re-dirty the hash on
  // every bump.
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-bump-'));
  const dir = path.join(root, 'plugin');
  fs.mkdirSync(path.join(dir, '.claude-plugin'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'skills'));
  fs.writeFileSync(path.join(dir, 'skills', 'SKILL.md'), 'rules v1');
  fs.writeFileSync(path.join(dir, '.claude-plugin', 'plugin.json'), JSON.stringify({ name: 'x', version: '1.2.3' }, null, 2));
  const packagePath = path.join(root, 'package.json');
  fs.writeFileSync(packagePath, JSON.stringify({ name: 'x', version: '1.2.3' }, null, 2));
  return { root, dir, versionFiles: [path.join(dir, '.claude-plugin', 'plugin.json'), packagePath] };
}

test('the payload hash is stable and ignores the manifest directory', () => {
  const { root, dir } = fixturePlugin();
  try {
    const first = payloadHash(dir);
    assert.equal(payloadHash(dir), first, 'same content, same hash');
    fs.writeFileSync(path.join(dir, '.claude-plugin', 'anything.txt'), 'manifest churn');
    assert.equal(payloadHash(dir), first, 'manifest dir is excluded from the hash');
    fs.writeFileSync(path.join(dir, 'skills', 'SKILL.md'), 'rules v2');
    assert.notEqual(payloadHash(dir), first, 'content change flips the hash');
  } finally {
    fs.rmSync(root, { recursive: true, force: true });
  }
});

test('bump rewrites every version file, records the hash, and is a no-op when unchanged', () => {
  const { root, dir, versionFiles } = fixturePlugin();
  try {
    const first = bump('patch', { pluginDir: dir, versionFiles });
    assert.deepEqual(first, { from: '1.2.3', to: '1.2.4' });
    for (const file of versionFiles) {
      assert.equal(JSON.parse(fs.readFileSync(file, 'utf8')).version, '1.2.4', `${path.basename(file)} bumped`);
    }
    assert.equal(status(dir).upToDate, true, 'hash recorded');
    assert.equal(bump('patch', { pluginDir: dir, versionFiles }), null, 'unchanged payload does not bump');

    fs.writeFileSync(path.join(dir, 'skills', 'SKILL.md'), 'rules v3');
    assert.equal(status(dir).upToDate, false);
    assert.deepEqual(bump('minor', { pluginDir: dir, versionFiles }), { from: '1.2.4', to: '1.3.0' });
  } finally {
    fs.rmSync(root, { recursive: true, force: true });
  }
});

test('semver arithmetic per level', () => {
  assert.equal(bumpedVersion('1.2.3', 'patch'), '1.2.4');
  assert.equal(bumpedVersion('1.2.3', 'minor'), '1.3.0');
  assert.equal(bumpedVersion('1.2.3', 'major'), '2.0.0');
});

test('CANARY: the repo plugin payload matches its recorded version hash', () => {
  assert.equal(status().upToDate, true,
    'plugin content changed without a version bump; run: node scripts/bump-version.js');
});
