// Promptfoo provider that drives the authenticated Claude Code CLI instead of
// the Anthropic API, so the benchmark needs no ANTHROPIC_API_KEY — just a
// logged-in `claude` on PATH.
//
// `--safe-mode` isolates each run from user CLAUDE.md, hooks, and plugins, so
// an installed uncle-bob-junior plugin cannot leak rules into the baseline arm.
// The run gets an empty temp cwd for the same reason: no repo context to read.
//
// The arm prompt functions return a message array; promptfoo hands it to the
// provider as a JSON string. A system message maps to --append-system-prompt,
// the user message to the -p prompt.
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const RUN_TIMEOUT_MS = 300_000;

// One headless generation. Returns { text, costUsd, durationMs } or null on failure.
function askClaude(prompt, model, systemAppend) {
  const cliArgs = ['-p', prompt, '--safe-mode', '--model', model, '--output-format', 'json'];
  if (systemAppend) cliArgs.push('--append-system-prompt', systemAppend);
  const emptyCwd = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-bench-'));
  try {
    const raw = execFileSync('claude', cliArgs, {
      encoding: 'utf8',
      timeout: RUN_TIMEOUT_MS,
      cwd: emptyCwd,
      maxBuffer: 16 * 1024 * 1024,
    });
    const reply = JSON.parse(raw);
    return { text: reply.result || '', costUsd: reply.total_cost_usd ?? null, durationMs: reply.duration_ms ?? null };
  } catch (error) {
    console.error(`  run failed: ${String(error.message).slice(0, 200)}`);
    return null;
  } finally {
    fs.rmSync(emptyCwd, { recursive: true, force: true });
  }
}

// Returns { system, user } from promptfoo's rendered prompt. Plain-string
// prompts pass through as the user prompt with no system append.
function parsePromptMessages(prompt) {
  let messages;
  try {
    messages = JSON.parse(prompt);
  } catch {
    return { system: null, user: String(prompt) };
  }
  if (!Array.isArray(messages)) return { system: null, user: String(prompt) };
  const system = messages.find((m) => m.role === 'system')?.content ?? null;
  const user = messages
    .filter((m) => m.role === 'user')
    .map((m) => m.content)
    .join('\n\n');
  return { system, user };
}

class ClaudeCliProvider {
  constructor(options = {}) {
    this.model = options.config?.model || 'haiku';
    this.label = options.label || `claude-cli:${this.model}`;
  }

  id() {
    return `claude-cli:${this.model}`;
  }

  async callApi(prompt) {
    const { system, user } = parsePromptMessages(prompt);
    const reply = askClaude(user, this.model, system);
    if (!reply) return { error: `claude CLI run failed (model ${this.model})` };
    return { output: reply.text, cost: reply.costUsd ?? undefined };
  }
}

module.exports = ClaudeCliProvider;
module.exports.parsePromptMessages = parsePromptMessages;
module.exports.askClaude = askClaude;
