// Turns fenced code blocks from a model reply into real source files: one
// file per top-level Java type (imports attributed to the types that use
// them), language-appropriate file names and extensions, and the habit-hooks
// plugin list matching the languages present. Shared by the promptfoo judge
// and the results exporter, so what gets scanned is exactly what gets saved.

const EXTENSIONS = {
  java: 'java',
  python: 'py',
  py: 'py',
  typescript: 'ts',
  ts: 'ts',
  tsx: 'tsx',
  javascript: 'js',
  js: 'js',
  jsx: 'jsx',
  php: 'php',
};

// habit-hooks plugin per fence language; JS runs under the typescript plugin.
const PLUGINS = {
  java: 'java',
  python: 'python',
  py: 'python',
  typescript: 'typescript',
  ts: 'typescript',
  tsx: 'typescript',
  javascript: 'typescript',
  js: 'typescript',
  jsx: 'typescript',
  php: 'php',
};

function pluginsFor(blocks) {
  const plugins = new Set(blocks.map((block) => PLUGINS[block.lang]).filter(Boolean));
  plugins.add('generic');
  return [...plugins];
}

const TYPE_DECLARATION = /\b(?:class|interface|enum|record)\s+(\w+)/;

// Brace depth at the start of each line, ignoring braces inside strings,
// chars, and comments — enough structure to find top-level type boundaries
// without a real parser.
function lineDepths(code) {
  const depths = [];
  let depth = 0;
  let state = 'code'; // code | line-comment | block-comment | string | char | text-block
  for (const line of code.split('\n')) {
    depths.push(depth);
    if (state === 'line-comment') state = 'code';
    for (let i = 0; i < line.length; i++) {
      const pair = line.slice(i, i + 2);
      if (state === 'code') {
        if (pair === '//') { state = 'line-comment'; break; }
        if (pair === '/*') { state = 'block-comment'; i++; continue; }
        if (line.slice(i, i + 3) === '"""') { state = 'text-block'; i += 2; continue; }
        if (line[i] === '"') { state = 'string'; continue; }
        if (line[i] === "'") { state = 'char'; continue; }
        if (line[i] === '{') depth++;
        if (line[i] === '}') depth = Math.max(0, depth - 1);
      } else if (state === 'block-comment') {
        if (pair === '*/') { state = 'code'; i++; }
      } else if (state === 'text-block') {
        if (line.slice(i, i + 3) === '"""') { state = 'code'; i += 2; }
      } else if (state === 'string' || state === 'char') {
        if (line[i] === '\\') i++;
        else if (state === 'string' && line[i] === '"') state = 'code';
        else if (state === 'char' && line[i] === "'") state = 'code';
      }
    }
    if (state === 'string' || state === 'char' || state === 'line-comment') state = 'code';
  }
  return depths;
}

function isAttachable(line) {
  const trimmed = line.trim();
  return trimmed === '' || trimmed.startsWith('@') || trimmed.startsWith('//')
    || trimmed.startsWith('/*') || trimmed.startsWith('*');
}

// Member names of the JDK packages models import with wildcards. Copying a
// wildcard import into every split unit manufactures unused-import findings
// the author never wrote (the original single file used the package fine), so
// wildcards are attributed like named imports: to the units that reference a
// known member. Packages not listed here still go to every unit.
const WILDCARD_PACKAGE_MEMBERS = {
  'java.util': /\b(List|ArrayList|LinkedList|Map|HashMap|TreeMap|LinkedHashMap|EnumMap|Set|HashSet|TreeSet|EnumSet|Deque|ArrayDeque|Queue|PriorityQueue|Optional|OptionalInt|Iterator|Collections|Arrays|Objects|Comparator|Scanner|UUID|Random|StringJoiner|Locale|NoSuchElementException)\b/,
  'java.util.stream': /\b(Stream|IntStream|LongStream|DoubleStream|Collectors)\b/,
  'java.util.concurrent': /\b(ConcurrentHashMap|ConcurrentMap|ExecutorService|Executors|ScheduledExecutorService|TimeUnit|CountDownLatch|Semaphore|CompletableFuture|ConcurrentLinkedQueue|CopyOnWriteArrayList|BlockingQueue|LinkedBlockingQueue)\b/,
  'java.util.concurrent.atomic': /\b(AtomicInteger|AtomicLong|AtomicBoolean|AtomicReference)\b/,
  'java.util.function': /\b(Function|BiFunction|Supplier|Consumer|BiConsumer|Predicate|UnaryOperator|BinaryOperator)\b/,
  'java.util.regex': /\b(Pattern|Matcher)\b/,
  'java.io': /\b(BufferedReader|BufferedWriter|FileReader|FileWriter|InputStream|OutputStream|InputStreamReader|OutputStreamWriter|PrintWriter|PrintStream|Reader|Writer|IOException|UncheckedIOException|File)\b/,
  'java.nio.file': /\b(Files|Path|Paths)\b/,
  'java.time': /\b(LocalDate|LocalDateTime|LocalTime|Instant|Duration|Period|YearMonth|Year|MonthDay|Month|DayOfWeek|ZonedDateTime|OffsetDateTime|ZoneId|ZoneOffset|Clock)\b/,
  'java.time.format': /\b(DateTimeFormatter|DateTimeParseException)\b/,
  'java.math': /\b(BigDecimal|BigInteger|RoundingMode|MathContext)\b/,
};

// Whether one split unit references anything an import brings in. Named
// imports match their imported name; wildcard imports of known JDK packages
// match any known member; unknown wildcards count as referenced everywhere.
function importReferenced(importLine, body) {
  const wildcard = importLine.match(/import\s+(?:static\s+)?([\w.]+)\.\*\s*;/);
  if (wildcard) {
    const members = WILDCARD_PACKAGE_MEMBERS[wildcard[1]];
    return members ? members.test(body) : true;
  }
  const imported = importLine.match(/(\w+)\s*;/)?.[1];
  return Boolean(imported && new RegExp(`\\b${imported}\\b`).test(body));
}

// Split one Java compilation unit into one unit per top-level type. The
// package line goes to every unit; each import goes to the units that
// reference what it imports (see importReferenced); an import no unit
// references stays in the first unit, once, so a genuinely unused import is
// still there for the unused-import rule to catch. Returns null when there
// is nothing to split.
function splitJavaTypes(code) {
  const lines = String(code).split('\n');
  const depths = lineDepths(code);

  // Nested types sit at depth > 0 when the line starts, so depth-0 matches
  // are exactly the top-level declarations. Annotations and comments directly
  // above a declaration move with it.
  const typeStarts = [];
  for (let i = 0; i < lines.length; i++) {
    const trimmed = lines[i].trim();
    const isDeclaration = depths[i] === 0 && TYPE_DECLARATION.test(lines[i])
      && !trimmed.startsWith('//') && !trimmed.startsWith('*') && !trimmed.startsWith('/*')
      && !/^\s*(?:package|import)\s/.test(lines[i]);
    if (!isDeclaration) continue;
    const floor = typeStarts.length ? typeStarts[typeStarts.length - 1] + 1 : 0;
    let start = i;
    while (start > floor && isAttachable(lines[start - 1])) start--;
    typeStarts.push(start);
  }
  if (typeStarts.length < 2) return null;

  const headerEnd = typeStarts[0];
  const packageLines = [];
  const imports = [];
  for (const line of lines.slice(0, headerEnd)) {
    if (/^\s*package\s/.test(line)) packageLines.push(line.trim());
    else if (/^\s*import\s/.test(line)) imports.push(line.trim());
  }

  const bodies = typeStarts.map((start, index) => {
    const end = index + 1 < typeStarts.length ? typeStarts[index + 1] : lines.length;
    return lines.slice(start, end).join('\n').trim();
  });

  const used = new Set();
  const units = bodies.map((body) => {
    const name = body.match(TYPE_DECLARATION)?.[1] || 'Snippet';
    const unitImports = imports.filter((imp) => {
      const referenced = importReferenced(imp, body);
      if (referenced) used.add(imp);
      return referenced;
    });
    return { name, packageLines, imports: unitImports, body };
  });

  const orphaned = imports.filter((imp) => !used.has(imp));
  units[0].imports = [...new Set([...units[0].imports, ...orphaned])];

  return units.map((unit) => ({
    name: unit.name,
    code: [...unit.packageLines, ...(unit.packageLines.length ? [''] : []), ...unit.imports, ...(unit.imports.length ? [''] : []), unit.body].join('\n') + '\n',
  }));
}

const NAME_PATTERNS = {
  java: TYPE_DECLARATION,
  py: /(?:class|def)\s+(\w+)/,
  ts: /(?:class|function)\s+(\w+)/,
  tsx: /(?:class|function)\s+(\w+)/,
  js: /(?:class|function)\s+(\w+)/,
  jsx: /(?:class|function)\s+(\w+)/,
  php: /(?:class|function)\s+(\w+)/,
};

function fileNameFor(code, ext, index) {
  const declared = code.match(NAME_PATTERNS[ext] || TYPE_DECLARATION);
  const base = declared ? declared[1] : `Snippet${index + 1}`;
  return `${base}.${ext}`;
}

// Blocks in, files out: {name, content} with unique names. A java block with
// no top-level type declaration is a snippet — a usage example, shell output
// pasted into a java fence, or bare statements — not a valid compilation
// unit. The benchmark judges valid code only, so snippets are excluded.
function codeFiles(blocks) {
  const files = [];
  blocks.forEach((block, index) => {
    const ext = EXTENSIONS[block.lang] || 'java';
    if (ext === 'java') {
      if (!TYPE_DECLARATION.test(block.code)) return; // snippet: excluded
      const split = splitJavaTypes(block.code);
      if (split) {
        split.forEach((part) => files.push({ name: `${part.name}.java`, content: part.code }));
        return;
      }
      files.push({ name: fileNameFor(block.code, 'java', index), content: block.code });
      return;
    }
    files.push({ name: fileNameFor(block.code, ext, index), content: block.code });
  });

  const used = new Set();
  return files.map((file, index) => {
    let { name } = file;
    if (used.has(name)) name = name.replace(/\.(\w+)$/, `-${index + 1}.$1`);
    used.add(name);
    return { name, content: file.content };
  });
}

// A model reply sometimes keeps its test class in the same code block as the
// production code (a hand-rolled runner called from main, or a JUnit class
// below the implementation). Block-level test detection cannot see it, so the
// judge would scan test code as production. Detected per extracted file.
function isTestFile(file) {
  return /Tests?\.\w+$/.test(file.name) || /@Test\b|org\.junit/.test(file.content);
}

// The files the smell judge scans: everything codeFiles extracts minus the
// units that are themselves tests.
function productionFiles(blocks) {
  return codeFiles(blocks).filter((file) => !isTestFile(file));
}

module.exports = { pluginsFor, splitJavaTypes, fileNameFor, codeFiles, isTestFile, productionFiles };
