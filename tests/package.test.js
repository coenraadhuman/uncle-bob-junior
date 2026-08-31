#!/usr/bin/env node
// The README tells users to run `node scripts/uninstall.js`, so the npm package
// must actually ship it. Guard the files entry so it can't silently drop out.

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

test('the advertised cleanup script exists', () => {
  // README tells users to run it to remove the mode flag, config, and statusline entry.
  assert.ok(
    fs.existsSync(path.join(root, 'scripts', 'uninstall.js')),
    'scripts/uninstall.js is advertised in the README but missing on disk',
  );
});
