// Baseline arm: no ruleset, just the task plus the shared delivery contract.
const { DELIVERY_INSTRUCTION } = require('./delivery');

module.exports = ({ vars }) => [
  { role: 'user', content: `${vars.task}\n\n${DELIVERY_INSTRUCTION}` },
];
