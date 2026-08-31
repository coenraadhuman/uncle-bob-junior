#!/usr/bin/env node
// Full site refresh in one command (also available as `npm run site:update`):
//
//   node scripts/update-site.js
//
// 1. Re-judges every stored run from its raw replies — fresh extraction,
//    habit-hooks scans, gates, and scores (benchmarks/results/ and
//    benchmarks/game-of-life-results/ alike; runs without replies are removed).
// 2. Regenerates the site content from the repository's current state
//    (ruleset, README pages, scoreboards, task pages, Game of Life).
// 3. Renders the static site into docs/, ready to commit.
//
// Every eval already does steps 2 and 3 automatically; this script is the
// manual full update, including the re-judging pass.
const { reprocessAll } = require('../benchmarks/reprocess-results');
const { buildSite, renderSite } = require('../benchmarks/build-site');

function run({ reprocess = reprocessAll, build = buildSite, render = renderSite, log = console.log } = {}) {
  log('1/3 re-judging stored runs from their replies (extraction, habit-hooks, gates, scores)...');
  reprocess();
  log('2/3 regenerating site content from the repository state...');
  build(undefined);
  log('3/3 rendering the static site into docs/...');
  const result = render();
  log(result.rendered ? 'done: docs/ is up to date' : `render skipped: ${result.reason}`);
  return result;
}

if (require.main === module) run();

module.exports = { run };
