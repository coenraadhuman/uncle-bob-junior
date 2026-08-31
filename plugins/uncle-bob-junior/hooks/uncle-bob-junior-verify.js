#!/usr/bin/env node
// uncle-bob-junior — Stop hook: the trust-but-verify half of the ruleset.
// When the agent is about to finish a turn, run habit-hooks over the branch's
// changed files and hand any findings back as a block reason, so smells get
// fixed instead of shipped. The prompt asks; this hook checks.
//
// Deliberately quiet: it allows the stop (exit 0, no output) whenever it
// cannot or should not judge — uncle-bob-junior off or in review mode, no
// habit-hooks CLI on PATH, no .habit-hooks/ config in the project (the check
// is opt-in per repo), a block already issued this same stop
// (stop_hook_active), or any scan error. A verification hook must never break
// the session it verifies. Recurring findings the user accepts are managed
// with habit-hooks' own snooze, which this hook honours by not passing
// --no-snooze.
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const { readMode } = require('./uncle-bob-junior-runtime');

const SCAN_TIMEOUT_MS = 30_000;
// Stop-hook reasons re-enter the model's context; cap so a huge report can't flood it.
const MAX_REASON_LENGTH = 4000;
const VERIFYING_MODES = ['lite', 'full', 'ultra'];
// The tool calls that mean a coding job took place this turn.
const EDIT_TOOLS = new Set(['Edit', 'Write', 'MultiEdit', 'NotebookEdit']);

function stdinJson() {
  try {
    return JSON.parse(fs.readFileSync(0, 'utf8') || '{}');
  } catch (error) {
    return {};
  }
}

// A real user prompt (not a tool result fed back) starts a new turn.
function isUserPrompt(entry) {
  if (entry.type !== 'user' || !entry.message) return false;
  const content = entry.message.content;
  if (typeof content === 'string') return true;
  return Array.isArray(content) && content.every((block) => block.type !== 'tool_result');
}

// Whether the turn since the last user prompt used a file-editing tool.
// Verification is pointless after a chat-only turn — and nagging about
// pre-existing branch smells then would be noise. Unreadable or absent
// transcripts count as edited: when we cannot tell, keep verifying.
function turnEditedFiles(transcriptPath) {
  let lines;
  try {
    lines = fs.readFileSync(transcriptPath, 'utf8').split('\n');
  } catch (error) {
    return true;
  }
  let edited = false;
  for (const line of lines) {
    if (!line.trim()) continue;
    let entry;
    try {
      entry = JSON.parse(line);
    } catch (error) {
      continue;
    }
    if (isUserPrompt(entry)) edited = false;
    else if (Array.isArray(entry.message?.content)
      && entry.message.content.some((block) => block.type === 'tool_use' && EDIT_TOOLS.has(block.name))) {
      edited = true;
    }
  }
  return edited;
}

function shouldSkip(input, cwd) {
  if (input.stop_hook_active) return true;
  if (!VERIFYING_MODES.includes(readMode() || '')) return true;
  if (!fs.existsSync(path.join(cwd, '.habit-hooks', 'config.toml'))) return true;
  return !turnEditedFiles(input.transcript_path);
}

// Exit 0 = clean, exit 1 = findings on stdout; anything else (no CLI, no git,
// timeout) is "not judgeable" and must not block the stop.
function scanBranchChanges(cwd) {
  try {
    execFileSync('habit-hooks', ['--branch'], {
      cwd,
      encoding: 'utf8',
      timeout: SCAN_TIMEOUT_MS,
      stdio: ['ignore', 'pipe', 'pipe'],
    });
    return { clean: true };
  } catch (error) {
    if (error.status === 1 && typeof error.stdout === 'string') {
      return { clean: false, report: error.stdout };
    }
    return { clean: true };
  }
}

function blockReason(report) {
  return 'UNCLE_BOB_JUNIOR VERIFICATION — habit-hooks found code smells in this branch\'s changes. ' +
    'The final gate is not passed: fix the findings below (or ask the user to snooze a deliberate deviation), then finish.\n\n' +
    report.trim().slice(0, MAX_REASON_LENGTH);
}

function main() {
  const input = stdinJson();
  const cwd = input.cwd || process.cwd();
  if (shouldSkip(input, cwd)) return;
  const result = scanBranchChanges(cwd);
  if (result.clean) return;
  process.stdout.write(JSON.stringify({ decision: 'block', reason: blockReason(result.report) }));
}

main();
