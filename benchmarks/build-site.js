#!/usr/bin/env node
// Generates the showcase site's content from one benchmark run:
//
//   node benchmarks/build-site.js [eval-id]   # defaults to the newest run
//
// The site itself is Docusaurus (website/) — libraries over wheels — and this
// script only writes its MDX content:
//   website/docs/index.mdx                  landing: the ruleset checklist
//   website/docs/benchmark/scoreboard.mdx   mean chart + smells-with-hits table
//   website/docs/benchmark/<task>.mdx       baseline vs ruleset code in tabs
//
// Publish with `npm --prefix website run build`, which renders static HTML
// into the repo's /docs for GitHub Pages. The run directory must carry a
// report.json (written by export-results.js); older runs can be re-exported
// with `node benchmarks/export-results.js <id>`.
const fs = require('fs');
const path = require('path');

const { parseIssues } = require('./habit-hooks-assert');
const { slug } = require('./export-results');

const RESULTS_DIR = path.join(__dirname, 'results');
const SITE_DOCS_DIR = path.join(__dirname, '..', 'website', 'docs');
const SKILL_PATH = path.join(__dirname, '..', 'plugins', 'uncle-bob-junior', 'skills', 'uncle-bob-junior', 'SKILL.md');
const REPO_URL = 'https://github.com/coenraadhuman/uncle-bob-junior';
// Outer fences use four backticks so model code containing ``` cannot close them.
const FENCE = '````';

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

// The newest run that has a report.json, by the timestamp in the directory
// name (eval-<key>-<timestamp>); the key part varies, the timestamp sorts.
function newestRunId(resultsDir) {
  const stamped = fs.readdirSync(resultsDir)
    .filter((name) => fs.existsSync(path.join(resultsDir, name, 'report.json')))
    .map((name) => ({ name, stamp: name.match(/\d{4}-\d{2}-\d{2}t[\d-]+/)?.[0] || '' }))
    .sort((a, b) => a.stamp.localeCompare(b.stamp));
  return stamped.at(-1)?.name || null;
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
rules, slash commands, a mode statusline, and a habit-hooks verification Stop
hook. See the [README](${REPO_URL}#readme) for installation.

## The checklist

${items.map((item, index) => `${index + 1}. ${item}`).join('\n')}

## Does it work?

The repo benchmarks the ruleset against a no-ruleset baseline on the same models
and tasks, judged by [habit-hooks](https://github.com/habit-hooks/habit-hooks),
an independent smell detector. [See the scoreboard and the generated code side
by side.](benchmark/scoreboard)
`;
}

function gateCells(row) {
  const mark = (pass) => (pass ? 'yes' : '**NO**');
  const habit = row.habitPass === null ? 'n/a' : row.habitPass ? 'pass' : '**FAIL**';
  return `${mark(row.gates.validCode !== false)} | ${habit} | ${mark(row.gates.shipsTests)} | ${mark(row.gates.correct)}`;
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

function scoreboardTable(rows) {
  const smells = hitSmells(rows);
  const header = `| task | arm | score | valid code | habit-hooks | ships tests | correct | ${smells.join(' | ')} |`;
  const divider = `|---|---|---:|:---:|:---:|:---:|:---:|${smells.map(() => '---:').join('|')}|`;
  const body = rows.map((row) =>
    `| [${mdxEscape(row.task)}](${slug(row.task)}) | ${mdxEscape(`${row.model} · ${row.arm}`)} | ${row.score.toFixed(2)} | ${gateCells(row)} | ` +
    smells.map((rule) => row.smellCounts[rule] ?? 'n/a').join(' | ') + ' |');
  return [header, divider, ...body].join('\n');
}

function scoreboardPage(data) {
  return `---
title: Benchmark scoreboard
sidebar_position: 1
---

Run \`${data.evalId}\`: the same tasks and models, once bare (baseline) and once
with the uncle-bob-junior ruleset as system prompt, judged by
[habit-hooks](https://github.com/habit-hooks/habit-hooks) plus valid-code,
ships-tests, and correctness gates. Single-shot generations, so expect
run-to-run variance.

## Mean score per model and arm

${meanChart(data.means)}

## Per task

${scoreboardTable(data.rows)}
`;
}

// Findings for one arm keyed by file name: { 'Foo.java': [{rule, line}] }.
function findingsByFile(runDir, row) {
  const reportPath = path.join(runDir, 'habit-hooks', `${slug(row.task)}-${slug(row.model)}-${slug(row.arm)}.md`);
  if (!fs.existsSync(reportPath)) return {};
  const byFile = {};
  for (const issue of parseIssues(fs.readFileSync(reportPath, 'utf8'))) {
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
  return fs.readdirSync(dir).sort().map((name) => ({ name, content: fs.readFileSync(path.join(dir, name), 'utf8') }));
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

function armTab(runDir, row, value) {
  const findings = findingsByFile(runDir, row);
  const main = sourceFiles(runDir, row, 'main');
  const tests = sourceFiles(runDir, row, 'test');
  const mainSections = main.map((file, index) => fileSection(file, findings[file.name] || [], index === 0)).join('\n\n');
  const testSections = tests.length
    ? `<details>\n<summary>Shipped tests (${tests.length} file${tests.length > 1 ? 's' : ''})</summary>\n\n${tests.map((file) => fileSection(file, [], false)).join('\n\n')}\n\n</details>`
    : '*No tests shipped.*';
  return `<TabItem value="${value}" label="${mdxEscape(row.arm)} · ${row.score.toFixed(2)}">

Gates: valid code ${row.gates.validCode !== false ? 'yes' : '**NO**'} ·
habit-hooks ${row.habitPass === null ? 'n/a' : row.habitPass ? 'pass' : '**FAIL**'} ·
ships tests ${row.gates.shipsTests ? 'yes' : '**NO**'} ·
correct ${row.gates.correct ? 'yes' : '**NO**'}

${mainSections || '*No production code extracted.*'}

${testSections}

</TabItem>`;
}

// A --repeat run carries several rows per model × arm; the run directory
// keeps only the last repetition's files, so the tabs show that one.
function lastRowPerArm(rows) {
  const byKey = new Map();
  for (const row of rows) byKey.set(`${row.model}|${row.arm}`, row);
  return [...byKey.values()];
}

function taskPage(runDir, task, allRows) {
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
title: ${task}
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

> ${mdxEscape(rows[0].prompt || '')}

${sections}
`;
}

// Generate the site content for one run into website/docs. Returns that dir;
// throws when the run (or its report.json) is missing so callers can point at
// export-results.js. Rendering to static HTML is Docusaurus's job:
// `npm --prefix website run build`.
function buildSite(evalId, { resultsDir = RESULTS_DIR, siteDocsDir = SITE_DOCS_DIR } = {}) {
  const runId = evalId ? slug(evalId) : newestRunId(resultsDir);
  if (!runId) throw new Error(`no run with a report.json under ${resultsDir}; run the benchmark or re-export one`);
  const runDir = path.join(resultsDir, runId);
  const dataPath = path.join(runDir, 'report.json');
  if (!fs.existsSync(dataPath)) throw new Error(`${dataPath} missing; re-export with: node benchmarks/export-results.js ${evalId}`);
  const data = JSON.parse(fs.readFileSync(dataPath, 'utf8'));

  // benchmark/ is fully derived from the run: wipe it so task pages from an
  // earlier run never linger. Hand-written docs next to index.mdx stay.
  const benchmarkDir = path.join(siteDocsDir, 'benchmark');
  fs.rmSync(benchmarkDir, { recursive: true, force: true });
  fs.mkdirSync(benchmarkDir, { recursive: true });
  fs.writeFileSync(path.join(siteDocsDir, 'index.mdx'), landingPage());
  fs.writeFileSync(path.join(benchmarkDir, 'scoreboard.mdx'), scoreboardPage(data));
  for (const task of new Set(data.rows.map((row) => row.task))) {
    const rows = data.rows.filter((row) => row.task === task);
    fs.writeFileSync(path.join(benchmarkDir, `${slug(task)}.mdx`), taskPage(runDir, task, rows));
  }
  return siteDocsDir;
}

function main() {
  const siteDocsDir = buildSite(process.argv[2]);
  console.log(`site content generated in ${siteDocsDir}; render with: npm --prefix website run build`);
}

if (require.main === module) main();

module.exports = { buildSite, newestRunId, checklistItems, mdxEscape, hitSmells };
