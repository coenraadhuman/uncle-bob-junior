// Promptfoo extension hook: after every eval, export the run's outcomes to
// benchmarks/results/<eval-id>/ automatically — report.md, the generated
// sources, and the full habit-hooks report per answer. Registered in
// promptfooconfig.yaml under `extensions`.
const { exportRun } = require('./export-results');

async function extensionHook(hookName, context) {
  if (hookName !== 'afterAll') return;
  try {
    const runDir = exportRun(context.evalId, context.results);
    if (runDir) console.log(`\nRun outcomes exported to ${runDir}`);
  } catch (error) {
    // A failed export must never fail the eval itself; the data stays
    // recoverable via `node benchmarks/export-results.js <eval-id>`.
    console.error(`results export failed: ${error.message}`);
  }
}

module.exports = { extensionHook };
