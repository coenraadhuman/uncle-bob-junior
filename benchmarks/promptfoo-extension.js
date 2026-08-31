// Promptfoo extension hook: after every eval, export the run's outcomes to
// benchmarks/results/<eval-id>/ automatically — report.md, report.json, the
// generated sources, and the full habit-hooks report per answer — and rebuild
// the docs/ showcase site from that run, so the site always reflects the
// latest eval. Registered in promptfooconfig.yaml under `extensions`.
const { exportRun } = require('./export-results');
const site = require('./build-site');

async function extensionHook(hookName, context, { doExport = exportRun, doBuildSite = site.buildSite } = {}) {
  if (hookName !== 'afterAll') return;
  let runDir = null;
  try {
    runDir = doExport(context.evalId, context.results);
    if (runDir) console.log(`\nRun outcomes exported to ${runDir}`);
  } catch (error) {
    // A failed export must never fail the eval itself; the data stays
    // recoverable via `node benchmarks/export-results.js <eval-id>`.
    console.error(`results export failed: ${error.message}`);
  }
  if (!runDir) return;
  try {
    console.log(`Site content regenerated in ${doBuildSite(context.evalId)}; publish with: npm --prefix website run build`);
  } catch (error) {
    // Same rule for the site: recoverable via `node benchmarks/build-site.js <eval-id>`.
    console.error(`site content generation failed: ${error.message}`);
  }
}

module.exports = { extensionHook };
