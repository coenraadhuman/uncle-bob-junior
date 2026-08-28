#!/usr/bin/env node
// Exporter for the standalone Game of Life showcase
// (promptfooconfig.gameoflife.yaml): stores each full reply as
// examples/<model>/<arm>/reply.md in the repo root — nothing else, no
// scoreboard, no habit-hooks reports. Registered as that config's extension hook.
//
//   node benchmarks/gameoflife-examples.js [evalId]   # re-export, defaults to the newest eval
const fs = require('fs');
const path = require('path');

const { resultRows, slug, loadEval, newestEvalId } = require('./export-results');

const EXAMPLES_DIR = path.join(__dirname, '..', 'examples');
// Matches the test description in promptfooconfig.gameoflife.yaml. Rows from
// other tasks are ignored, so re-exporting the wrong eval is a harmless no-op
// instead of overwriting the examples with unrelated replies.
const GAME_OF_LIFE_TASK = 'gameoflife';

// Writes one examples/<model>/<arm>/reply.md per Game of Life reply and
// returns the written paths — the arm level keeps a model's baseline and
// ruleset replies from overwriting each other. `data` is a promptfoo export
// JSON or a bare results array.
function exportExamples(data, { examplesDir = EXAMPLES_DIR } = {}) {
  const rows = resultRows(data).filter(
    (row) => row.task === GAME_OF_LIFE_TASK && row.output.trim() !== '',
  );
  return rows.map((row) => {
    const armDir = path.join(examplesDir, slug(row.model), slug(row.arm));
    fs.mkdirSync(armDir, { recursive: true });
    const file = path.join(armDir, 'reply.md');
    fs.writeFileSync(file, row.output);
    return file;
  });
}

async function extensionHook(hookName, context) {
  if (hookName !== 'afterAll') return;
  try {
    const written = exportExamples(context.results);
    if (written.length) console.log(`\nExample replies written to ${EXAMPLES_DIR}`);
  } catch (error) {
    // A failed export must never fail the eval itself; the data stays
    // recoverable via `node benchmarks/gameoflife-examples.js <eval-id>`.
    console.error(`examples export failed: ${error.message}`);
  }
}

function main() {
  const evalId = process.argv[2] || newestEvalId();
  const written = exportExamples(loadEval(evalId));
  if (written.length === 0) {
    console.error(`Eval ${evalId} has no ${GAME_OF_LIFE_TASK} replies to export.`);
    process.exit(1);
  }
  console.log(`wrote:\n${written.join('\n')}`);
}

if (require.main === module) main();

module.exports = { exportExamples, extensionHook, EXAMPLES_DIR, GAME_OF_LIFE_TASK };
