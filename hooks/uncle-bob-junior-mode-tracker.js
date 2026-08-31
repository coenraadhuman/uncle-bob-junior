#!/usr/bin/env node
// uncle-bob-junior — UserPromptSubmit hook to track which uncle-bob-junior mode is active
// Inspects user input for /uncle-bob-junior commands and writes mode to flag file

const { getDefaultMode, isDeactivationCommand, writeDefaultMode } = require('./uncle-bob-junior-config');
const { clearMode, readMode, setMode, writeHookOutput } = require('./uncle-bob-junior-runtime');

let input = '';
let done = false;

function finish() {
  if (done) return;
  done = true;
  try {
    // Strip UTF-8 BOM some shells prepend when piping (breaks JSON.parse)
    const data = JSON.parse(input.replace(/^\uFEFF/, ''));
    const prompt = (data.prompt || '').trim().toLowerCase();

    // Match /uncle-bob-junior commands
    let modeSwitched = false;
    let deactivated = false;
    if (/^[/@$]uncle-bob-junior/.test(prompt)) {
      const parts = prompt.split(/\s+/);
      const cmd = parts[0].replace(/^[@$]/, '/');
      const arg = parts[1] || '';

      let mode = null;
      let isReportOnly = false;

      if (cmd === '/uncle-bob-junior-review' || cmd === '/uncle-bob-junior:uncle-bob-junior-review') {
        mode = 'review';
      } else if (cmd === '/uncle-bob-junior' || cmd === '/uncle-bob-junior:uncle-bob-junior') {
        // `/uncle-bob-junior default <mode>` persists the default to config (survives
        // restarts). Plain switches stay session-scoped ("sticks until session
        // end"), so this is the only path that writes config. review is not a
        // valid default (#377), so only off/lite/full/ultra are accepted.
        if (arg === 'default') {
          const dmode = parts[2];
          if (dmode === 'off' || dmode === 'lite' || dmode === 'full' || dmode === 'ultra') {
            writeDefaultMode(dmode);
            writeHookOutput('UserPromptSubmit', dmode, 'UNCLE_BOB_JUNIOR DEFAULT SET — new sessions start in ' + dmode + '.');
          }
          return; // don't fall through to the session-mode switch
        }
        if (arg === 'lite') mode = 'lite';
        else if (arg === 'full') mode = 'full';
        else if (arg === 'ultra') mode = 'ultra';
        else if (arg === 'off') mode = 'off';
        else if (arg === '') {
          isReportOnly = true;
          mode = readMode() || getDefaultMode();
        } else {
          mode = getDefaultMode();
        }
      }

      if (isReportOnly) {
        writeHookOutput(
          'UserPromptSubmit',
          mode,
          'UNCLE_BOB_JUNIOR MODE ACTIVE — level: ' + mode,
        );
      } else if (mode && mode !== 'off') {
        setMode(mode);
        modeSwitched = true;
        writeHookOutput(
          'UserPromptSubmit',
          mode,
          'UNCLE_BOB_JUNIOR MODE CHANGED — level: ' + mode,
        );
      } else if (mode === 'off') {
        clearMode();
        deactivated = true;
        writeHookOutput('UserPromptSubmit', 'off', 'UNCLE_BOB_JUNIOR MODE OFF');
      }
    }

    // Detect deactivation
    if (!modeSwitched && !deactivated && isDeactivationCommand(prompt)) {
      clearMode();
      deactivated = true;
      writeHookOutput('UserPromptSubmit', 'off', 'UNCLE_BOB_JUNIOR MODE OFF');
    }

  } catch (e) {
    // Silent fail
  }
}

process.stdin.on('data', chunk => { input += chunk; });
process.stdin.on('end', finish);

// Never hang the session. On Windows, Claude Code runs this hook through a
// PowerShell `if {}` wrapper that can swallow the piped prompt JSON, so stdin
// 'end' never fires and the hook blocks forever — freezing the session (#443).
// On error, or after a short fallback, process whatever arrived (recovering the
// mode if data came without EOF) and exit. unref() keeps the timer from adding
// latency to the normal path, where 'end' fires first. Mirrors the best-effort,
// never-block contract the other lifecycle hooks already follow.
process.stdin.on('error', () => { finish(); process.exit(0); });
setTimeout(() => { finish(); process.exit(0); }, 1000).unref();
