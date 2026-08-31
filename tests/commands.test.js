#!/usr/bin/env node
// Every uncle-bob-junior skill must also ship as a Claude Code file-based
// command (commands/*.toml). /uncle-bob-junior-help was once advertised in the
// README and the help card but missing its file; this guards that drift: a
// skill with no command adapter fails here.

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

// The skills directory is the canonical command set.
const commands = fs.readdirSync(path.join(root, 'skills'))
  .filter((name) => fs.existsSync(path.join(root, 'skills', name, 'SKILL.md')))
  // The debt/gain/help/audit/review skills all get commands; the base skill does too.
  .concat('uncle-bob-junior')
  .filter((name, index, all) => all.indexOf(name) === index);

test('skills exist to derive the command set from', () => {
  assert.ok(commands.includes('uncle-bob-junior'), 'expected the base uncle-bob-junior skill');
  assert.ok(commands.length > 1, 'expected the companion skills');
});

test('every skill ships a Claude commands/*.toml', () => {
  for (const name of commands) {
    assert.ok(
      fs.existsSync(path.join(root, 'commands', `${name}.toml`)),
      `missing commands/${name}.toml`,
    );
  }
});
