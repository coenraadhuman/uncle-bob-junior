#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

function read(relPath) {
  return fs.readFileSync(path.join(root, relPath), 'utf8').replace(/\r\n/g, '\n').trim();
}

function stripFrontmatter(text) {
  return text.replace(/^---\n[\s\S]*?\n---\n*/, '').trim();
}

const agents = read('AGENTS.md');
const canonical = agents.replace(/\n\n\(Yes, this file also applies[\s\S]*?\)$/, '').trim();

// Compact copies: same body as AGENTS.md, host-specific frontmatter stripped.
const copies = [
  ['.cursor/rules/uncle-bob-junior.mdc', stripFrontmatter],
  ['.windsurf/rules/uncle-bob-junior.md', text => text.trim()],
  ['.clinerules/uncle-bob-junior.md', text => text.trim()],
  ['.agents/rules/uncle-bob-junior.md', text => text.trim()],
  ['.qoder/rules/uncle-bob-junior.md', text => text.trim()],
  ['.github/copilot-instructions.md', text => text.trim()],
  ['.kiro/steering/uncle-bob-junior.md', stripFrontmatter],
];

let failed = false;

for (const [relPath, normalize] of copies) {
  const actual = normalize(read(relPath));
  if (actual !== canonical) {
    console.error(`${relPath} drifted from AGENTS.md`);
    failed = true;
  }
}

// SKILL.md is the runtime source of truth and is longer than the compact body,
// so it cannot be byte-compared. ubj: canary, not full equality. Assert the
// load-bearing rules survive verbatim in both the source and AGENTS.md. Changing
// a rule's wording trips this, which is the reminder to propagate it everywhere.
// Upgrade path: generate the copies from SKILL.md if this ever misses a real drift.
const INVARIANTS = [
  'does one thing',                        // single responsibility
  'Names reveal intent',                   // naming rule
  'guard clause',                          // flat control flow
  'named constant',                        // no magic values
  'wrong abstraction',                     // DRY has a ceiling
  'cleaner than you found it',             // boy-scout closer
  // the safety carve-outs: pin each so a reword in either file
  // can't silently drop one.
  'input validation at trust boundaries',
  'prevents data loss',
  'security',
  'accessibility',
  'Changed behavior without its test is unfinished', // test reflex headline
];

const skill = read('skills/uncle-bob-junior/SKILL.md');
const sources = [['skills/uncle-bob-junior/SKILL.md', skill], ['AGENTS.md', agents]];
for (const phrase of INVARIANTS) {
  for (const [label, text] of sources) {
    if (!text.includes(phrase)) {
      console.error(`${label} is missing rule invariant: "${phrase}"`);
      failed = true;
    }
  }
}

if (failed) {
  console.error('Update the copied rule text, AGENTS.md, or SKILL.md so the shared rules match.');
  process.exit(1);
}

console.log(`Rule copies match AGENTS.md; ${INVARIANTS.length} rule invariants present in SKILL.md and AGENTS.md.`);
