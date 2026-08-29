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

// The benchmark is single-shot text generation, so the session gets no tools:
// with tools available, models sometimes "verify" their answer by running it,
// and a task whose program never exits (Game of Life redraws forever) then
// hangs the whole eval until the timeout.
function cliArgsFor(prompt, model, systemAppend) {
  const cliArgs = ['-p', prompt, '--safe-mode', '--tools', '', '--model', model, '--output-format', 'json'];
  if (systemAppend) cliArgs.push('--append-system-prompt', systemAppend);
  return cliArgs;
}

// The CLI can exit 0 with valid JSON for a failed generation (API error,
// usage limit): is_error/subtype carry the verdict and `result` is empty.
// Those must surface as errors, not score as an empty answer.
function interpretReply(reply) {
  if (reply.is_error || (reply.subtype && reply.subtype !== 'success')) {
    return { error: `claude CLI reported ${reply.subtype || 'is_error'}: ${String(reply.result || '').slice(0, 200)}` };
  }
  if (String(reply.result || '').trim() === '') {
    return { error: 'claude CLI returned an empty result' };
  }
  return { text: reply.result, costUsd: reply.total_cost_usd ?? null, durationMs: reply.duration_ms ?? null };
}

// One headless generation. Returns { text, costUsd, durationMs } or { error }.
function askClaude(prompt, model, systemAppend) {
  const emptyCwd = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-bench-'));
  try {
    const raw = execFileSync('claude', cliArgsFor(prompt, model, systemAppend), {
      encoding: 'utf8',
      timeout: RUN_TIMEOUT_MS,
      // SIGTERM can be survived mid-request and would leave execFileSync
      // blocked forever; SIGKILL makes the timeout a real upper bound.
      killSignal: 'SIGKILL',
      cwd: emptyCwd,
      maxBuffer: 16 * 1024 * 1024,
    });
    return interpretReply(JSON.parse(raw));
  } catch (error) {
    return { error: `claude CLI run failed: ${String(error.message).slice(0, 200)}` };
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

// Joins model and arm in the provider label, e.g. "claude-cli:haiku · uncle-bob-junior".
// The promptfoo web UI names its graph series by provider only (never by prompt
// label), so the arm must ride along here for the graphs to tell the arms apart.
// The exporter splits on this separator to recover the bare model for report.md.
const ARM_SEPARATOR = ' · ';

class ClaudeCliProvider {
  constructor(options = {}) {
    this.model = options.config?.model || 'haiku';
    this.arm = options.config?.arm || null;
    this.label = this.id();
  }

  id() {
    const model = `claude-cli:${this.model}`;
    return this.arm ? `${model}${ARM_SEPARATOR}${this.arm}` : model;
  }

  async callApi(prompt) {
    const { system, user } = parsePromptMessages(prompt);
    const reply = askClaude(user, this.model, system);
    if (reply.error) return { error: `${reply.error} (model ${this.model})` };
    return { output: reply.text, cost: reply.costUsd ?? undefined };
  }
}

module.exports = ClaudeCliProvider;
module.exports.ARM_SEPARATOR = ARM_SEPARATOR;
module.exports.parsePromptMessages = parsePromptMessages;
module.exports.askClaude = askClaude;
module.exports.cliArgsFor = cliArgsFor;
module.exports.interpretReply = interpretReply;
