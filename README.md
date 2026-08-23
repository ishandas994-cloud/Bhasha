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
├── backend/                        # Spring Boot interpreter API
│   ├── pom.xml                     # Maven build configuration
│   └── src/main/java/com/banglalang/
│       ├── lexer/                  # Tokenizer (Token, TokenType, Lexer)
│       ├── parser/                 # Parser + AST definitions (Stmt, Expr)
│       ├── interpreter/            # Tree-walking interpreter + environment
│       ├── api/                    # REST layer (/api/run), CORS config
│       └── Main.java               # CLI entry point
│   └── src/main/resources/
│       └── application.properties  # Server config (port 8081)
│
├── frontend/                       # React playground
│   ├── package.json
│   ├── vite.config.js              # Dev proxy: /api → localhost:8081
│   └── src/
│       ├── editor/                 # CodeMirror setup, Bangla grammar, autocomplete
│       ├── keyboard/               # Virtual Bangla keyboard + phonetic map
│       ├── components/             # Output panel
│       ├── api/                    # runCode.js — fetch wrapper for /api/run
│       └── App.jsx                 # Playground layout
│
├── package.json                    # Root helper deps (mvn runner)
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
git clone https://github.com/ishandas994-cloud/bangla-lang.git
cd bangla-lang
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
    ধরি i = i + ১;
}
```

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
- [ ] Comments in Bangla (`#` or `//`)
- [ ] Standalone CLI runner (`bangla run file.bangla`)
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
