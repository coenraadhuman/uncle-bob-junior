#!/usr/bin/env node
// Unit tests for the showcase-site content generator: featured-run selection
// (newest FULL run), history and subset sections, the Game of Life gallery,
// MDX escaping, findings rendering, and stale-page cleanup. Rendering MDX to
// HTML is Docusaurus's job (website/), deliberately not exercised here, and
// example scanning is injected so no habit-hooks CLI runs.

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const { buildSite, newestRunId, mdxEscape, hitSmells, isFullRun, readmeSection } = require('../benchmarks/build-site.js');
const { taskPrompts } = require('../benchmarks/reprocess-results.js');

const CANONICAL_TASKS = Object.keys(taskPrompts());

function rowFor(task, arm, score, overrides = {}) {
  return {
    task,
    model: 'claude-cli:haiku',
    arm,
    prompt: `Prompt for ${task} with <angle> brackets.`,
    score,
    gates: { validCode: true, shipsTests: arm === 'uncle-bob-junior', correct: true },
    habitPass: arm === 'uncle-bob-junior',
    smellCounts: { 'oversized-function': arm === 'uncle-bob-junior' ? 0 : 2, 'unused-import': 0 },
    ...overrides,
  };
}

function runData(evalId, tasks) {
  const rows = tasks.flatMap((task) => [rowFor(task, 'baseline (no ruleset)', 0.8), rowFor(task, 'uncle-bob-junior', 1)]);
  return {
    evalId,
    rows,
    means: [
      { key: 'claude-cli:haiku / baseline (no ruleset)', mean: 0.8, n: tasks.length },
      { key: 'claude-cli:haiku / uncle-bob-junior', mean: 1, n: tasks.length },
    ],
  };
}

const HABIT_HOOKS_REPORT = [
  '── oversized-function (2 issues) ──',
  '',
  'Functions over 12 lines carry more than one responsibility.',
  '',
  'OrderProcessor.java:5',
  'OrderProcessor.java:30',
].join('\n');

// A run directory shaped like export-results.js writes it. Only the first
// task gets source files; the pages tolerate rows without exported code.
function writeFixtureRun(resultsDir, runId, data) {
  const runDir = path.join(resultsDir, runId);
  const task = data.rows[0].task;
  const armDir = (arm) => path.join(runDir, 'src', task, 'claude-cli-haiku', arm);
  fs.mkdirSync(path.join(armDir('baseline-no-ruleset'), 'main'), { recursive: true });
  fs.mkdirSync(path.join(armDir('uncle-bob-junior'), 'main'), { recursive: true });
  fs.mkdirSync(path.join(armDir('uncle-bob-junior'), 'test'), { recursive: true });
  fs.mkdirSync(path.join(runDir, 'habit-hooks'), { recursive: true });
  fs.writeFileSync(path.join(armDir('baseline-no-ruleset'), 'main', 'OrderProcessor.java'),
    'public class OrderProcessor { List<Item> items; }');
  fs.writeFileSync(path.join(armDir('uncle-bob-junior'), 'main', 'Order.java'), 'public record Order() {}');
  fs.writeFileSync(path.join(armDir('uncle-bob-junior'), 'test', 'OrderTest.java'), 'class OrderTest {}');
  fs.writeFileSync(path.join(runDir, 'habit-hooks', `${task}-claude-cli-haiku-baseline-no-ruleset.md`), HABIT_HOOKS_REPORT);
  fs.writeFileSync(path.join(runDir, 'report.json'), JSON.stringify(data));
  return runDir;
}

function inTempDirs(run) {
  const resultsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-site-results-'));
  const siteDocsDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-site-docs-'));
  const gameOfLifeResultsDir = path.join(resultsDir, 'no-gol'); // absent unless a test creates it
  const build = (opts = {}) => buildSite(undefined, { resultsDir, siteDocsDir, gameOfLifeResultsDir, ...opts });
  try {
    return run({ resultsDir, siteDocsDir, gameOfLifeResultsDir, build });
  } finally {
    fs.rmSync(resultsDir, { recursive: true, force: true });
    fs.rmSync(siteDocsDir, { recursive: true, force: true });
  }
}

test('the newest FULL run is featured; older full runs land in history, partial runs in subset', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-old-2026-08-28t09-00-00', runData('eval-OLD', CANONICAL_TASKS));
    writeFixtureRun(resultsDir, 'eval-new-2026-08-30t09-00-00', runData('eval-NEW', CANONICAL_TASKS));
    writeFixtureRun(resultsDir, 'eval-part-2026-08-31t09-00-00', runData('eval-PART', [CANONICAL_TASKS[0]]));
    build();
    const scoreboard = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'scoreboard.mdx'), 'utf8');
    assert.ok(scoreboard.includes('eval-NEW'), 'newest full run is featured even when a subset run is newer');
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'history', 'eval-old-2026-08-28t09-00-00', 'scoreboard.mdx')), 'older full run in history');
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'subset', 'eval-part-2026-08-31t09-00-00', 'scoreboard.mdx')), 'partial run in subset');
    assert.ok(!fs.existsSync(path.join(siteDocsDir, 'benchmark', 'history', 'eval-new-2026-08-30t09-00-00')), 'featured run not duplicated in history');
    for (const section of ['history', 'subset']) {
      assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', section, '_category_.json')), `${section} category present`);
    }
  });
});

test('without any full run the newest run is featured as fallback', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-part-2026-08-29t09-00-00', runData('eval-PART', ['order']));
    build();
    const scoreboard = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'scoreboard.mdx'), 'utf8');
    assert.ok(scoreboard.includes('eval-PART'));
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'tasks', 'order.mdx')), 'task pages emitted for the featured run');
  });
});

test('task page shows escaped code, prompt, findings, and shipped tests', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-part-2026-08-29t09-00-00', runData('eval-PART', ['order']));
    build();
    const page = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'tasks', 'order.mdx'), 'utf8');
    assert.ok(page.includes("import Tabs from '@theme/Tabs'"), 'uses the Docusaurus Tabs component');
    assert.ok(page.includes('label="baseline (no ruleset) · 0.80"') && page.includes('label="uncle-bob-junior · 1.00"'));
    assert.ok(page.includes('````java\npublic class OrderProcessor { List<Item> items; }\n````'), 'code verbatim in four-backtick fences');
    assert.ok(page.includes('Prompt for order with &lt;angle&gt; brackets.'), 'prompt MDX-escaped');
    assert.ok(page.includes('`oversized-function` at line 5'), 'findings at their lines');
    assert.ok(page.includes('OrderTest.java'), 'shipped tests present');
  });
});

test('history runs mirror the featured layout: datetime category, scoreboard, and task pages', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-old-2026-08-28t09-00-00', runData('eval-OLD', CANONICAL_TASKS));
    writeFixtureRun(resultsDir, 'eval-new-2026-08-30t09-00-00', runData('eval-NEW', CANONICAL_TASKS));
    build();
    const runDir = path.join(siteDocsDir, 'benchmark', 'history', 'eval-old-2026-08-28t09-00-00');
    const category = JSON.parse(fs.readFileSync(path.join(runDir, '_category_.json'), 'utf8'));
    assert.equal(category.label, '2026-08-28 09:00', 'run categories are labelled by date and time');
    assert.equal(category.position, 1, 'newest non-featured run comes first');
    const scoreboard = fs.readFileSync(path.join(runDir, 'scoreboard.mdx'), 'utf8');
    assert.ok(scoreboard.includes('xychart-beta'), 'mermaid chart present');
    assert.ok(scoreboard.includes('| Oversized function |'), 'hit smells as columns');
    assert.ok(scoreboard.includes(`](tasks/${CANONICAL_TASKS[0]})`), 'scoreboard links into the run\'s own Tasks section');
    const taskPage = fs.readFileSync(path.join(runDir, 'tasks', `${CANONICAL_TASKS[0]}.mdx`), 'utf8');
    assert.ok(taskPage.includes('public class OrderProcessor { List<Item> items; }'), 'stored sources render on the run\'s task pages');
    assert.ok(taskPage.includes('`oversized-function` at line 5'), 'findings annotated');
  });
});

test('game of life run directories render one page per run with findings from the stored reports', () => {
  inTempDirs(({ resultsDir, siteDocsDir, gameOfLifeResultsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-part-2026-08-29t09-00-00', runData('eval-PART', ['order']));
    const golData = runData('eval-GOL', ['gameoflife']);
    const golRun = writeFixtureRun(gameOfLifeResultsDir, 'first-showcase', golData);
    fs.writeFileSync(
      path.join(golRun, 'habit-hooks', 'gameoflife-claude-cli-haiku-baseline-no-ruleset.md'),
      '── oversized-function (1 issue) ──\n\nblurb\n\nOrderProcessor.java:2\n',
    );
    build();
    const page = fs.readFileSync(path.join(siteDocsDir, 'gameoflife', 'first-showcase.mdx'), 'utf8');
    assert.ok(page.includes('title: eval-GOL'), 'page titled by the run');
    assert.ok(page.includes('OrderProcessor'), 'stored sources render');
    assert.ok(page.includes('`oversized-function` at line 2'), 'stored habit-hooks findings annotated');
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'gameoflife', '_category_.json')));
  });
});

test('a rebuild wipes pages from earlier runs but keeps hand-written docs', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    fs.writeFileSync(path.join(siteDocsDir, 'notes.md'), 'hand-written');
    writeFixtureRun(resultsDir, 'eval-a-2026-08-28t09-00-00', runData('eval-A', ['order']));
    build();
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'tasks', 'order.mdx')));
    writeFixtureRun(resultsDir, 'eval-b-2026-08-30t09-00-00', runData('eval-B', ['email']));
    build();
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'benchmark', 'tasks', 'email.mdx')), 'new featured task page written');
    assert.ok(!fs.existsSync(path.join(siteDocsDir, 'benchmark', 'tasks', 'order.mdx')), 'stale task page removed');
    assert.equal(fs.readFileSync(path.join(siteDocsDir, 'notes.md'), 'utf8'), 'hand-written');
  });
});

test('the homepage is a standalone React page and stale generated landings are removed', () => {
  const homepage = fs.readFileSync(path.join(__dirname, '..', 'website', 'src', 'pages', 'index.js'), 'utf8');
  for (const link of ['/ruleset/skill', '/plugin', '/benchmark']) {
    assert.ok(homepage.includes(`'${link}'`) || homepage.includes(`"${link}"`), `homepage links ${link}`);
  }
  assert.ok(homepage.includes('mascot-bot.svg'), 'homepage shows the mascot');
  assert.ok(fs.existsSync(path.join(__dirname, '..', 'website', 'static', 'img', 'mascot-bot.svg')), 'mascot asset exists');
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    fs.writeFileSync(path.join(siteDocsDir, 'index.mdx'), 'stale generated landing');
    writeFixtureRun(resultsDir, 'eval-a-2026-08-28t09-00-00', runData('eval-A', ['order']));
    build();
    assert.ok(!fs.existsSync(path.join(siteDocsDir, 'index.mdx')), 'a leftover generated landing would collide with the React homepage route');
  });
});

test('the benchmark intro opens the section with a trend line per model and section buttons', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-old-2026-08-28t09-00-00', runData('eval-OLD', CANONICAL_TASKS));
    writeFixtureRun(resultsDir, 'eval-new-2026-08-30t09-00-00', runData('eval-NEW', CANONICAL_TASKS));
    build();
    const intro = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'intro.mdx'), 'utf8');
    assert.ok(intro.includes('slug: /benchmark'), 'the intro owns the section URL');
    assert.ok(intro.includes('### claude-cli&#58;haiku'), 'one chart per model');
    assert.ok(intro.includes('xychart-beta'), 'trend chart is a line graph');
    assert.ok((intro.match(/^ {4}line \[/gm) || []).length === 2, 'two lines: baseline and ruleset');
    assert.ok(intro.includes('"08-28 09:00", "08-30 09:00"'), 'x-axis runs oldest to newest');
    for (const target of ['methodology', 'scoreboard', 'history', 'subset']) {
      assert.ok(intro.includes(`href="${target}"`), `button to ${target}`);
    }
    const category = JSON.parse(fs.readFileSync(path.join(siteDocsDir, 'benchmark', '_category_.json'), 'utf8'));
    assert.deepEqual(category.link, { type: 'doc', id: 'benchmark/intro' }, 'the category opens on the intro');
  });
});

test('a methodology page precedes the scoreboard, embedded from the benchmarks README', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-a-2026-08-28t09-00-00', runData('eval-A', ['order']));
    build();
    const page = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'methodology.md'), 'utf8');
    assert.ok(page.includes('sidebar_position: 1'), 'methodology comes first in the benchmark section');
    assert.ok(page.includes('habit-hooks'), 'explains the smell judge');
    assert.ok(page.includes('deterministic judges'), 'the benchmarks README preamble is embedded');
    assert.ok(page.includes('## Reading the results'), 'reading guidance embedded');
    assert.ok(page.includes('github.com/coenraadhuman/uncle-bob-junior/blob/main/benchmarks/'), 'relative links point into benchmarks/ on the repo');
    const scoreboard = fs.readFileSync(path.join(siteDocsDir, 'benchmark', 'scoreboard.mdx'), 'utf8');
    assert.ok(scoreboard.includes('sidebar_position: 2'), 'scoreboard follows the methodology');
  });
});

test('plugin, commands, and FAQ pages are embedded from the README with repo-safe links', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-a-2026-08-28t09-00-00', runData('eval-A', ['order']));
    build();
    const plugin = fs.readFileSync(path.join(siteDocsDir, 'plugin.md'), 'utf8');
    assert.ok(plugin.includes('/plugin install uncle-bob-junior@uncle-bob-junior'), 'install steps embedded');
    assert.ok(plugin.includes('### Plugin Update') && plugin.includes('### Uninstall'), 'subsections survive');
    assert.ok(plugin.includes('github.com/coenraadhuman/uncle-bob-junior/blob/main/AGENTS.md'), 'repo-relative links point at the repo');
    const commands = fs.readFileSync(path.join(siteDocsDir, 'commands.md'), 'utf8');
    assert.ok(commands.includes('/uncle-bob-junior-review'), 'command table embedded');
    assert.ok(commands.includes('**ultra**'), 'levels table embedded on the same page');
    const faq = fs.readFileSync(path.join(siteDocsDir, 'faq.md'), 'utf8');
    assert.ok(faq.includes('quick hack'), 'FAQ embedded');
  });
});

test('a removed README section fails the site build loudly', () => {
  assert.ok(readmeSection('Install').length > 0);
  assert.throws(() => readmeSection('No Such Heading'), /README\.md has no "## No Such Heading" section/);
});

test('the ruleset section carries SKILL.md verbatim with site-linked references', () => {
  inTempDirs(({ resultsDir, siteDocsDir, build }) => {
    writeFixtureRun(resultsDir, 'eval-a-2026-08-28t09-00-00', runData('eval-A', ['order']));
    build();
    const skill = fs.readFileSync(path.join(siteDocsDir, 'ruleset', 'skill.md'), 'utf8');
    assert.ok(skill.includes('slim core, depth on demand'), 'design note present');
    assert.ok(skill.includes('## Final gate'), 'the full ruleset body ships');
    assert.ok(skill.includes('](references/refactoring-moves)'), 'reference mentions become site links');
    assert.ok(!skill.includes('name: uncle-bob-junior'), 'skill frontmatter stripped');
    const reference = fs.readFileSync(path.join(siteDocsDir, 'ruleset', 'references', 'refactoring-moves.md'), 'utf8');
    assert.ok(reference.includes('title: Refactoring moves, by smell'), 'reference title from its heading');
    assert.ok(reference.includes('Extract Method'), 'reference body verbatim');
    assert.ok(fs.existsSync(path.join(siteDocsDir, 'ruleset', '_category_.json')));
  });
});

test('helpers: full-run detection, newest run, checklist extraction, escaping, hit smells, example grouping', () => {
  assert.equal(isFullRun(runData('x', CANONICAL_TASKS), CANONICAL_TASKS), true);
  assert.equal(isFullRun(runData('x', CANONICAL_TASKS.slice(1)), CANONICAL_TASKS), false);
  assert.equal(mdxEscape('<a> & {expr}'), '&lt;a&gt; &amp; &#123;expr&#125;');
  assert.equal(mdxEscape('claude-cli:haiku'), 'claude-cli&#58;haiku', 'colons escaped so remark-directive cannot eat :haiku');
  assert.deepEqual(hitSmells(runData('x', ['order']).rows), ['oversized-function']);
  inTempDirs(({ resultsDir }) => {
    writeFixtureRun(resultsDir, 'eval-a-2026-08-28t09-00-00', runData('eval-A', ['order']));
    writeFixtureRun(resultsDir, 'eval-b-2026-08-30t09-00-00', runData('eval-B', ['order']));
    assert.equal(newestRunId(resultsDir), 'eval-b-2026-08-30t09-00-00');
  });
});
