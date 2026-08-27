// Code extraction shared by the promptfoo judges, plus the ships-tests gate.
// Smell detection itself is habit-hooks-assert.js — an independent third-party
// judge — so this file only decides what code gets judged and whether tests
// shipped with it.
const { analyze } = require('./clean-code-metrics');

function fencedBlocks(text) {
  return [...String(text).matchAll(/```(\w*)\r?\n([\s\S]*?)```/g)]
    .map((m) => ({ lang: (m[1] || '').toLowerCase(), code: m[2] }));
}

// The largest fenced block carries the deliverable; fall back to the whole
// reply when the model answered with bare code.
function extractCode(text) {
  const blocks = fencedBlocks(text);
  if (blocks.length === 0) return { lang: '', code: String(text) };
  return blocks.reduce((a, b) => (b.code.length > a.code.length ? b : a));
}

// Smell judges measure production code only: counting a test's expected-value
// literals as smells would penalize exactly the arm that ships tests.
function isTestBlock(code) {
  return /@Test\b|\borg\.junit\b|\bclass\s+\w*Tests?\b/.test(code);
}

const NON_CODE_LANGS = new Set(['bash', 'sh', 'shell', 'xml', 'json', 'yaml', 'yml', 'properties', 'text', 'txt', 'sql']);

function productionBlocks(text) {
  return fencedBlocks(text)
    .filter((block) => !NON_CODE_LANGS.has(block.lang) && !isTestBlock(block.code));
}

function productionCode(text) {
  const production = productionBlocks(text);
  if (production.length === 0) return null;
  return {
    lang: (production.find((block) => block.lang) || production[0]).lang,
    code: production.map((block) => block.code).join('\n'),
  };
}

// Gate on the ruleset's headline rule: new behavior ships with tests. Tests
// often land in a separate block, so this reads the whole reply.
function shipsTests(output) {
  const { lang } = productionCode(String(output || '')) || extractCode(String(output || ''));
  const shipped = analyze(String(output || ''), lang).hasTests;
  return { pass: shipped, score: shipped ? 1 : 0, reason: shipped ? 'ships tests' : 'no tests shipped' };
}

module.exports = {
  fencedBlocks,
  extractCode,
  isTestBlock,
  productionBlocks,
  productionCode,
  shipsTests,
};
