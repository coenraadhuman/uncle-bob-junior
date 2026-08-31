#!/usr/bin/env node
// Exporter for the standalone Game of Life showcase
// (promptfooconfig.gameoflife.yaml): stores each run as a full run directory
// under benchmarks/game-of-life-results/<eval-id>/ — the same structure as
// benchmarks/results/ (report.json, report.md, src/, habit-hooks/) — so the
// site and reprocess-results.js treat it like any other stored run.
//
//   node benchmarks/gameoflife-examples.js [evalId]   # re-export, defaults to the newest eval
const path = require('path');

const { resultRows, slug, writeRunArtifacts, loadEval, newestEvalId } = require('./export-results');
const site = require('./build-site');

const GAME_OF_LIFE_RESULTS_DIR = path.join(__dirname, 'game-of-life-results');
// Matches the test description in promptfooconfig.gameoflife.yaml. Rows from
// other tasks are ignored, so re-exporting the wrong eval is a harmless no-op
// instead of writing unrelated replies into the showcase.
const GAME_OF_LIFE_TASK = 'gameoflife';

// Export one Game of Life eval as a run directory. Returns the run dir, or
// null when the eval carries no Game of Life replies. `data` is a promptfoo
// export JSON or a bare results array.
function exportExamples(evalId, data, { resultsDir = GAME_OF_LIFE_RESULTS_DIR } = {}) {
  const rows = resultRows(data).filter(
    (row) => row.task === GAME_OF_LIFE_TASK && row.output.trim() !== '',
  );
  if (rows.length === 0) return null;
  const runDir = path.join(resultsDir, slug(evalId));
  writeRunArtifacts(evalId, rows, runDir);
  return runDir;
}

async function extensionHook(hookName, context, {
  doExport = exportExamples,
  doBuildSite = site.buildSite,
  doRenderSite = site.renderSite,
} = {}) {
  if (hookName !== 'afterAll') return;
  let runDir = null;
  try {
    runDir = doExport(context.evalId, context.results);
    if (runDir) console.log(`\nGame of Life run exported to ${runDir}`);
  } catch (error) {
    // A failed export must never fail the eval itself; the data stays
    // recoverable via `node benchmarks/gameoflife-examples.js <eval-id>`.
    console.error(`game of life export failed: ${error.message}`);
  }
  if (!runDir) return;
  try {
    console.log(`Site content regenerated in ${doBuildSite(context.evalId)}`);
    const render = doRenderSite();
    console.log(render.rendered ? 'Static site rendered into docs/' : `Site render skipped: ${render.reason}`);
  } catch (error) {
    console.error(`site build failed: ${error.message}`);
  }
}

function main() {
  const evalId = process.argv[2] || newestEvalId();
  const runDir = exportExamples(evalId, loadEval(evalId));
  if (!runDir) {
    console.error(`Eval ${evalId} has no ${GAME_OF_LIFE_TASK} replies to export.`);
    process.exit(1);
  }
  console.log(`wrote ${runDir}`);
}

if (require.main === module) main();

module.exports = { exportExamples, extensionHook, GAME_OF_LIFE_RESULTS_DIR, GAME_OF_LIFE_TASK };
