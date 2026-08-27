// Unit tests for the benchmark runner's source-tree writer: generated code
// must land as real files under src/<task>/<arm>-run<N>/, named after their
// declared Java type, without clobbering alternative implementations.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { writeRunSources, scoreRun } = require('../benchmarks/run-clean-code.js');

test('smell metrics score production code only; tests count toward ships-tests', () => {
  const replyText = '```java\npublic class Adder {\n    static int add(int a, int b) { return a + b; }\n}\n```\n\n```java\nimport org.junit.jupiter.api.Test;\nclass AdderTest {\n    @Test\n    void adds() { assertEquals(4321, Adder.add(4000, 321)); }\n}\n```';
  const score = scoreRun({ text: replyText }, { prompt: 'unknown task' });
  assert.equal(score.magicNumberCount, 0, 'test expectation literals must not count as magic');
  assert.equal(score.hasTests, true);
});

test('a test-only reply still gets measured via the fallback', () => {
  const replyText = '```java\nimport org.junit.jupiter.api.Test;\nclass OnlyTest {\n    @Test\n    void checks() { assertEquals(4321, compute()); }\n}\n```';
  const score = scoreRun({ text: replyText }, { prompt: 'unknown task' });
  assert.ok(score.loc > 0);
  assert.equal(score.hasTests, true);
});

function inTmpRunDir(entry) {
  const runDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-src-test-'));
  writeRunSources(runDir, entry);
  const cellDir = path.join(runDir, 'src', entry.task, `${entry.arm}-run${entry.run + 1}`);
  const files = fs.readdirSync(cellDir).sort();
  const contents = Object.fromEntries(files.map((name) => [name, fs.readFileSync(path.join(cellDir, name), 'utf8')]));
  fs.rmSync(runDir, { recursive: true, force: true });
  return { files, read: (name) => contents[name] };
}

test('java blocks are saved as files named after their declared type', () => {
  const replyText = 'Here you go:\n\n```java\npublic class EmailValidator {\n    static boolean isValid(String e) { return e.contains("@"); }\n}\n```\n\nAnd a test:\n\n```java\nclass EmailValidatorTest {\n    void run() {}\n}\n```';
  const { files, read } = inTmpRunDir({ task: 'email', arm: 'ubj', run: 0, replyText });
  assert.deepEqual(files, ['EmailValidator.java', 'EmailValidatorTest.java']);
  assert.match(read('EmailValidator.java'), /class EmailValidator/);
});

test('alternative implementations of the same type both survive', () => {
  const replyText = '```java\npublic class Validator { boolean a(String s) { return true; } }\n```\n\nOr stricter:\n\n```java\npublic class Validator { boolean b(String s) { return false; } }\n```';
  const { files } = inTmpRunDir({ task: 'email', arm: 'baseline', run: 1, replyText });
  assert.deepEqual(files, ['Validator.java', 'alt-2-Validator.java']);
});

test('non-java blocks get numbered names with the right extension', () => {
  const replyText = '```xml\n<project/>\n```\n\n```bash\nmvn test\n```\n\n```\nplain notes\n```';
  const { files } = inTmpRunDir({ task: 'order', arm: 'ubj', run: 0, replyText });
  assert.deepEqual(files, ['block-1.xml', 'block-2.sh', 'block-3.txt']);
});

test('a reply with no fenced code is kept as reply.txt', () => {
  const { files, read } = inTmpRunDir({ task: 'retry', arm: 'baseline', run: 0, replyText: 'I need permission to write the file.' });
  assert.deepEqual(files, ['reply.txt']);
  assert.match(read('reply.txt'), /permission/);
});
