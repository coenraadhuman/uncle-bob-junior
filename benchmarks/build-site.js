#!/usr/bin/env node
// Generates the showcase site's content from the stored benchmark runs:
//
//   node benchmarks/build-site.js
//
// The site itself is Docusaurus (website/) — libraries over wheels — and this
// script only writes its MDX content:
//   website/docs/index.mdx                  landing: the ruleset checklist
//   website/docs/plugin.md                  README's Install/Update/Uninstall
//   website/docs/commands.md                README's Commands and Levels
//   website/docs/faq.md                     README's FAQ
//   website/docs/ruleset/skill.md           SKILL.md verbatim + design note
//   website/docs/ruleset/references/*.md    the reference files, verbatim
//   website/docs/benchmark/scoreboard.mdx   the newest FULL run (all tasks)
//   website/docs/benchmark/<task>.mdx       that run's code, baseline vs ruleset
//   website/docs/benchmark/history/*.mdx    past full runs, one scoreboard each
//   website/docs/benchmark/subset/*.mdx     runs covering only part of the task set
//   website/docs/gameoflife/*.mdx           the Game of Life showcase replies
//
// Publish with `npm --prefix website run build` (renderSite()), which renders
// static HTML into the repo's /docs for GitHub Pages. Every run directory
// must carry a report.json; re-judge old runs with reprocess-results.js.
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const { parseIssues } = require('./habit-hooks-assert');
const { slug } = require('./export-results');
const { taskPrompts } = require('./reprocess-results');

const RESULTS_DIR = path.join(__dirname, 'results');
// Not imported from gameoflife-examples.js: that module requires this one.
const GAME_OF_LIFE_RESULTS_DIR = path.join(__dirname, 'game-of-life-results');
const WEBSITE_DIR = path.join(__dirname, '..', 'website');
const SITE_DOCS_DIR = path.join(WEBSITE_DIR, 'docs');
const SKILL_PATH = path.join(__dirname, '..', 'plugins', 'uncle-bob-junior', 'skills', 'uncle-bob-junior', 'SKILL.md');
const REFERENCES_DIR = path.join(path.dirname(SKILL_PATH), 'references');
const README_PATH = path.join(__dirname, '..', 'README.md');
const BENCH_README_PATH = path.join(__dirname, 'README.md');
const REPO_URL = 'https://github.com/coenraadhuman/uncle-bob-junior';
// Outer fences use four backticks so model code containing ``` cannot close them.
const FENCE = '````';
const RENDER_TIMEOUT_MS = 300_000;

// Plain text dropped into MDX prose: neutralize the characters MDX would
// read as JSX, expressions, or directives (remark-directive eats ':name',
// which truncated 'claude-cli:haiku' to 'claude-cli' in headings).
function mdxEscape(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\{/g, '&#123;')
    .replace(/\}/g, '&#125;')
    .replace(/:/g, '&#58;');
}

function runStamp(name) {
  return name.match(/\d{4}-\d{2}-\d{2}t[\d-]+/)?.[0] || '';
}

// Human page title for a run: its date and time when the id carries one
// ('eval-dwh-2026-08-31t11-59-48' -> '2026-08-31 11:59'), else the id.
function runTitle(run) {
  const stamp = runStamp(run.id);
  if (!stamp) return run.data.evalId || run.id;
  const [date, time] = stamp.split('t');
  const [hours, minutes] = time.split('-');
  return `${date} ${hours}:${minutes}`;
}

// Every stored run with a report.json, newest first.
function allRuns(resultsDir) {
  if (!fs.existsSync(resultsDir)) return [];
  return fs.readdirSync(resultsDir)
    .filter((name) => fs.existsSync(path.join(resultsDir, name, 'report.json')))
    .sort((a, b) => runStamp(b).localeCompare(runStamp(a)))
    .map((name) => ({
      id: name,
      dir: path.join(resultsDir, name),
      data: JSON.parse(fs.readFileSync(path.join(resultsDir, name, 'report.json'), 'utf8')),
    }));
}

function newestRunId(resultsDir) {
  return allRuns(resultsDir)[0]?.id || null;
}

// A run is full when it covers every task in the current config; anything
// else (iteration runs, runs predating newer tasks) is a subset run.
function isFullRun(data, canonicalTasks) {
  const covered = new Set(data.rows.map((row) => row.task));
  return canonicalTasks.every((task) => covered.has(task));
}

// The homepage is a hand-written React page (website/src/pages/index.js), so
// the docs root must stay free: builds from before that change left a
// generated index.mdx behind, which would collide with the page route.
function removeGeneratedLanding(siteDocsDir) {
  fs.rmSync(path.join(siteDocsDir, 'index.mdx'), { force: true });
}

// --- Pages embedded from the README ------------------------------------------

// One `## Heading` section of a README, verbatim (its ### subsections
// included). Throws when the heading is gone so a README restructure breaks
// the site build loudly instead of silently dropping a page.
function readmeSection(heading, readmePath = README_PATH) {
  const readme = fs.readFileSync(readmePath, 'utf8');
  const section = readme.split(/\n(?=## )/).find((part) => part.startsWith(`## ${heading}`));
  if (!section) throw new Error(`${path.basename(readmePath)} has no "## ${heading}" section for the site to embed`);
  return section.replace(`## ${heading}`, '').trim();
}

// Everything between a README's H1 and its first `## ` section.
function readmePreamble(readmePath) {
  const readme = fs.readFileSync(readmePath, 'utf8');
  return readme.replace(/^#\s.+\n/, '').split(/\n(?=## )/)[0].trim();
}

// Repo-relative links work on GitHub, not on the site — point them at the
// repo, prefixed with the directory the source README lives in.
function repoLinks(markdown, baseDir = '') {
  return markdown.replace(/\]\((?!https?:|#|\/)([^)]+)\)/g, `](${REPO_URL}/blob/main/${baseDir}$1)`);
}

function readmePage(title, position, headings) {
  const body = headings
    .map((heading) => `## ${heading}\n\n${repoLinks(readmeSection(heading))}`)
    .join('\n\n');
  return `---
title: ${title}
sidebar_position: ${position}
---

*Embedded from the repository README on every site build.*

${body}
`;
}

function writeReadmePages(siteDocsDir) {
  fs.writeFileSync(path.join(siteDocsDir, 'plugin.md'), readmePage('Claude Code Plugin', 5, ['Install']));
  fs.writeFileSync(path.join(siteDocsDir, 'commands.md'), readmePage('Commands & Levels', 6, ['Commands', 'Levels']));
  fs.writeFileSync(path.join(siteDocsDir, 'faq.md'), readmePage('FAQ', 7, ['FAQ']));
}

// How the benchmark works — arms, judges (habit-hooks smell detection, the
// gates), scoring threshold, and the task set — embedded from the benchmarks
// README so the site never drifts from the method actually in force.
function methodologyPage() {
  const embed = (markdown) => repoLinks(markdown, 'benchmarks/');
  return `---
title: Methodology
sidebar_position: 1
---

*Embedded from \`benchmarks/README.md\` on every site build.*

${embed(readmePreamble(BENCH_README_PATH))}

## Running it, and the tasks

${embed(readmeSection('Run it', BENCH_README_PATH))}

## Reading the results

${embed(readmeSection('Reading the results', BENCH_README_PATH))}
`;
}

// --- The ruleset section -----------------------------------------------------

// SKILL.md verbatim, with its reference mentions rewritten into site links.
function skillBody() {
  return fs.readFileSync(SKILL_PATH, 'utf8')
    .replace(/^---[\s\S]*?---\s*/, '')
    .replace(/`references\/([\w-]+)\.md`/g, '[`references/$1.md`](references/$1)');
}

function skillPage() {
  return `---
title: SKILL.md
sidebar_position: 1
---

This page is generated from the plugin's \`SKILL.md\` on every site build, so
it always shows the ruleset exactly as Claude Code injects it.

**Design: slim core, depth on demand.** The core below is injected into every
session: the checklist, the rules, and a final gate placed last so it is the
freshest instruction in the model's context. Everything explanatory lives in
the reference files, which the agent reads only when a task needs them (the
injecting hook rewrites the reference mentions to absolute paths). The
benefits, measured by the [benchmark](../benchmark/scoreboard): a lighter
always-on prompt with no loss of force — the imperative gate lines are what
bind, the coaching prose was movable — and one source of truth shared by the
hook, the benchmark arm, and this page.

---

${skillBody()}
`;
}

function referencePage(fileName, position) {
  const raw = fs.readFileSync(path.join(REFERENCES_DIR, fileName), 'utf8');
  const title = raw.match(/^#\s+(.+)$/m)?.[1] || fileName;
  const body = raw.replace(/^#\s+.+\n/, '');
  return `---
title: ${title}
sidebar_position: ${position}
---

${body}`;
}

function writeRuleset(siteDocsDir) {
  const dir = path.join(siteDocsDir, 'ruleset');
  fs.rmSync(dir, { recursive: true, force: true });
  writeCategory(dir, 'The Ruleset', 2, 'The clean-code rules exactly as the plugin injects them, plus the on-demand reference files.', '/ruleset');
  fs.writeFileSync(path.join(dir, 'skill.md'), skillPage());
  if (!fs.existsSync(REFERENCES_DIR)) return;
  writeCategory(path.join(dir, 'references'), 'References', 2, 'Depth the slim core points at: the agent reads these on demand.', '/ruleset/references');
  fs.readdirSync(REFERENCES_DIR).filter((name) => name.endsWith('.md')).sort().forEach((name, index) => {
    fs.writeFileSync(path.join(dir, 'references', name), referencePage(name, index + 1));
  });
}

function gateCells(row) {
  const mark = (pass) => (pass ? 'Yes' : '**No**');
  const habit = row.habitPass === null ? 'n/a' : row.habitPass ? 'Pass' : '**Fail**';
  return `${mark(row.gates.validCode !== false)} | ${habit} | ${mark(row.gates.shipsTests)} | ${mark(row.gates.correct)}`;
}

// 'too-many-parameters' -> 'Too many parameters', for table headers.
function humanize(rule) {
  const words = rule.replace(/-/g, ' ');
  return words.charAt(0).toUpperCase() + words.slice(1);
}

// Smells at least one row hit, in catch-list order (report.json preserves it).
function hitSmells(rows) {
  const order = Object.keys(rows[0]?.smellCounts || {});
  return order.filter((rule) => rows.some((row) => (row.smellCounts[rule] || 0) > 0));
}

function meanChart(means) {
  return [
    '```mermaid',
    'xychart-beta',
    '    title "Mean score per model and arm"',
    `    x-axis [${means.map(({ key }) => `"${key}"`).join(', ')}]`,
    '    y-axis "mean score" 0 --> 1',
    `    bar [${means.map(({ mean }) => mean.toFixed(3)).join(', ')}]`,
    '```',
  ].join('\n');
}

function scoreboardTable(rows, { linkTasks = true } = {}) {
  const smells = hitSmells(rows);
  const header = `| Task | Arm | Score | Valid code | Habit-hooks | Ships tests | Correct | ${smells.map(humanize).join(' | ')} |`;
  const divider = `|---|---|---:|:---:|:---:|:---:|:---:|${smells.map(() => '---:').join('|')}|`;
  const body = rows.map((row) => {
    const task = linkTasks ? `[${mdxEscape(row.task)}](tasks/${slug(row.task)})` : mdxEscape(row.task);
    return `| ${task} | ${mdxEscape(`${row.model} · ${row.arm}`)} | ${row.score.toFixed(2)} | ${gateCells(row)} | ` +
      smells.map((rule) => row.smellCounts[rule] ?? 'n/a').join(' | ') + ' |';
  });
  return [header, divider, ...body].join('\n');
}

function runSummary(data) {
  return `Run \`${data.evalId}\`: the same tasks and models, once bare (baseline) and once
with the uncle-bob-junior ruleset as system prompt, judged by
[habit-hooks](https://github.com/habit-hooks/habit-hooks) plus valid-code,
ships-tests, and correctness gates. Single-shot generations, so expect
run-to-run variance.`;
}

// 'eval-x-2026-08-31t11-59-48' -> '08-31 11:59', compact enough for an x-axis.
function chartLabel(run) {
  const stamp = runStamp(run.id);
  if (!stamp) return run.id.slice(0, 11);
  const [date, time] = stamp.split('t');
  const [hours, minutes] = time.split('-');
  return `${date.slice(5)} ${hours}:${minutes}`;
}

// One line chart per model: mean score per run, oldest to newest, baseline
// and ruleset as two lines. Mermaid's xychart has no legend, so the text
// names the series; the ruleset is the upper line throughout the history.
function trendCharts(runs) {
  const chronological = [...runs].reverse();
  const models = [...new Set(chronological.flatMap((run) => run.data.means.map((m) => m.key.split(' / ')[0])))].sort();
  return models.map((model) => {
    const meanOf = (run, arm) => run.data.means.find((m) => m.key === `${model} / ${arm}`)?.mean;
    const points = chronological.filter((run) => meanOf(run, 'uncle-bob-junior') !== undefined && meanOf(run, 'baseline (no ruleset)') !== undefined);
    if (points.length < 2) return '';
    return [
      `### ${mdxEscape(model)}`,
      '',
      '```mermaid',
      'xychart-beta',
      `    title "${model} — mean score per run (upper line: ruleset, lower: baseline)"`,
      `    x-axis [${points.map((run) => `"${chartLabel(run)}"`).join(', ')}]`,
      '    y-axis "mean score" 0 --> 1',
      `    line [${points.map((run) => meanOf(run, 'baseline (no ruleset)').toFixed(3)).join(', ')}]`,
      `    line [${points.map((run) => meanOf(run, 'uncle-bob-junior').toFixed(3)).join(', ')}]`,
      '```',
    ].join('\n');
  }).filter(Boolean).join('\n\n');
}

// The benchmark section's front door: what the benchmark is, how the two
// arms have trended across the stored history, and where to go next.
function introPage(runs) {
  const button = (to, label) => `<a className="button button--primary" href="${to}">${label}</a>`;
  return `---
title: Benchmark
slug: /benchmark
---

Does the ruleset actually change the code Claude writes? Every stored run
answers with the same experiment: identical tasks, once bare (baseline) and
once with the uncle-bob-junior ruleset as system prompt, judged by
[habit-hooks](https://github.com/habit-hooks/habit-hooks) and correctness
gates — no LLM grading.

## The trend, oldest to newest

Mean score per run and arm. Task sets grew over time (each run's own pages
list its coverage), so read the lines as the arms' gap per run rather than
one continuous series.

${trendCharts(runs)}

## Dig in

<div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
${button('methodology', 'Methodology')}
${button('scoreboard', 'Featured scoreboard')}
${button('history', 'Past runs')}
${button('subset', 'Subset runs')}
</div>
`;
}
function scoreboardPage(data, { title = 'Scoreboard', position = 1, footer = '' } = {}) {
  return `---
title: ${title}
sidebar_position: ${position}
---

${runSummary(data)}

## Mean score per model and arm

${meanChart(data.means)}

## Per task

${scoreboardTable(data.rows)}

The generated code per task lives under [Tasks](tasks).${footer}
`;
}

// Findings for one arm keyed by file name: { 'Foo.java': [{rule, line}] }.
function findingsByFile(runDir, row) {
  const reportPath = path.join(runDir, 'habit-hooks', `${slug(row.task)}-${slug(row.model)}-${slug(row.arm)}.md`);
  if (!fs.existsSync(reportPath)) return {};
  return groupFindings(parseIssues(fs.readFileSync(reportPath, 'utf8')));
}

function groupFindings(issues) {
  const byFile = {};
  for (const issue of issues) {
    for (const location of issue.locations) {
      const [file, line] = location.split(':');
      (byFile[file] = byFile[file] || []).push({ rule: issue.rule, line: line || '' });
    }
  }
  return byFile;
}

function sourceFiles(runDir, row, kind) {
  const dir = path.join(runDir, 'src', slug(row.task), slug(row.model), slug(row.arm), kind);
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true })
    // Older runs may carry sensor droppings (.ruff_cache/); only real sources render.
    .filter((entry) => entry.isFile() && !entry.name.startsWith('.'))
    .map((entry) => entry.name)
    .sort()
    .map((name) => ({ name, content: fs.readFileSync(path.join(dir, name), 'utf8') }));
}

function fileSection(file, findings, open) {
  const notes = findings.map(({ rule, line }) => `- \`${rule}\` at line ${line}`).join('\n');
  const flag = findings.length ? ` · ${findings.length} smell${findings.length > 1 ? 's' : ''}` : '';
  const language = path.extname(file.name).slice(1) || 'text';
  return `<details${open ? ' open' : ''}>
<summary><code>${mdxEscape(file.name)}</code>${mdxEscape(flag)}</summary>

${notes ? `${notes}\n` : ''}
${FENCE}${language}
${file.content.replaceAll('```', '`​`​`')}
${FENCE}

</details>`;
}

function codeSections(main, tests, findings) {
  const mainSections = main.map((file, index) => fileSection(file, findings[file.name] || [], index === 0)).join('\n\n');
  const testSections = tests.length
    ? `<details>\n<summary>Shipped tests (${tests.length} file${tests.length > 1 ? 's' : ''})</summary>\n\n${tests.map((file) => fileSection(file, [], false)).join('\n\n')}\n\n</details>`
    : '*No tests shipped.*';
  return `${mainSections || '*No production code extracted.*'}\n\n${testSections}`;
}

function armTab(runDir, row, value) {
  const findings = findingsByFile(runDir, row);
  const main = sourceFiles(runDir, row, 'main');
  const tests = sourceFiles(runDir, row, 'test');
  return `<TabItem value="${value}" label="${mdxEscape(row.arm)} · ${row.score.toFixed(2)}">

Gates: valid code ${row.gates.validCode !== false ? 'Yes' : '**No**'} ·
habit-hooks ${row.habitPass === null ? 'n/a' : row.habitPass ? 'Pass' : '**Fail**'} ·
ships tests ${row.gates.shipsTests ? 'Yes' : '**No**'} ·
correct ${row.gates.correct ? 'Yes' : '**No**'}

${codeSections(main, tests, findings)}

</TabItem>`;
}

// A --repeat run carries several rows per model × arm; the run directory
// keeps only the last repetition's files, so the tabs show that one.
function lastRowPerArm(rows) {
  const byKey = new Map();
  for (const row of rows) byKey.set(`${row.model}|${row.arm}`, row);
  return [...byKey.values()];
}

const TABS_IMPORT = "import Tabs from '@theme/Tabs';\nimport TabItem from '@theme/TabItem';";

// Baseline-vs-ruleset code tabs for one task's rows, one section per model.
function modelSections(runDir, allRows, heading) {
  const rows = lastRowPerArm(allRows);
  const models = [...new Set(rows.map((row) => row.model))];
  return models.map((model) => {
    const arms = rows
      .filter((row) => row.model === model)
      .sort((a, b) => Number(b.arm.includes('baseline')) - Number(a.arm.includes('baseline')));
    const tabs = arms.map((row) => armTab(runDir, row, slug(row.arm))).join('\n\n');
    return `${heading} ${mdxEscape(model)}\n\n<Tabs>\n\n${tabs}\n\n</Tabs>`;
  }).join('\n\n');
}

function taskPage(runDir, task, allRows, { title = task, position = null } = {}) {
  return `---
title: ${title}
${position === null ? '' : `sidebar_position: ${position}\n`}---

${TABS_IMPORT}

> ${mdxEscape(allRows[0].prompt || '')}

${modelSections(runDir, allRows, '##')}
`;
}

// --- Assembly ----------------------------------------------------------------

// slug pins the generated index's URL so in-page links can target it; without
// one Docusaurus defaults to /category/<label>, which nothing links to. Pass
// an object instead of a slug string to use a custom link (e.g. a doc).
function writeCategory(dir, label, position, description, slugOrLink) {
  const link = typeof slugOrLink === 'object'
    ? slugOrLink
    : { type: 'generated-index', description, slug: slugOrLink };
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, '_category_.json'), JSON.stringify({ label, position, link }, null, 2) + '\n');
}

// The Tasks section of a run: one code page per task, baseline vs ruleset.
function writeTaskPages(dir, run, baseSlug, categoryPosition) {
  const tasksDir = path.join(dir, 'tasks');
  writeCategory(tasksDir, 'Tasks', categoryPosition, 'The run\'s generated code, task by task: baseline vs ruleset.', `${baseSlug}/tasks`);
  [...new Set(run.data.rows.map((row) => row.task))].forEach((task, index) => {
    const rows = run.data.rows.filter((row) => row.task === task);
    fs.writeFileSync(path.join(tasksDir, `${slug(task)}.mdx`), taskPage(run.dir, task, rows, { position: index + 1 }));
  });
}

// Past and subset runs mirror the featured layout: one datetime-named
// category per run holding its Scoreboard and Tasks, newest first.
function writeRunSections(dir, runs, baseSlug) {
  runs.forEach((run, index) => {
    const runDir = path.join(dir, slug(run.id));
    const runSlug = `${baseSlug}/${slug(run.id)}`;
    writeCategory(runDir, runTitle(run), index + 1, `Run ${run.data.evalId}.`, runSlug);
    fs.writeFileSync(path.join(runDir, 'scoreboard.mdx'), scoreboardPage(run.data));
    writeTaskPages(runDir, run, runSlug, 2);
  });
}

function writeFeatured(benchmarkDir, featured, runs) {
  fs.writeFileSync(path.join(benchmarkDir, 'intro.mdx'), introPage(runs));
  fs.writeFileSync(path.join(benchmarkDir, 'methodology.md'), methodologyPage());
  fs.writeFileSync(path.join(benchmarkDir, 'scoreboard.mdx'), scoreboardPage(featured.data, {
    title: 'Benchmark Scoreboard',
    position: 2,
    footer: ' Older full runs live under [past runs](history); partial runs under [subset runs](subset).',
  }));
  writeTaskPages(benchmarkDir, featured, '/benchmark', 3);
}

// One page per stored Game of Life run — the run directories share the
// regular results structure, so the task-page renderer applies as-is.
function writeGameOfLife(siteDocsDir, gameOfLifeResultsDir) {
  const runs = allRuns(gameOfLifeResultsDir);
  if (runs.length === 0) return;
  const dir = path.join(siteDocsDir, 'gameoflife');
  writeCategory(dir, 'Game of Life', 4, 'The standalone Game of Life showcase, one page per stored run.', '/gameoflife');
  runs.forEach((run, index) => {
    const rows = run.data.rows.filter((row) => row.task === 'gameoflife');
    if (rows.length === 0) return;
    fs.writeFileSync(
      path.join(dir, `${slug(run.id)}.mdx`),
      taskPage(run.dir, 'gameoflife', rows, { title: runTitle(run), position: index + 1 }),
    );
  });
}

// Generate the site content from every stored run. The newest run covering
// the full current task set is showcased; older full runs land in history/,
// partial runs in subset/. Returns the site docs dir; throws when no run
// carries a report.json (reprocess-results.js rebuilds old ones).
function buildSite(_evalIdIgnored, {
  resultsDir = RESULTS_DIR,
  siteDocsDir = SITE_DOCS_DIR,
  gameOfLifeResultsDir = GAME_OF_LIFE_RESULTS_DIR,
} = {}) {
  const runs = allRuns(resultsDir);
  if (runs.length === 0) throw new Error(`no run with a report.json under ${resultsDir}; run the benchmark or re-export one`);
  const canon = Object.keys(taskPrompts());
  const fullRuns = runs.filter((run) => isFullRun(run.data, canon));
  const featured = fullRuns[0] || runs[0];

  const benchmarkDir = path.join(siteDocsDir, 'benchmark');
  fs.rmSync(benchmarkDir, { recursive: true, force: true });
  fs.rmSync(path.join(siteDocsDir, 'gameoflife'), { recursive: true, force: true });
  // The category opens on the intro page (trend graphs + section buttons).
  writeCategory(benchmarkDir, 'Benchmark', 3, 'With vs without the ruleset, judged by habit-hooks and correctness gates.', { type: 'doc', id: 'benchmark/intro' });
  writeCategory(path.join(benchmarkDir, 'history'), 'Past runs', 90, 'Older full runs, newest first. All runs are re-judged with the current judges.', '/benchmark/history');
  writeCategory(path.join(benchmarkDir, 'subset'), 'Subset runs', 91, 'Runs covering only part of the current task set: iteration runs and runs from before newer tasks existed.', '/benchmark/subset');

  removeGeneratedLanding(siteDocsDir);
  writeReadmePages(siteDocsDir);
  writeRuleset(siteDocsDir);
  writeFeatured(benchmarkDir, featured, runs);
  writeRunSections(path.join(benchmarkDir, 'history'), fullRuns.filter((run) => run !== featured), '/benchmark/history');
  writeRunSections(path.join(benchmarkDir, 'subset'), runs.filter((run) => !fullRuns.includes(run)), '/benchmark/subset');
  writeGameOfLife(siteDocsDir, gameOfLifeResultsDir);
  return siteDocsDir;
}

const DOCS_OUTPUT_DIR = path.join(__dirname, '..', 'docs');

function htmlPages(dir) {
  const pages = [];
  (function walk(current) {
    for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
      if (entry.isDirectory()) walk(path.join(current, entry.name));
      else if (entry.name.endsWith('.html')) pages.push(path.join(current, entry.name));
    }
  })(dir);
  return pages;
}

// Every internal href on every rendered page must resolve to a rendered
// file. Returned (not thrown) so the test suite and renderSite share one
// checker: rendering fails loudly on dead links, and the committed docs/
// stays guarded by the suite.
function brokenSiteLinks(docsDir = DOCS_OUTPUT_DIR) {
  const { baseUrl } = require(path.join(WEBSITE_DIR, 'docusaurus.config.js'));
  const resolves = (href) => {
    const relative = href.slice(baseUrl.length).replace(/\/$/, '');
    if (relative === '') return true;
    return [path.join(docsDir, relative, 'index.html'), path.join(docsDir, relative), path.join(docsDir, `${relative}.html`)]
      .some((candidate) => fs.existsSync(candidate));
  };
  const broken = [];
  for (const page of htmlPages(docsDir)) {
    const html = fs.readFileSync(page, 'utf8');
    for (const match of html.matchAll(/href="([^"#]+)"/g)) {
      const href = match[1];
      if (/^(https?:|mailto:|data:)/.test(href) || !href.startsWith('/')) continue;
      if (!href.startsWith(baseUrl)) broken.push(`${href} escapes the base URL <- ${path.relative(docsDir, page)}`);
      else if (!resolves(href)) broken.push(`${href} <- ${path.relative(docsDir, page)}`);
    }
  }
  return broken;
}

// Render the static site into /docs and verify its links. Skipped (with a
// hint) when the website dependencies are not installed; a render with dead
// internal links throws, so no regeneration path can publish one silently.
function renderSite() {
  if (!fs.existsSync(path.join(WEBSITE_DIR, 'node_modules'))) {
    return { rendered: false, reason: 'website dependencies missing; run: npm --prefix website install' };
  }
  execFileSync('npm', ['--prefix', WEBSITE_DIR, 'run', 'build'], {
    stdio: ['ignore', 'pipe', 'pipe'],
    timeout: RENDER_TIMEOUT_MS,
    // A separate generated-files dir, so a concurrently running dev server
    // (which owns .docusaurus/) can never poison a production build.
    env: { ...process.env, DOCUSAURUS_GENERATED_FILES_DIR_NAME: '.docusaurus-build' },
  });
  const broken = brokenSiteLinks();
  if (broken.length > 0) {
    throw new Error(`site rendered with dead internal links:\n${broken.join('\n')}`);
  }
  return { rendered: true };
}

function main() {
  const siteDocsDir = buildSite(undefined);
  console.log(`site content generated in ${siteDocsDir}`);
  const render = renderSite();
  console.log(render.rendered ? 'static site rendered into docs/' : `render skipped: ${render.reason}`);
}

if (require.main === module) main();

module.exports = {
  buildSite, renderSite, brokenSiteLinks, newestRunId, mdxEscape, hitSmells,
  isFullRun, readmeSection,
};
