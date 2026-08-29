// Functional correctness assertion for the Java benchmark tasks: proves
// "cleaner" is never quietly "broken". The email and csv tasks compile and run
// the generated code with the local JDK (javac + java) against real checks;
// the open-ended tasks (retry, ratelimit, order, statement, booking, config)
// get structural checks, since their contracts have no single runnable shape.
//
// Metric: `correct` (1 = all checks pass, 0 = at least one fails). Without a
// JDK the executable checks report "skipped" and pass, so a missing toolchain
// never masquerades as broken generated code.

const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

function correctnessTimeoutMs() {
  const value = Number.parseInt(process.env.UNCLE_BOB_JUNIOR_CORRECTNESS_TIMEOUT_MS || '', 10);
  return Number.isFinite(value) && value > 0 ? value : 60_000;
}

// Extract fenced code blocks, tagged by language.
function extractBlocks(text) {
  text = String(text || '');
  const matches = [...text.matchAll(/```(\w*)\r?\n([\s\S]*?)```/g)];
  // ubj: terse models often answer with bare, unfenced code. Treat the whole
  // response as one block so the gate scores the code instead of reporting "no block".
  if (matches.length === 0 && text.trim()) return [{ lang: '', code: text }];
  return matches.map((m) => ({ lang: (m[1] || '').toLowerCase(), code: m[2] }));
}

// Identify which task we're evaluating from vars.task.
function identifyTask(task) {
  const t = task.toLowerCase();
  if (t.includes('email') && t.includes('valid')) return 'email';
  if (t.includes('csv') && t.includes('sum')) return 'csv';
  if (t.includes('retry')) return 'retry';
  if (t.includes('rate limit') || t.includes('rate-limit') || t.includes('rate limiting')) return 'ratelimit';
  if (t.includes('order') && t.includes('vat')) return 'order';
  if (t.includes('bank statement')) return 'statement';
  if (t.includes('booking')) return 'booking';
  if (t.includes('configuration') && t.includes('pars')) return 'config';
  return null;
}

// ubj: probe once at load; the executable checks are meaningless without a JDK.
let jdkChecked = false;
let jdkAvailable = false;
function hasJdk() {
  if (jdkChecked) return jdkAvailable;
  jdkChecked = true;
  try {
    execFileSync('javac', ['-version'], { stdio: 'pipe', timeout: 15_000 });
    execFileSync('java', ['-version'], { stdio: 'pipe', timeout: 15_000 });
    jdkAvailable = true;
  } catch {
    jdkAvailable = false;
  }
  return jdkAvailable;
}

function javaBlocksOf(blocks) {
  const java = blocks.filter((b) => b.lang === 'java' || (!b.lang && /\b(class|public|void)\b/.test(b.code)));
  return java.length ? java : null;
}

// Replies often append a usage-example block of bare statements, or a JUnit
// test block whose org.junit imports cannot compile without the dependency;
// joining either into the compilation unit breaks javac. Keep declaration
// blocks that aren't tests, falling back to everything when nothing declares.
function compilableBlocks(java) {
  const declares = (code) => /\b(class|interface|enum|record)\s+\w+/.test(code)
    || /\b[\w<>\[\]]+\s+\w+\s*\([^)]*\)\s*\{/.test(code);
  const isTest = (code) => /@Test\b|\borg\.junit\b/.test(code);
  const picked = java.filter((b) => declares(b.code) && !isTest(b.code));
  return picked.length ? picked : java;
}

// Model classes must be package-private so our public Bench class owns the file name.
function demoteTopLevelTypes(code) {
  return code.replace(/^(\s*)public\s+((?:final\s+|abstract\s+)?(?:class|interface|enum|record)\b)/gm, '$1$2');
}

function declaredTypeNames(code) {
  return [...code.matchAll(/\b(?:class|interface|enum|record)\s+(\w+)/g)].map((m) => m[1]);
}

// Hoist the model's package/import lines above our Bench class; a compilation
// unit accepts them only at the top.
function splitImports(code) {
  const imports = [];
  const body = code
    .split('\n')
    .filter((line) => {
      if (/^\s*(package|import)\s/.test(line)) {
        if (/^\s*import\s/.test(line)) imports.push(line.trim());
        return false;
      }
      return true;
    })
    .join('\n');
  return { imports: [...new Set(imports)], body };
}

// Compile Bench.java (our harness class + the model's demoted classes) and run
// it. Returns { ok, output } where output is stdout+stderr, truncated.
function compileAndRun(harnessClass, modelCode, files = {}) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'ubj-java-'));
  try {
    const { imports, body } = splitImports(modelCode);
    const source = imports.join('\n') + '\n' + harnessClass + '\n' + demoteTopLevelTypes(body) + '\n';
    fs.writeFileSync(path.join(dir, 'Bench.java'), source);
    for (const [name, content] of Object.entries(files)) {
      fs.writeFileSync(path.join(dir, name), content);
    }
    const opts = { cwd: dir, timeout: correctnessTimeoutMs(), encoding: 'utf8', stdio: 'pipe' };
    try {
      execFileSync('javac', ['Bench.java'], opts);
      const stdout = execFileSync('java', ['-cp', '.', 'Bench'], opts);
      return { ok: true, output: String(stdout).slice(0, 2000) };
    } catch (e) {
      const output = `${e.stdout || ''}\n${e.stderr || e.message || ''}`.trim().slice(0, 800);
      return { ok: false, output };
    }
  } finally {
    fs.rmSync(dir, { recursive: true, force: true });
  }
}

// Harness class for the email task: reflect over the model's classes for a
// String -> boolean method and feed it known-good and known-bad addresses.
function emailHarness(typeNames) {
  const names = typeNames.map((n) => `"${n}"`).join(', ');
  return `
public class Bench {
    public static void main(String[] args) throws Exception {
        String[] classes = { ${names} };
        // Well-factored answers have several String->boolean methods (the
        // public validator plus private helpers); pick the best-named public
        // one instead of whichever reflection lists first.
        java.lang.reflect.Method target = null;
        Object owner = null;
        int bestScore = -1;
        for (String className : classes) {
            Class<?> type = Class.forName(className);
            for (java.lang.reflect.Method method : type.getDeclaredMethods()) {
                boolean stringIn = method.getParameterCount() == 1 && method.getParameterTypes()[0] == String.class;
                boolean booleanOut = method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
                if (!stringIn || !booleanOut) continue;
                String name = method.getName().toLowerCase();
                int score = 0;
                if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) score += 2;
                if (name.contains("valid") || name.contains("email")) score += 1;
                if (score <= bestScore) continue;
                method.setAccessible(true);
                target = method;
                bestScore = score;
                owner = null;
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    var ctor = type.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    owner = ctor.newInstance();
                }
            }
        }
        if (target == null) { System.out.println("FAIL: no String->boolean validator method found"); System.exit(1); }
        expect(target, owner, "user@example.com", true);
        expect(target, owner, "a@b.co", true);
        expect(target, owner, "no-at-sign", false);
        expect(target, owner, "", false);
        expect(target, owner, "@missing-local.com", false);
        System.out.println("PASS");
    }

    private static void expect(java.lang.reflect.Method target, Object owner, String input, boolean want) throws Exception {
        boolean got;
        try {
            got = (Boolean) target.invoke(owner, input);
        } catch (Exception e) {
            got = false; // a validator that throws on bad input is still rejecting it
            if (want) { System.out.println("FAIL: threw on valid input: " + input); System.exit(1); }
        }
        if (got != want) { System.out.println("FAIL: " + input + " -> " + got + ", want " + want); System.exit(1); }
    }
}
`;
}

// Harness class for the csv task: invoke the model's own main and check the
// printed sum. Fixture rows sum to exactly 351.
function csvHarness(typeNames) {
  const names = typeNames.map((n) => `"${n}"`).join(', ');
  return `
public class Bench {
    public static void main(String[] args) throws Exception {
        String[] classes = { ${names} };
        for (String className : classes) {
            Class<?> type = Class.forName(className);
            try {
                java.lang.reflect.Method main = type.getDeclaredMethod("main", String[].class);
                main.setAccessible(true);
                main.invoke(null, (Object) new String[0]);
                return;
            } catch (NoSuchMethodException e) {
                // try the next class
            }
        }
        System.out.println("FAIL: no main method in generated code");
        System.exit(1);
    }
}
`;
}

const CSV_FIXTURE = 'id,amount,region\n1,120.50,north\n2,80.25,south\n3,150.25,north\n';
// 351 exactly; tolerate 351, 351.0, 351.00 but reject 3510/1351-style neighbours.
const CSV_SUM_PATTERN = /(^|[^\d.])351(\.0{1,2})?([^\d]|$)/m;

const SKIP_NO_JDK = { pass: true, score: 1, reason: 'skipped: no JDK on PATH (install one to run correctness checks)' };

const CHECKS = {
  email(blocks) {
    if (!hasJdk()) return SKIP_NO_JDK;
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    // Replies often offer alternative implementations that redeclare the same
    // class or method, so joining them cannot compile. Each candidate runs on
    // its own; one working validator means the answer works.
    let lastReason = 'no compilable validator block';
    for (const block of compilableBlocks(java)) {
      let code = block.code;
      // A bare method with no surrounding type still deserves a run.
      if (!/\b(class|interface|enum|record)\s+\w+/.test(code)) code = `class ModelCode {\n${code}\n}`;
      const run = compileAndRun(emailHarness(declaredTypeNames(code)), code);
      if (run.ok && run.output.includes('PASS')) return { pass: true, reason: 'Email validator passes all checks' };
      lastReason = run.output || 'validator run failed';
    }
    return { pass: false, reason: lastReason };
  },

  csv(blocks) {
    if (!hasJdk()) return SKIP_NO_JDK;
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = compilableBlocks(java).map((b) => b.code).join('\n');
    const types = declaredTypeNames(code);
    if (types.length === 0) return { pass: false, reason: 'No class declaration in generated code' };
    const run = compileAndRun(csvHarness(types), code, { 'sales.csv': CSV_FIXTURE });
    if (!run.ok) return { pass: false, reason: run.output || 'csv run failed' };
    if (CSV_SUM_PATTERN.test(run.output)) return { pass: true, reason: 'CSV sum printed correctly (351)' };
    return { pass: false, reason: `expected sum 351 in output, got: ${run.output.slice(0, 200)}` };
  },

  retry(blocks) {
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = java.map((b) => b.code).join('\n');
    const failures = [];
    if (!/\b(for|while)\s*\(/.test(code)) failures.push('no retry loop');
    if (!/\bcatch\s*\(/.test(code)) failures.push('no exception handling');
    if (!/Thread\.sleep|TimeUnit\.\w+\.sleep|ScheduledExecutor|\.sleep\(/.test(code)) failures.push('no delay between attempts');
    if (failures.length === 0) return { pass: true, reason: 'Retry helper has required structure' };
    return { pass: false, reason: 'Missing: ' + failures.join(', ') };
  },

  ratelimit(blocks) {
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = java.map((b) => b.code).join('\n');
    const failures = [];
    if (!/Map|Deque|Queue|AtomicInteger|AtomicLong|Semaphore|Bucket|Cache/.test(code)) failures.push('no per-client state');
    if (!/currentTimeMillis|nanoTime|Instant\.|LocalDateTime|System\.currentTimeMillis|ScheduledExecutor|TimeUnit/.test(code)) failures.push('no time window logic');
    if (!/429|TOO_MANY|TooManyRequests|reject|deny|exceed|limit/i.test(code)) failures.push('no reject path');
    if (failures.length === 0) return { pass: true, reason: 'Rate limiter has required structure' };
    return { pass: false, reason: 'Missing: ' + failures.join(', ') };
  },

  order(blocks) {
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = java.map((b) => b.code).join('\n');
    const failures = [];
    if (!/0\.21|21\b/.test(code)) failures.push('no 21% VAT');
    if (!/0\.1\b|0\.10|10\b/.test(code)) failures.push('no 10% discount');
    if (!/100\b/.test(code)) failures.push('no 100-euro threshold');
    if (!/throw|IllegalArgument|isEmpty|isBlank|== null|!= null|Objects\.require/.test(code)) failures.push('no item validation');
    if (!/String\.format|StringBuilder|StringJoiner|"\s*\+|\+\s*"|formatted\(|text block/i.test(code)) failures.push('no receipt string building');
    if (failures.length === 0) return { pass: true, reason: 'Order processor has required structure' };
    return { pass: false, reason: 'Missing: ' + failures.join(', ') };
  },

  statement(blocks) {
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = java.map((b) => b.code).join('\n');
    const failures = [];
    if (!/USD/.test(code) || !/GBP/.test(code)) failures.push('no USD/GBP conversion');
    if (!(/salary/i.test(code) && /rent/i.test(code) && /grocer/i.test(code))) failures.push('no category rules');
    if (!/2000/.test(code)) failures.push('no 2000 EUR suspicion threshold');
    if (!/duplicat|seen|repeat/i.test(code)) failures.push('no duplicate detection');
    if (!/YearMonth|Month|getMonth|substring\(0,\s*7\)|yyyy-MM/i.test(code)) failures.push('no per-month grouping');
    if (!/String\.format|StringBuilder|StringJoiner|"\s*\+|\+\s*"|formatted\(/.test(code)) failures.push('no report building');
    if (failures.length === 0) return { pass: true, reason: 'Statement analyser has required structure' };
    return { pass: false, reason: 'Missing: ' + failures.join(', ') };
  },

  booking(blocks) {
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = java.map((b) => b.code).join('\n');
    const failures = [];
    if (!/hold/i.test(code)) failures.push('no hold lifecycle');
    if (!/confirm/i.test(code)) failures.push('no confirm step');
    if (!/cancel|release/i.test(code)) failures.push('no cancel path');
    if (!/15/.test(code) || !/minute|Duration|Instant|currentTimeMillis|LocalDateTime|plusMinutes/i.test(code)) failures.push('no hold expiry');
    if (!(/adult/i.test(code) && /child/i.test(code) && /senior/i.test(code) && /student/i.test(code))) failures.push('no price tiers');
    if (!/refund/i.test(code)) failures.push('no refund policy');
    if (!/wait/i.test(code) || !/Queue|Deque|LinkedList|List/.test(code)) failures.push('no waiting list');
    if (failures.length === 0) return { pass: true, reason: 'Booking engine has required structure' };
    return { pass: false, reason: 'Missing: ' + failures.join(', ') };
  },

  config(blocks) {
    const java = javaBlocksOf(blocks);
    if (!java) return { pass: false, reason: 'No Java code block found' };
    const code = java.map((b) => b.code).join('\n');
    const failures = [];
    if (!/section/i.test(code)) failures.push('no section handling');
    if (!/#/.test(code)) failures.push('no comment handling');
    if (!/parseInt|Integer\.parse|parseBoolean|Boolean\.parse|parseLong|Duration/.test(code)) failures.push('no typed values');
    if (!/line/i.test(code) || !/error|exception|invalid|throw/i.test(code)) failures.push('no line-numbered errors');
    if (!/default/i.test(code)) failures.push('no defaults');
    if (failures.length === 0) return { pass: true, reason: 'Config parser has required structure' };
    return { pass: false, reason: 'Missing: ' + failures.join(', ') };
  },
};

// --- Main assertion entry point ---

module.exports = (output, context) => {
  const task = identifyTask(context.vars.task || '');
  if (!task) {
    return { pass: true, score: 1, reason: 'Unknown task, skipped correctness check' };
  }

  const blocks = extractBlocks(String(output || ''));
  if (blocks.length === 0) {
    return { pass: false, score: 0, reason: 'No code blocks in output' };
  }

  const check = CHECKS[task];
  const result = check(blocks);
  return {
    pass: result.pass,
    score: result.pass ? 1 : 0,
    reason: result.reason,
  };
};
