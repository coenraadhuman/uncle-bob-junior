#!/usr/bin/env node
// Generate the OpenClaw / ClawHub skill package (.openclaw/skills/) from the
// canonical skills/. OpenClaw skills are SKILL.md (frontmatter + body), the same
// format uncle-bob-junior already uses, with one difference: `description` must be a
// single line under 160 chars. The canonical descriptions are long (tuned for
// Claude's skill picker), so each ships a short one here. The body is copied
// verbatim from skills/<name>/SKILL.md so the ruleset never drifts; only the
// frontmatter is rewritten.
//
// Run:  node scripts/build-openclaw-skills.js
// tests/openclaw-skills.test.js fails if the committed copies are stale.

const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const HOMEPAGE = 'https://github.com/coenraadhuman/uncle-bob-junior';

const DESCRIPTIONS = {
  'uncle-bob-junior': 'Clean-code mode for any coding task (write, refactor, fix, review): one job per function, intent-revealing names, flat flow, tests. Not for non-coding requests.',
  'uncle-bob-junior-review': 'Review a diff for clean-code violations: vague names, long functions, deep nesting, duplication, magic values, missing tests. One line per finding.',
  'uncle-bob-junior-audit': 'Audit the whole repo for clean-code violations. A ranked list of the hardest-to-read, hardest-to-change spots, hot files first.',
  'uncle-bob-junior-debt': 'Harvest every ubj: deviation comment into one cleanup ledger, so deferrals get tracked instead of forgotten. One-shot report.',
  'uncle-bob-junior-gain': 'Show the measured with/without scoreboard from the newest promptfoo eval: checklist gates, smell penalties, tests. One-shot display.',
  'uncle-bob-junior-help': "Quick reference for uncle-bob-junior's modes, skills, and commands. One-shot display.",
};

const NAMES = Object.keys(DESCRIPTIONS);

function sourceBody(name) {
  const src = fs.readFileSync(path.join(ROOT, 'skills', name, 'SKILL.md'), 'utf8').replace(/\r\n/g, '\n');
  const fm = src.match(/^---\n[\s\S]*?\n---\n?/);
  if (!fm) throw new Error(`skills/${name}/SKILL.md has no frontmatter`);
  return src.slice(fm[0].length);
}

function render(name) {
  const desc = DESCRIPTIONS[name];
  if (desc.length > 160 || desc.includes('\n') || desc.includes('"')) {
    throw new Error(`description for ${name} must be one line, no quotes, under 160 chars`);
  }
  const frontmatter =
    `---\nname: ${name}\ndescription: "${desc}"\nhomepage: ${HOMEPAGE}\nlicense: MIT\n---\n`;
  return frontmatter + sourceBody(name);
}

function outPath(name) {
  return path.join(ROOT, '.openclaw', 'skills', name, 'SKILL.md');
}

module.exports = { DESCRIPTIONS, NAMES, render, outPath, sourceBody };

if (require.main === module) {
  for (const name of NAMES) {
    const p = outPath(name);
    fs.mkdirSync(path.dirname(p), { recursive: true });
    fs.writeFileSync(p, render(name));
    console.log('wrote', path.relative(ROOT, p).replace(/\\/g, '/'));
  }
}
