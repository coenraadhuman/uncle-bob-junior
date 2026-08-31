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
const REPO_URL = 'https://github.com/coenraadhuman/uncle-bob-junior';
// Outer fences use four backticks so model code containing ``` cannot close them.
const FENCE = '````';
const RENDER_TIMEOUT_MS = 300_000;

// Plain text dropped into MDX prose: neutralize the characters MDX would
// read as JSX or expressions.
function mdxEscape(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\{/g, '&#123;')
    .replace(/\}/g, '&#125;');
}

function runStamp(name) {
  return name.match(/\d{4}-\d{2}-\d{2}t[\d-]+/)?.[0] || '';
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

// The numbered checklist items from the ruleset's own SKILL.md — they are
// already markdown, which MDX renders natively.
function checklistItems(skillMarkdown) {
  const body = String(skillMarkdown).replace(/^---[\s\S]*?---\s*/, '');
  const section = body.split('## The checklist')[1]?.split(/\n## /)[0] || '';
  return section.split('\n').filter((line) => /^\d+\.\s/.test(line)).map((line) => line.replace(/^\d+\.\s*/, ''));
}

function landingPage() {
  const items = checklistItems(fs.readFileSync(SKILL_PATH, 'utf8'));
  return `---
title: Uncle Bob Junior
slug: /
sidebar_position: 1
---

A clean-code ruleset for Claude Code: software that is easy to read, simple to
understand, and safe to change. It ships as a Claude Code plugin — always-on
rules, slash commands, a mode statusline, and a habit-hooks verification stop
hook. See the [README](${REPO_URL}#readme) for installation.

## The checklist

${items.map((item, index) => `${index + 1}. ${item}`).join('\n')}

[Read the full ruleset and its reference files](ruleset/skill) — the slim
core above is what every session gets; the depth loads on demand.

[Install, update, or remove the plugin](plugin), browse the
[commands and intensity levels](commands), or check the [FAQ](faq).

## Does it work?

The repo benchmarks the ruleset against a no-ruleset baseline on the same models
and tasks using [promptfoo](https://github.com/promptfoo/promptfoo), judged by [habit-hooks](https://github.com/habit-hooks/habit-hooks),
an independent smell detector. [See the scoreboard and the generated code side
by side](benchmark/scoreboard), browse [past runs](benchmark/history) and
[subset runs](benchmark/subset), or watch the ruleset play
[Conway's Game of Life](gameoflife).
`;
}

// --- Pages embedded from the README ------------------------------------------

// One `## Heading` section of the README, verbatim (its ### subsections
// included). Throws when the heading is gone so a README restructure breaks
// the site build loudly instead of silently dropping a page.
function readmeSection(heading) {
  const readme = fs.readFileSync(README_PATH, 'utf8');
  const section = readme.split(/\n(?=## )/).find((part) => part.startsWith(`## ${heading}`));
  if (!section) throw new Error(`README.md has no "## ${heading}" section for the site to embed`);
  return section.replace(`## ${heading}`, '').trim();
}

// Repo-relative links work on GitHub, not on the site — point them at the repo.
function repoLinks(markdown) {
  return markdown.replace(/\]\((?!https?:|#|\/)([^)]+)\)/g, `](${REPO_URL}/blob/main/$1)`);
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
    const task = linkTasks ? `[${mdxEscape(row.task)}](${slug(row.task)})` : mdxEscape(row.task);
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

function scoreboardPage(data) {
  return `---
title: Benchmark Scoreboard
sidebar_position: 1
---

${runSummary(data)}

## Mean score per model and arm

${meanChart(data.means)}

## Per task

${scoreboardTable(data.rows)}

Older full runs live under [past runs](history); partial runs under
[subset runs](subset).
`;
}

// One compact page per non-featured run: chart plus scoreboard, no code pages.
function runPage(run, position) {
  return `---
title: ${run.data.evalId}
sidebar_position: ${position}
---

${runSummary(run.data)}

${meanChart(run.data.means)}

${scoreboardTable(run.data.rows, { linkTasks: false })}
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

function taskPage(runDir, task, allRows, { title = task, position = null } = {}) {
  const rows = lastRowPerArm(allRows);
  const models = [...new Set(rows.map((row) => row.model))];
  const sections = models.map((model) => {
    const arms = rows
      .filter((row) => row.model === model)
      .sort((a, b) => Number(b.arm.includes('baseline')) - Number(a.arm.includes('baseline')));
    const tabs = arms.map((row) => armTab(runDir, row, slug(row.arm))).join('\n\n');
    return `## ${mdxEscape(model)}\n\n<Tabs>\n\n${tabs}\n\n</Tabs>`;
  }).join('\n\n');
  return `---
title: ${title}
${position === null ? '' : `sidebar_position: ${position}\n`}---

${TABS_IMPORT}

> ${mdxEscape(rows[0].prompt || '')}

${sections}
`;
}

// --- Assembly ----------------------------------------------------------------

// slug pins the generated index's URL so in-page links can target it; without
// one Docusaurus defaults to /category/<label>, which nothing links to.
function writeCategory(dir, label, position, description, categorySlug) {
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, '_category_.json'), JSON.stringify({
    label,
    position,
    link: { type: 'generated-index', description, slug: categorySlug },
  }, null, 2) + '\n');
}

function writeRunPages(dir, runs) {
  runs.forEach((run, index) => {
    fs.writeFileSync(path.join(dir, `${slug(run.id)}.mdx`), runPage(run, index + 2));
  });
}

function writeFeatured(benchmarkDir, featured) {
  fs.writeFileSync(path.join(benchmarkDir, 'scoreboard.mdx'), scoreboardPage(featured.data));
  for (const task of new Set(featured.data.rows.map((row) => row.task))) {
    const rows = featured.data.rows.filter((row) => row.task === task);
    fs.writeFileSync(path.join(benchmarkDir, `${slug(task)}.mdx`), taskPage(featured.dir, task, rows));
  }
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
      taskPage(run.dir, 'gameoflife', rows, { title: run.data.evalId, position: index + 1 }),
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
  writeCategory(benchmarkDir, 'Benchmark', 3, 'With vs without the ruleset, judged by habit-hooks and correctness gates.', '/benchmark');
  writeCategory(path.join(benchmarkDir, 'history'), 'Past runs', 90, 'Older full runs, newest first. All runs are re-judged with the current judges.', '/benchmark/history');
  writeCategory(path.join(benchmarkDir, 'subset'), 'Subset runs', 91, 'Runs covering only part of the current task set: iteration runs and runs from before newer tasks existed.', '/benchmark/subset');

  fs.writeFileSync(path.join(siteDocsDir, 'index.mdx'), landingPage());
  writeReadmePages(siteDocsDir);
  writeRuleset(siteDocsDir);
  writeFeatured(benchmarkDir, featured);
  writeRunPages(path.join(benchmarkDir, 'history'), fullRuns.filter((run) => run !== featured));
  writeRunPages(path.join(benchmarkDir, 'subset'), runs.filter((run) => !fullRuns.includes(run)));
  writeGameOfLife(siteDocsDir, gameOfLifeResultsDir);
  return siteDocsDir;
}

// Render the static site into /docs. Skipped (with a hint) when the website
// dependencies are not installed; never throws further than the caller's try.
function renderSite() {
  if (!fs.existsSync(path.join(WEBSITE_DIR, 'node_modules'))) {
    return { rendered: false, reason: 'website dependencies missing; run: npm --prefix website install' };
  }
  execFileSync('npm', ['--prefix', WEBSITE_DIR, 'run', 'build'], {
    stdio: ['ignore', 'pipe', 'pipe'],
    timeout: RENDER_TIMEOUT_MS,
  });
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
  buildSite, renderSite, newestRunId, checklistItems, mdxEscape, hitSmells,
  isFullRun, readmeSection,
};
