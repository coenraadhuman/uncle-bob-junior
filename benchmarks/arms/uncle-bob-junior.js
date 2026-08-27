// Uncle Bob Junior arm: the repo's own SKILL.md (frontmatter stripped) as the
// system prompt. Single source of truth; the ruleset never drifts from the plugin.
const fs = require('fs');
const path = require('path');
const { DELIVERY_INSTRUCTION } = require('./delivery');

const SKILL_PATH = path.join(__dirname, '..', '..', 'skills', 'uncle-bob-junior', 'SKILL.md');
const system = fs.readFileSync(SKILL_PATH, 'utf8').replace(/^---[\s\S]*?---\s*/, '');

module.exports = ({ vars }) => [
  { role: 'system', content: system },
  { role: 'user', content: `${vars.task}\n\n${DELIVERY_INSTRUCTION}` },
];
