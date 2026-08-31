#!/usr/bin/env node
// Link-integrity crawl over the rendered site in docs/: every internal href
// on every page must resolve to a rendered file. Docusaurus's own
// onBrokenLinks check guards a build; this guards the committed artifact —
// a stale or hand-touched docs/ with dead links fails the suite. Skips when
// docs/ has not been rendered (fresh clone without a site build).

const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const DOCS_DIR = path.join(__dirname, '..', 'docs');
const BASE_URL = '/uncle-bob-junior/';

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

function resolves(href) {
  const relative = href.slice(BASE_URL.length).replace(/\/$/, '');
  if (relative === '') return true;
  return [
    path.join(DOCS_DIR, relative, 'index.html'),
    path.join(DOCS_DIR, relative),
    path.join(DOCS_DIR, `${relative}.html`),
  ].some((candidate) => fs.existsSync(candidate));
}

const rendered = fs.existsSync(path.join(DOCS_DIR, 'index.html'));

test('every internal link on the rendered site resolves', { skip: !rendered && 'docs/ not rendered' }, () => {
  const broken = [];
  for (const page of htmlPages(DOCS_DIR)) {
    const html = fs.readFileSync(page, 'utf8');
    for (const match of html.matchAll(/href="([^"#]+)"/g)) {
      const href = match[1];
      if (/^(https?:|mailto:|data:)/.test(href) || !href.startsWith('/')) continue;
      if (!href.startsWith(BASE_URL)) {
        broken.push(`${href} escapes the base URL <- ${path.relative(DOCS_DIR, page)}`);
      } else if (!resolves(href)) {
        broken.push(`${href} <- ${path.relative(DOCS_DIR, page)}`);
      }
    }
  }
  assert.deepEqual(broken, [], `dead internal links in the rendered site:\n${broken.join('\n')}`);
});
