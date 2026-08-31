#!/usr/bin/env node
// Link-integrity crawl over the rendered site in docs/, reusing the same
// checker renderSite() runs after every regeneration — the suite guards the
// committed artifact, the render guards every regeneration path. Skips when
// docs/ has not been rendered (fresh clone without a site build).

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const { brokenSiteLinks } = require('../benchmarks/build-site.js');

const DOCS_DIR = path.join(__dirname, '..', 'docs');
const rendered = fs.existsSync(path.join(DOCS_DIR, 'index.html'));

test('every internal link on the rendered site resolves', { skip: !rendered && 'docs/ not rendered' }, () => {
  const broken = brokenSiteLinks(DOCS_DIR);
  assert.deepEqual(broken, [], `dead internal links in the rendered site:\n${broken.join('\n')}`);
});
