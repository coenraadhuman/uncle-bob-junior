#!/usr/bin/env node
// Version bumping keyed to the plugin's actual content, so Claude Code's
// `/plugin update` flow (which compares plugin.json versions) always sees a
// new version when the skills, hooks, commands, or scripts change:
//
//   node scripts/bump-version.js            # bump patch if the payload changed
//   node scripts/bump-version.js --minor    # bump minor instead
//   node scripts/bump-version.js --major    # bump major instead
//   node scripts/bump-version.js --check    # exit 1 if a bump is needed
//
// The payload hash covers every file under plugins/uncle-bob-junior/ except
// .claude-plugin/ (which holds the version and the recorded hash themselves).
// The recorded hash lives in .claude-plugin/content.sha256; check-versions.js
// and the test suite fail when it no longer matches, and the committable
// pre-commit hook (.githooks/pre-commit) runs the bump automatically.
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const PLUGIN_DIR = path.join(__dirname, '..', 'plugins', 'uncle-bob-junior');
const VERSION_FILES = [
  path.join(PLUGIN_DIR, '.claude-plugin', 'plugin.json'),
  path.join(__dirname, '..', 'package.json'),
];
const MANIFEST_DIR_NAME = '.claude-plugin';
const HASH_FILE = 'content.sha256';
const LEVELS = ['major', 'minor', 'patch'];

function payloadFiles(pluginDir) {
  const files = [];
  const walk = (dir) => fs.readdirSync(dir, { withFileTypes: true }).forEach((entry) => {
    if (entry.name === MANIFEST_DIR_NAME) return;
    const absolute = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(absolute);
    else files.push(absolute);
  });
  walk(pluginDir);
  return files.sort();
}

function payloadHash(pluginDir) {
  const hash = crypto.createHash('sha256');
  for (const file of payloadFiles(pluginDir)) {
    hash.update(path.relative(pluginDir, file).split(path.sep).join('/'));
    hash.update('\0');
    hash.update(fs.readFileSync(file));
    hash.update('\0');
  }
  return hash.digest('hex');
}

function hashPath(pluginDir) {
  return path.join(pluginDir, MANIFEST_DIR_NAME, HASH_FILE);
}

function recordedHash(pluginDir) {
  try {
    return fs.readFileSync(hashPath(pluginDir), 'utf8').trim();
  } catch (error) {
    return null;
  }
}

// Whether the recorded hash still matches the payload.
function status(pluginDir = PLUGIN_DIR) {
  const current = payloadHash(pluginDir);
  const recorded = recordedHash(pluginDir);
  return { current, recorded, upToDate: current === recorded };
}

function bumpedVersion(version, level) {
  const [major, minor, patch] = version.split('.').map(Number);
  if (level === 'major') return `${major + 1}.0.0`;
  if (level === 'minor') return `${major}.${minor + 1}.0`;
  return `${major}.${minor}.${patch + 1}`;
}

function rewriteVersion(filePath, toVersion) {
  const manifest = JSON.parse(fs.readFileSync(filePath, 'utf8').replace(/^﻿/, ''));
  manifest.version = toVersion;
  fs.writeFileSync(filePath, JSON.stringify(manifest, null, 2) + '\n');
}

// Bump every version file and record the payload hash. Returns {from, to},
// or null when the payload has not changed since the recorded hash.
function bump(level = 'patch', { pluginDir = PLUGIN_DIR, versionFiles = VERSION_FILES } = {}) {
  const { current, upToDate } = status(pluginDir);
  if (upToDate) return null;
  const from = JSON.parse(fs.readFileSync(versionFiles[0], 'utf8')).version;
  const to = bumpedVersion(from, level);
  for (const file of versionFiles) rewriteVersion(file, to);
  fs.writeFileSync(hashPath(pluginDir), current + '\n');
  return { from, to };
}

function main() {
  const args = process.argv.slice(2);
  if (args.includes('--check')) {
    if (status().upToDate) {
      console.log('plugin payload unchanged since the last version bump');
      return;
    }
    console.error('plugin content changed without a version bump; run: node scripts/bump-version.js');
    process.exit(1);
  }
  const level = LEVELS.find((name) => args.includes(`--${name}`)) || 'patch';
  const result = bump(level);
  console.log(result
    ? `version bumped ${result.from} -> ${result.to} (payload hash recorded)`
    : 'plugin payload unchanged; version stays');
}

if (require.main === module) main();

module.exports = { payloadHash, status, bump, bumpedVersion };
