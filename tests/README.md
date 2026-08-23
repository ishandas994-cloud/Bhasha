# BanglaLang Test Suite

Each `NN-name.bangla` script is executed through the CLI binary, and its
stdout must exactly match the sibling `.expected` file.

| File | Covers |
|------|--------|
| `01-hello` | Basic `লিখ` printing |
| `02-arithmetic` | `+ - * / %`, unary minus, precedence |
| `03-values` | Strings, concatenation, booleans (`সত্য`/`মিথ্যা`), null (`নাল`) |
| `04-conditionals` | `যদি` / `নাহলে`, nested else-if chains |
| `05-logic` | `&&` `\|\|` `!` incl. short-circuit proof |
| `06-while-loop` | `যতক্ষণ` with counter updates |
| `07-for-loop` | `জন্য` init/condition/increment, nesting |
| `08-break-continue` | `থামো`, `চালিয়ে_যাও` in while & for |
| `09-functions` | `ফাংশন` params, returns, string building |
| `10-recursion` | Factorial & Fibonacci recursion depth |
| `11-bengali-text` | Conjuncts (ক্ষ), vowel signs (আমি), Bengali numerals in math |
| `12-scoping` | Block shadowing vs bare assignment semantics |

## Running

```powershell
# Windows
.\tests\run-tests.ps1              # auto-detects exe > jar > classes
.\tests\run-tests.ps1 path\to\cli.jar   # explicit binary

# Linux / macOS
./tests/run-tests.sh
```

The runner prints a PASS/FAIL line per test plus a summary, and exits
non-zero on any failure - suitable for CI.

## Adding a test

1. Create `13-my-feature.bangla`.
2. Run it once: `bangla run 13-my-feature.bangla`
3. Save the output to `13-my-feature.expected`.
4. Re-run the suite.
