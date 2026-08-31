// Deterministic clean-code metrics over a generated-code string. Zero
// dependencies, so the benchmark runs anywhere node runs. Handles brace
// languages (JS/TS/Java/C-like) and indent languages (Python) well enough to
// compare two arms of the same task; it is a ruler, not a compiler.
// ubj: regex-based scanning, swap in a real parser if per-language precision starts to matter.

const SHORT_NAME_ALLOWLIST = new Set(['i', 'j', 'k', 'e', 'id', 'fn', 'ok', 'db', '_']);
const NON_MAGIC_NUMBERS = new Set(['0', '1', '-1', '2', '10', '100', '1000']);
const DUPLICATE_WINDOW_LINES = 4;
const LONG_FUNCTION_THRESHOLD = 20;
const DEEP_NESTING_THRESHOLD = 2;
const PYTHON_INDENT_WIDTH = 4;

function stripComments(code, isPython) {
  if (isPython) {
    return code
      .replace(/("""|''')[\s\S]*?\1/g, '')
      .split('\n')
      .map((line) => line.replace(/(^|\s)#.*$/, ''))
      .join('\n');
  }
  return code
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .split('\n')
    .map((line) => line.replace(/(^|[^:])\/\/.*$/, '$1'))
    .join('\n');
}

function looksLikePython(code, lang) {
  if (/^(py|python)$/i.test(lang || '')) return true;
  if (lang) return false;
  return /^\s*def \w+\(.*\):/m.test(code) || /^\s*(import|from) \w+/m.test(code);
}

function codeLines(code) {
  return code.split('\n').map((raw) => raw.replace(/\s+$/, '')).filter((line) => line.trim() !== '');
}

function indentDepth(line) {
  const spaces = line.match(/^[ \t]*/)[0].replace(/\t/g, ' '.repeat(PYTHON_INDENT_WIDTH)).length;
  return Math.floor(spaces / PYTHON_INDENT_WIDTH);
}

// Function extents for indent languages: a def owns every deeper-indented line below it.
function pythonFunctionLengths(lines) {
  const lengths = [];
  for (let start = 0; start < lines.length; start++) {
    if (!/^\s*(async\s+)?def \w+\(/.test(lines[start])) continue;
    const baseDepth = indentDepth(lines[start]);
    let end = start + 1;
    while (end < lines.length && indentDepth(lines[end]) > baseDepth) end++;
    lengths.push(end - start);
  }
  return lengths;
}

// A line that starts a control block is never a function opener, even when it
// otherwise looks like a signature (e.g. `synchronized (lock) {`).
const CONTROL_FLOW_LINE = /^\s*}?\s*(?:if|for|while|switch|catch|do|try|else|return|new)\b/;

const FUNCTION_OPENERS = [
  /\bfunction\b.*\{/,                                  // JS function declaration/expression
  /(=>|->)\s*\{/,                                      // JS arrow / Java lambda body
  /^\s*[\w$]+\s*\([^)]*\)\s*\{\s*$/,                   // shorthand method: name() {
  // Java method with modifiers: public static boolean isValid(String email) {
  /^\s*(?:(?:public|private|protected|static|final|synchronized|abstract|default|native)\s+)+[\w$<>\[\],\s]*[\w$]+\s*\([^)]*\)\s*(?:throws[^{]*)?\{\s*$/,
  // package-private Java method: boolean isValid(String email) {
  /^\s*[\w$<>\[\]]+\s+[\w$]+\s*\([^)]*\)\s*(?:throws[^{]*)?\{\s*$/,
];

function opensFunction(line) {
  if (CONTROL_FLOW_LINE.test(line)) return false;
  return FUNCTION_OPENERS.some((pattern) => pattern.test(line));
}

// Function extents for brace languages: from a function-opening line to its matching brace.
function braceFunctionLengths(lines) {
  const lengths = [];
  for (let start = 0; start < lines.length; start++) {
    if (!opensFunction(lines[start])) continue;
    let depth = 0;
    for (let end = start; end < lines.length; end++) {
      depth += (lines[end].match(/\{/g) || []).length;
      depth -= (lines[end].match(/\}/g) || []).length;
      if (end > start || depth === 0) {
        if (depth <= 0) {
          lengths.push(end - start + 1);
          break;
        }
      }
    }
  }
  return lengths;
}

function maxBraceNesting(lines) {
  let depth = 0;
  let max = 0;
  for (const line of lines) {
    for (const ch of line) {
      if (ch === '{') {
        depth++;
        max = Math.max(max, depth);
      } else if (ch === '}') {
        depth--;
      }
    }
  }
  return max;
}

function maxIndentNesting(lines) {
  return lines.reduce((max, line) => Math.max(max, indentDepth(line)), 0);
}

// A literal is magic when it carries meaning but no name. Named-constant
// declarations (UPPER_CASE assignments, const declarations) are the fix, so
// literals on those lines don't count against the code.
function countMagicNumbers(lines) {
  let count = 0;
  const namesItsValue = /^(\s*(export\s+)?const\s+[A-Z][A-Z0-9_]*\s*=|\s*[A-Z][A-Z0-9_]*\s*(:\s*\w+\s*)?=[^=])/;
  const javaConstant = /\bfinal\b[^=]*\b[A-Z][A-Z0-9_]*\s*=/;
  for (const line of lines) {
    if (namesItsValue.test(line) || javaConstant.test(line)) continue;
    const numbers = line.match(/(?<![\w.])-?\d+(\.\d+)?(?![\w.])/g) || [];
    count += numbers.filter((n) => !NON_MAGIC_NUMBERS.has(n)).length;
  }
  return count;
}

// Declared names of one or two characters hide intent; conventional loop and
// error names are allowed.
function countShortNames(lines) {
  let count = 0;
  const declarations = [
    /\b(?:let|const|var)\s+([\w$]{1,2})\b/g,
    /\bdef\s+([\w]{1,2})\s*\(/g,
    /\bfunction\s+([\w$]{1,2})\s*\(/g,
    /\b(?:int|long|short|byte|double|float|boolean|char|String)\s+([a-zA-Z_$]{1,2})\b/g,
    /^\s*([\w]{1,2})\s*=[^==]/,
  ];
  for (const line of lines) {
    for (const pattern of declarations) {
      const matches = pattern.global ? [...line.matchAll(pattern)] : (line.match(pattern) ? [line.match(pattern)] : []);
      count += matches.filter((m) => !SHORT_NAME_ALLOWLIST.has(m[1])).length;
    }
  }
  return count;
}

// Distinct normalized windows of DUPLICATE_WINDOW_LINES lines that occur more
// than once: the cheapest honest duplication signal.
function countDuplicateBlocks(lines) {
  const normalized = lines.map((line) => line.trim().replace(/\s+/g, ' '));
  const seen = new Map();
  for (let i = 0; i + DUPLICATE_WINDOW_LINES <= normalized.length; i++) {
    const window = normalized.slice(i, i + DUPLICATE_WINDOW_LINES).join('\n');
    seen.set(window, (seen.get(window) || 0) + 1);
  }
  return [...seen.values()].filter((occurrences) => occurrences > 1).length;
}

// Java fields left mutable when final would do: the cheapest honest
// immutability signal. Static finals are constants; locals have no modifier
// and never match. A field initialised with a call (`= new ArrayList<>()`)
// keeps its parens after the `=`, so only parens before any `=` mark a
// method or constructor signature.
const JAVA_FIELD_DECL = /^\s*(?:private|protected|public)\s+(?!static\s+final\b|final\b)[\w<>\[\], ?.]+\s+\w+(\s*=.*)?;\s*$/;

function countMutableFields(lines, isPython) {
  if (isPython) return 0;
  let count = 0;
  for (const line of lines) {
    const parenAt = line.indexOf('(');
    const equalsAt = line.indexOf('=');
    const isSignature = parenAt !== -1 && (equalsAt === -1 || parenAt < equalsAt);
    if (isSignature) continue;
    if (JAVA_FIELD_DECL.test(line)) count++;
  }
  return count;
}

// Setters advertise mutable state; each one is a place an invariant can leak.
function countSetters(lines, isPython) {
  if (isPython) return 0;
  const setter = /\bvoid\s+set[A-Z]\w*\s*\(/;
  return lines.filter((line) => setter.test(line)).length;
}

function hasTestSignal(code) {
  return /\b(assert|test\(|describe\(|it\(|expect\(|unittest|pytest|node:test)\b/.test(code)
    || /@Test\b|\bassert\w+\s*\(|\borg\.junit\b/.test(code)
    // C#: xUnit/NUnit/MSTest attributes and their Assert classes.
    || /\[(Fact|Theory|Test|TestMethod)\]|\bAssert\.\w+\s*\(|using\s+(Xunit|NUnit)/.test(code);
}

function analyze(rawCode, lang) {
  const isPython = looksLikePython(rawCode, lang);
  const code = stripComments(String(rawCode || ''), isPython);
  const lines = codeLines(code);

  const functionLengths = isPython ? pythonFunctionLengths(lines) : braceFunctionLengths(lines);
  const maxNestingDepth = isPython ? maxIndentNesting(lines) : maxBraceNesting(lines);

  return {
    language: isPython ? 'python' : 'brace',
    loc: lines.length,
    functionCount: functionLengths.length,
    maxFunctionLength: Math.max(0, ...functionLengths),
    longFunctionCount: functionLengths.filter((length) => length > LONG_FUNCTION_THRESHOLD).length,
    maxNestingDepth,
    // "Nesting deeper than 2 levels inside a function": a Python def body
    // starts one indent in; brace-language code burns two levels of wrapper
    // (class + method) before any control flow.
    deeplyNested: maxNestingDepth > DEEP_NESTING_THRESHOLD + (isPython ? 1 : 2),
    magicNumberCount: countMagicNumbers(lines),
    shortNameCount: countShortNames(lines),
    duplicateBlockCount: countDuplicateBlocks(lines),
    mutableFieldCount: countMutableFields(lines, isPython),
    setterCount: countSetters(lines, isPython),
    hasTests: hasTestSignal(code),
  };
}

module.exports = {
  analyze,
  LONG_FUNCTION_THRESHOLD,
  DEEP_NESTING_THRESHOLD,
};
