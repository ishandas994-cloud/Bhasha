import { StreamLanguage } from "@codemirror/language";
import { LanguageSupport } from "@codemirror/language";
import { tags as t } from "@lezer/highlight";
import { HighlightStyle, syntaxHighlighting } from "@codemirror/language";


export const KEYWORDS = {
  "ধরি": { meaning: "let / declare variable", category: "keyword" },
  "যদি": { meaning: "if", category: "keyword" },
  "নাহলে": { meaning: "else", category: "keyword" },
  "যতক্ষণ": { meaning: "while", category: "keyword" },
  "ফাংশন": { meaning: "function", category: "keyword" },
  "ফেরত": { meaning: "return", category: "keyword" },
  "লিখ": { meaning: "print", category: "builtin" },
  "সত্য": { meaning: "true", category: "literal" },
  "মিথ্যা": { meaning: "false", category: "literal" },
  "থামো": { meaning: "break", category: "keyword" },
  "চালিয়ে_যাও": { meaning: "continue", category: "keyword" },
  "জন্য": { meaning: "for", category: "keyword" },
};

export const KEYWORD_LIST = Object.keys(KEYWORDS);

const OPERATORS = ["+", "-", "*", "/", "%", "==", "!=", "<=", ">=", "<", ">", "="];
const PUNCTUATION = ["{", "}", "(", ")", ";", ","];

// Longest-match-first so multi-char operators (==, !=) beat single-char (=)
const SORTED_OPERATORS = [...OPERATORS].sort((a, b) => b.length - a.length);

/**
 * Tokenizer for CodeMirror's StreamLanguage. Reads one token at a time
 * from the stream and returns a highlight tag name.
 */
const banglaStreamParser = {
  startState() {
    return { inString: false };
  },

  token(stream, state) {
    if (stream.eatSpace()) return null;

    // Comments: # to end of line
    if (stream.match("#")) {
      stream.skipToEnd();
      return "comment";
    }

    // String literals
    if (stream.peek() === '"') {
      stream.next();
      while (!stream.eol()) {
        const ch = stream.next();
        if (ch === "\\") {
          stream.next(); // skip escaped char
          continue;
        }
        if (ch === '"') break;
      }
      return "string";
    }

    // Numbers (Bengali digits ০-৯ or ASCII 0-9)
    if (stream.match(/^[0-9০-৯]+(\.[0-9০-৯]+)?/)) {
      return "number";
    }

    // Keywords / identifiers (Bengali word characters, underscore, ASCII letters)
    if (stream.match(/^[\u0980-\u09FFa-zA-Z_][\u0980-\u09FFa-zA-Z0-9_]*/)) {
      const word = stream.current();
      if (KEYWORDS[word]) {
        const category = KEYWORDS[word].category;
        if (category === "builtin") return "builtin";
        if (category === "literal") return "atom";
        return "keyword";
      }
      return "variableName";
    }

    // Operators
    for (const op of SORTED_OPERATORS) {
      if (stream.match(op, true)) return "operator";
    }

    // Punctuation
    if (PUNCTUATION.includes(stream.peek())) {
      stream.next();
      return "punctuation";
    }

    stream.next();
    return null;
  },
};

export const banglaStreamLanguage = StreamLanguage.define(banglaStreamParser);

// ---- Highlight style: maps token tags to editor colors ----
export const banglaHighlightStyle = HighlightStyle.define([
  { tag: t.keyword, color: "#c678dd", fontWeight: "bold" },
  { tag: t.name, color: "#61afef" }, // builtins like লিখ (mapped from "builtin")
  { tag: t.atom, color: "#d19a66" }, // সত্য / মিথ্যা constants
  { tag: t.string, color: "#98c379" },
  { tag: t.number, color: "#d19a66" },
  { tag: t.comment, color: "#7f848e", fontStyle: "italic" },
  { tag: t.variableName, color: "#e06c75" },
  { tag: t.operator, color: "#56b6c2" },
  { tag: t.punctuation, color: "#abb2bf" },
]);

/**
 * Full language support bundle to pass into CodeMirror's `extensions`.
 * Usage: <CodeMirror extensions={[bangla()]} ... />
 */
export function bangla() {
  return new LanguageSupport(banglaStreamLanguage, [syntaxHighlighting(banglaHighlightStyle)]);
}

export default {
  KEYWORDS,
  KEYWORD_LIST,
  bangla,
  banglaStreamLanguage,
  banglaHighlightStyle,
};