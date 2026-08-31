#!/usr/bin/env node
// Rule-drift guard between the two rule texts: skills/uncle-bob-junior/SKILL.md
// (the runtime source of truth the plugin injects) and AGENTS.md (the compact
// version agents working on this repo read). SKILL.md is longer than the
// compact body, so the two cannot be byte-compared. ubj: canary, not full
// equality. Assert the load-bearing rules survive verbatim in both; changing
// a rule's wording trips this, which is the reminder to propagate it.
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

function read(relPath) {
  return fs.readFileSync(path.join(root, relPath), 'utf8').replace(/\r\n/g, '\n').trim();
}

const INVARIANTS = [
  'does one thing',                        // single responsibility
  'Names reveal intent',                   // naming rule
  'guard clause',                          // flat control flow
  'named constant',                        // no magic values
  'wrong abstraction',                     // DRY has a ceiling
  'duty to search',                        // search before hand-rolling a wheel
  'cleaner than you found it',             // boy-scout closer
  // the safety carve-outs: pin each so a reword in either file
  // can't silently drop one.
  'input validation at trust boundaries',
  'prevents data loss',
  'security',
  'accessibility',
  'Changed behavior without its test is unfinished', // test reflex headline
];

const sources = [
  ['skills/uncle-bob-junior/SKILL.md', read('skills/uncle-bob-junior/SKILL.md')],
  ['AGENTS.md', read('AGENTS.md')],
];

let failed = false;
for (const phrase of INVARIANTS) {
  for (const [label, text] of sources) {
    if (!text.includes(phrase)) {
      console.error(`${label} is missing rule invariant: "${phrase}"`);
      failed = true;
    }
  }
}

if (failed) {
  console.error('Update AGENTS.md or SKILL.md so the shared rules match.');
  process.exit(1);
}

console.log(`${INVARIANTS.length} rule invariants present in SKILL.md and AGENTS.md.`);
