// Promptfoo asserts wrapping the repo's deterministic judges
// (clean-code-metrics.js). Reference one as
// `file://promptfoo-metrics.js:<name>` in promptfooconfig.yaml.
//
// Three kinds, so the arm comparison reads correctly in the promptfoo UI
// (which treats higher score as better and aggregates pass rates):
//   - gates: pass/fail on the ruleset's own checklist rules (functions ≤ 20
//     lines, nesting ≤ 2 inside a method, tests ship). Baseline failing these
//     is the finding, not noise.
//   - penalties: smell counts normalized to a 0..1 score where 1 = clean, so
//     more magic numbers means a lower score. Never fail: the rulers have
//     known false positives, so counts inform, thresholds don't gate.
//   - raw measurements: plain counts for reading, weight 0 in the config so
//     they never skew the aggregate score.
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

// Smell metrics measure production code only: counting a test's expected-value
// literals as magic numbers would penalize exactly the arm that ships tests.
function isTestBlock(code) {
  return /@Test\b|\borg\.junit\b|\bclass\s+\w*Tests?\b/.test(code);
}

const NON_CODE_LANGS = new Set(['bash', 'sh', 'shell', 'xml', 'json', 'yaml', 'yml', 'properties', 'text', 'txt', 'sql']);

function productionCode(text) {
  const production = fencedBlocks(text)
    .filter((block) => !NON_CODE_LANGS.has(block.lang) && !isTestBlock(block.code));
  if (production.length === 0) return null;
  return {
    lang: (production.find((block) => block.lang) || production[0]).lang,
    code: production.map((block) => block.code).join('\n'),
  };
}

// Test-only or unfenced replies fall back to the largest block, so they still
// get measured instead of scoring an empty string.
function productionMetrics(output) {
  const { lang, code } = productionCode(String(output || '')) || extractCode(String(output || ''));
  return analyze(code, lang);
}

function rawMeasurement(field, label) {
  return (output) => {
    const value = productionMetrics(output)[field];
    return { pass: true, score: value, reason: `${value} ${label}` };
  };
}

// 1 = clean, falling linearly to 0 at `worst` occurrences.
function penalty(field, label, worst) {
  return (output) => {
    const value = productionMetrics(output)[field];
    const score = Math.max(0, 1 - value / worst);
    return { pass: true, score, reason: `${value} ${label}` };
  };
}

function gate(check, passReason, failReason) {
  return (output) => {
    const pass = check(productionMetrics(output), output);
    return { pass, score: pass ? 1 : 0, reason: pass ? passReason : failReason(productionMetrics(output)) };
  };
}

// Raw measurements — give these weight: 0 in promptfooconfig.yaml.
const codeLoc = rawMeasurement('loc', 'code LOC');
const longestFunction = rawMeasurement('maxFunctionLength', 'lines in longest function');
const maxNesting = rawMeasurement('maxNestingDepth', 'max nesting depth');

// Penalties — smell density, 1 = clean.
const magicNumbers = penalty('magicNumberCount', 'magic numbers', 8);
const mutableFields = penalty('mutableFieldCount', 'mutable fields', 4);
const setters = penalty('setterCount', 'setters', 4);

// Gates — the checklist's own rules.
const noLongFunctions = gate(
  (m) => m.longFunctionCount === 0,
  'no function over 20 lines',
  (m) => `${m.longFunctionCount} function(s) over 20 lines (longest: ${m.maxFunctionLength})`,
);

// analyze() counts braces from the file top, so a Java method body sits at
// depth 2 and deeplyNested fires past depth 4, i.e. nesting > 2 inside a method.
const flatControlFlow = gate(
  (m) => !m.deeplyNested,
  'control flow stays flat (nesting <= 2 inside a method)',
  (m) => `nesting reaches brace depth ${m.maxNestingDepth} (deeper than 2 levels inside a method)`,
);

// Tests often land in a separate block, so this one reads the whole reply.
function shipsTests(output) {
  const { lang } = productionCode(String(output || '')) || extractCode(String(output || ''));
  const shipped = analyze(String(output || ''), lang).hasTests;
  return { pass: shipped, score: shipped ? 1 : 0, reason: shipped ? 'ships tests' : 'no tests shipped' };
}

module.exports = {
  codeLoc,
  longestFunction,
  maxNesting,
  magicNumbers,
  mutableFields,
  setters,
  noLongFunctions,
  flatControlFlow,
  shipsTests,
};
