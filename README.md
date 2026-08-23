# BanglaLang — বাংলা প্রোগ্রামিং ভাষা

**BanglaLang** is a Bengali-keyword programming language with a complete interpreter pipeline (**Lexer → Parser → Interpreter**) built in Java, paired with a browser-based playground where you can write and run BanglaLang code with full syntax highlighting, autocompletion, and a virtual Bangla keyboard.

Write code like this:

```bangla
ধরি নাম = "বিশ্ব";
লিখ "হ্যালো, " + নাম + "!";

যদি (১০ > ৫) {
    লিখ "গণিত এখনও কাজ করে";
} নাহলে {
    লিখ "এটা অসম্ভব!";
}
```

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Download & Install](#download--install)
- [CLI Usage](#cli-usage)
- [API Reference](#api-reference)
- [Language Reference](#language-reference)
- [Error Handling](#error-handling)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Bengali keywords** — write programs using `ধরি`, `যদি`, `নাহলে`, `যতক্ষণ`, `ফাংশন` and more.
- **Bengali numerals supported** — use `৫`, `২৫.৫`, or plain `5`, `25.5`.
- **Full language core** — variables, arithmetic, conditionals, loops (`while` / `for`), functions with return values, and logical operators.
- **Stage-aware errors** — lexing, parsing, and runtime errors are reported separately so you always know *what kind* of mistake you made.
- **Smart editor** — CodeMirror-based editor with custom Bangla syntax highlighting and keyword autocompletion.
- **Virtual Bangla keyboard** — type Bengali characters without a native keyboard installed, including phonetic mapping support.

## Architecture

BanglaLang follows the classic three-stage interpreter design:

```
Source Code (বাংলা)
      │
      ▼
┌───────────┐   tokens   ┌───────────┐    AST     ┌───────────────┐   output
│   Lexer   │ ─────────▶ │   Parser  │ ─────────▶ │  Interpreter  │ ─────────▶ Console
└───────────┘            └───────────┘            └───────────────┘
```

1. **Lexer** — converts raw source text into a stream of tokens (`TokenType.java`).
2. **Parser** — builds an Abstract Syntax Tree (AST) from the token stream (`Stmt.java`, `Expr.java`).
3. **Interpreter** — walks the AST and executes it against an environment/scope chain (`Environment.java`).

The backend exposes this pipeline over HTTP, and the frontend is a single-page playground that talks to it.

## Tech Stack

| Layer     | Technology                                        |
|-----------|---------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.3.2, Maven, Bean Validation |
| Frontend  | React 18, Vite 5, CodeMirror 6                    |
| Dev tooling | Vite dev-server proxy, Maven wrapper via npm     |

## Project Structure

```
bangla-lang/
├── backend/                        # Spring Boot interpreter API + CLI
│   ├── pom.xml                     # Maven build (default: web, -Pcli: CLI jar)
│   ├── Dockerfile                  # Container build for the web API
│   └── src/main/java/com/banglalang/
│       ├── lexer/                  # Tokenizer (Token, TokenType, Lexer)
│       ├── parser/                 # Parser + AST definitions (Stmt, Expr)
│       ├── interpreter/            # Tree-walking interpreter + environment
│       ├── cli/                    # CliMain — standalone `bangla` command
│       ├── api/                    # REST layer (/api/run), CORS config
│       └── Main.java               # Spring Boot entry point
│   └── src/main/resources/
│       └── application.properties  # Server config (port from $PORT)
│
├── frontend/                       # React playground
│   ├── package.json
│   ├── vercel.json                 # Vercel deploy config (Vite)
│   ├── vite.config.js              # Dev proxy: /api → localhost:8081
│   └── src/
│       ├── editor/                 # CodeMirror setup, Bangla grammar, autocomplete
│       ├── keyboard/               # Virtual Bangla keyboard (neon glass UI)
│       ├── components/             # Output panel
│       ├── api/                    # runCode.js — fetch wrapper for /api/run
│       └── App.jsx                 # Playground layout
│
├── examples/
│   └── hello.bangla                # Runnable demo program for the CLI
├── scripts/                        # Local CLI build scripts (Windows/Linux/macOS)
├── .github/workflows/release.yml   # Release pipeline → GitHub Releases
├── package.json                    # Root helper deps
└── README.md
```

## Prerequisites

Make sure you have the following installed:

| Tool        | Version | Check with          |
|-------------|---------|---------------------|
| Java JDK    | 17+     | `java -version`     |
| Maven       | 3.8+    | `mvn -version`      |
| Node.js     | 18+     | `node -v`           |
| npm         | 9+      | `npm -v`            |

> The root `package.json` includes the `mvn` npm package, which can be used to run Maven without a system-wide install.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ishandas994-cloud/Bhasha.git
cd Bhasha
```

### 2. Start the backend (port 8081)

```bash
cd backend
mvn spring-boot:run
```

The interpreter API will be available at `http://localhost:8081`.

To build a production JAR instead:

```bash
mvn clean package
java -jar target/bangla-lang-backend.jar
```

### 3. Start the frontend (port 5173)

Open a new terminal:

```bash
cd frontend
npm install
npm run dev
```

Then open **http://localhost:5173** in your browser.

> During development, Vite automatically proxies all `/api/*` requests to the backend at `localhost:8081` — no manual URL or CORS setup needed.

## Download & Install

BanglaLang ships as a standalone CLI — download one binary, put it on your `PATH`, and run programs. **No Java installation required** (the native packages bundle their own runtime).

### Downloads

All builds are produced automatically by GitHub Actions and published on the [Releases](https://github.com/ishandas994-cloud/Bhasha/releases) page. These links always fetch the **latest** release:

| Platform | Download | Size |
|----------|----------|------|
| Windows 10/11 (x64) | [bangla-windows-x64.zip](https://github.com/ishandas994-cloud/Bhasha/releases/latest/download/bangla-windows-x64.zip) | ~47 MB |
| Linux (x64) | [bangla-linux-x64.tar.gz](https://github.com/ishandas994-cloud/Bhasha/releases/latest/download/bangla-linux-x64.tar.gz) | ~45 MB |
| macOS (Apple Silicon) | [bangla-macos-aarch64.tar.gz](https://github.com/ishandas994-cloud/Bhasha/releases/latest/download/bangla-macos-aarch64.tar.gz) | ~45 MB |
| Any OS with Java 17+ | [bangla-lang-cli.jar](https://github.com/ishandas994-cloud/Bhasha/releases/latest/download/bangla-lang-cli.jar) | ~30 KB |

> Older versions are available on the [full releases list](https://github.com/ishandas994-cloud/Bhasha/releases).

### Setup Guide — Windows

1. **Download** `bangla-windows-x64.zip` from the table above.
2. **Extract** it — you get a folder named `bangla` containing `bangla.exe`:

   ```powershell
   Expand-Archive .\Downloads\bangla-windows-x64.zip -DestinationPath C:\Tools\
   ```

3. **Add it to PATH** so you can type `bangla` from anywhere (pick one):
   - *GUI:* Settings → search "environment variables" → Edit the user `Path` → New → `C:\Tools\bangla`
   - *PowerShell (permanent for your user):*

     ```powershell
     [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Tools\bangla", "User")
     ```

4. **Open a new terminal** (PATH changes only apply to fresh terminals) and verify:

   ```powershell
   bangla --version
   # BanglaLang 1.0.0
   ```

*Don't want to touch PATH?* Just call the exe by its full path: `C:\Tools\bangla\bangla.exe run hello.bangla`

### Setup Guide — Linux

```bash
# 1. Download & extract
wget https://github.com/ishandas994-cloud/Bhasha/releases/latest/download/bangla-linux-x64.tar.gz
tar -xzf bangla-linux-x64.tar.gz

# 2. Install to a directory on PATH
sudo mv bangla/bin/bangla /usr/local/bin/

# 3. Verify
bangla --version
```

### Setup Guide — macOS (Apple Silicon)

```bash
# 1. Download & extract
curl -LO https://github.com/ishandas994-cloud/Bhasha/releases/latest/download/bangla-macos-aarch64.tar.gz
tar -xzf bangla-macos-aarch64.tar.gz

# 2. Install to a directory on PATH
sudo mv bangla/bin/bangla /usr/local/bin/
chmod +x /usr/local/bin/bangla

# 3. If Gatekeeper blocks the unsigned binary:
xattr -d com.apple.quarantine /usr/local/bin/bangla 2>/dev/null || true

# 4. Verify
bangla --version
```

### Alternative — the plain jar (any OS)

If you already have Java 17+ installed, skip the native bundles entirely:

```bash
java -jar bangla-lang-cli.jar run hello.bangla
```

Handy as a shell alias: `alias bangla='java -jar ~/tools/bangla-lang-cli.jar'`

## CLI Usage

### Commands

```
bangla run <file.bangla>    বাংলা প্রোগ্রাম চালান — execute a program
bangla --version, -v        সংস্করণ দেখান — show version
bangla --help, -h           এই বার্তা দেখান — show help
```

### Your first program

1. Create a file named `hello.bangla` (UTF-8 encoding):

   ```bangla
   ধরি নাম = "পৃথিবী";
   লিখ "নমস্কার, " + নাম + "!";

   ধরি i = ১;
   যতক্ষণ (i <= ৩) {
       লিখ "সংখ্যা: " + i;
       i = i + ১;
   }
   ```

2. Run it:

   ```
   $ bangla run hello.bangla
   নমস্কার, পৃথিবী!
   সংখ্যা: ১
   সংখ্যা: ২
   সংখ্যা: ৩
   ```

3. Explore more features in [`examples/hello.bangla`](examples/hello.bangla) — functions (`ফাংশন`), conditionals (`যদি`/`নাহলে`), and loops.

> **Editor tip:** save files with UTF-8 encoding (the default in VS Code / Notepad++) or Bengali text will corrupt.

### Exit codes (for scripts & CI)

| Code | Meaning |
|------|---------|
| `0`  | Program ran successfully |
| `64` | Bad command-line usage |
| `65` | Lexing/parsing error in the source |
| `66` | Input file missing or unreadable |
| `70` | Runtime error during execution |

Example — run tests only if the program compiles cleanly:

```bash
bangla run main.bangla && echo "OK" || echo "FAILED with code $?"
```

### Building from source

Requires JDK 17+. Maven is used if available; otherwise plain `javac`.

```bash
git clone https://github.com/ishandas994-cloud/Bhasha.git
cd Bhasha

./scripts/build-cli.sh              # Linux/macOS  (--native → bundled launcher tarball)
.\scripts\build-cli.ps1             # Windows      (-Native → jpackage .exe zip)
```

The jar lands at `backend/target/bangla-lang-cli.jar`. Release artifacts are cut automatically when a `v*` tag is pushed — see [`.github/workflows/release.yml`](.github/workflows/release.yml).

### How it works

The CLI is the same interpreter core that powers the web playground, with Spring Boot compiled out entirely (`mvn -Pcli package`). `jpackage` then wraps that jar with a trimmed JRE into a self-contained folder per platform — which is what gets zipped into the release artifacts.

## API Reference

### Run BanglaLang code

```http
POST /api/run
Content-Type: application/json
```

**Request body**

| Field    | Type   | Required | Description                |
|----------|--------|----------|----------------------------|
| `source` | string | Yes      | BanglaLang source code     |

**Example request**

```json
{
  "source": "ধরি সংখ্যা = ৫; লিখ সংখ্যা * ২;"
}
```

**Success response**

```json
{
  "output": "১০\n",
  "error": null
}
```

**Failure response** (e.g., parse error)

```json
{
  "output": "",
  "error": "পার্সিং ত্রুটি (Parsing Error): ..."
}
```

| Response field | Description                                                        |
|----------------|--------------------------------------------------------------------|
| `output`       | Program stdout (partial output is preserved on runtime errors).    |
| `error`        | Stage-tagged error message (`null` when execution succeeds).        |

## Language Reference

### Keywords

| Keyword         | Meaning                        | Equivalent |
|-----------------|--------------------------------|------------|
| `ধরি`           | Declare a variable             | `let`      |
| `যদি`           | Conditional                    | `if`       |
| `নাহলে`         | Alternative branch             | `else`     |
| `যতক্ষণ`        | Loop while condition holds     | `while`    |
| `জন্য`          | Counted loop                   | `for`      |
| `ফাংশন`         | Define a function              | `function` |
| `ফেরত`          | Return from function           | `return`   |
| `লিখ`           | Print to output                | `print`    |
| `সত্য`          | Boolean truth value            | `true`     |
| `মিথ্যা`        | Boolean false value            | `false`    |
| `থামো`          | Exit loop early                | `break`    |
| `চালিয়ে_যাও`    | Skip to next loop iteration    | `continue` |

### Operators

| Category   | Symbols                                  |
|------------|------------------------------------------|
| Arithmetic | `+`  `-`  `*`  `/`  `%`                  |
| Comparison | `==`  `!=`  `<`  `<=`  `>`  `>=`         |
| Logical    | `&&`  `\|\|`  `!`                        |
| Assignment | `=`                                      |

### Literals

- **Numbers:** `৫`, `২৫.৫`, `5`, `25.5`
- **Strings:** `"কিছু লেখা"`
- **Booleans:** `সত্য`, `মিথ্যা`

### Example Programs

**Variables and printing**

```bangla
ধরি নাম = "রাহুল";
ধরি বয়স = ২১;
লিখ "নাম: " + নাম;
লিখ "বয়স: " + বয়স;
```

**Conditionals**

```bangla
ধরি নম্বর = ৭৫;

যদি (নম্বর >= ৮০) {
    লিখ "A গ্রেড";
} নাহলে {
    লিখ "A গ্রেড নয়";
}
```

**Loops**

```bangla
ধরি i = ১;
যতক্ষণ (i <= ৫) {
    লিখ i;
    i = i + ১;    # plain assignment updates the existing variable
}
```

> **`ধরি` vs bare assignment:** `ধরি x = ...` *declares* a new variable in the current scope — inside a loop body, that shadows the outer `x`, so the counter would never advance. Use bare assignment (`x = ...`) to update an existing variable.

**Functions**

```bangla
ফাংশন যোগ(a, b) {
    ফেরত a + b;
}

লিখ যোগ(১০, ৩২);
```

## Error Handling

Errors are reported per pipeline stage, so messages are precise rather than generic:

| Stage            | Error prefix                              | Example cause                          |
|------------------|-------------------------------------------|----------------------------------------|
| Lexing           | `লেক্সিং ত্রুটি (Lexing Error)`            | Unknown character in source            |
| Parsing          | `পার্সিং ত্রুটি (Parsing Error)`           | Wrong keyword order / missing `;`      |
| Runtime          | `রানটাইম ত্রুটি (Runtime Error)`           | Type mismatch, undefined variable      |

Partial program output produced *before* a runtime error is preserved in the response, and infinite recursion is caught safely instead of crashing the server.

## Roadmap

- [ ] Arrays / lists (`তালিকা`)
- [ ] String manipulation built-ins
- [x] Comments in Bangla (`#`)
- [x] Standalone CLI runner (`bangla run file.bangla`) — see [Download & Install](#download--install)
- [ ] Standard library (`গণিত`, `সময়`)
- [ ] Online sharing of runnable snippets

## Contributing

Contributions are welcome!

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push the branch: `git push origin feature/my-feature`
5. Open a Pull Request.

Please make sure the backend builds cleanly (`mvn clean package`) before submitting.

## License

This project is licensed under the [MIT License](LICENSE).
