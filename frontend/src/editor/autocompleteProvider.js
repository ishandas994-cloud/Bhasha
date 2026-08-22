// frontend/src/editor/autocompleteProvider.js
//
// Supplies suggestions for CodeMirror's autocomplete popup from two
// sources:
//   1. Static keywords - pulled from banglaLanguageDef.KEYWORDS, so this
//      list can never drift out of sync with what's actually highlighted
//      as a keyword in the editor.
//   2. Live variable/function names - extracted on every keystroke by
//      scanning the CURRENT document text for declarations
//      (ধরি <name>  ->  variable,  ফাংশন <name>  ->  function),
//      so as soon as the user declares `ধরি বয়স = ২৫;` the name বয়স
//      starts showing up as a suggestion elsewhere in the file.

import { autocompletion } from "@codemirror/autocomplete";
import { KEYWORDS } from "./banglaLanguageDef";

const IDENTIFIER_RE = /[\u0980-\u09FFa-zA-Z_][\u0980-\u09FFa-zA-Z0-9_]*/;

// Matches: ধরি <name>   (variable declaration)
const VAR_DECL_RE = /ধরি\s+([\u0980-\u09FFa-zA-Z_][\u0980-\u09FFa-zA-Z0-9_]*)/g;

// Matches: ফাংশন <name>   (function declaration)
const FUNC_DECL_RE = /ফাংশন\s+([\u0980-\u09FFa-zA-Z_][\u0980-\u09FFa-zA-Z0-9_]*)/g;

/**
 * Scan the full document text and return { variables: [...], functions: [...] }
 * with declared names, de-duplicated, in first-seen order.
 */
export function extractDeclaredNames(docText) {
  const variables = [];
  const functions = [];

  let match;
  VAR_DECL_RE.lastIndex = 0;
  while ((match = VAR_DECL_RE.exec(docText)) !== null) {
    if (!variables.includes(match[1])) variables.push(match[1]);
  }

  FUNC_DECL_RE.lastIndex = 0;
  while ((match = FUNC_DECL_RE.exec(docText)) !== null) {
    if (!functions.includes(match[1])) functions.push(match[1]);
  }

  return { variables, functions };
}

// Static keyword completions - built once, reused on every popup.
const KEYWORD_COMPLETIONS = Object.entries(KEYWORDS).map(([word, info]) => ({
  label: word,
  type: info.category === "builtin" ? "function" : info.category === "literal" ? "constant" : "keyword",
  detail: info.meaning,
  boost: 2, // rank language keywords above inferred variable names
}));

/**
 * The completion source function CodeMirror calls on every keystroke
 * inside a completion-triggering context.
 */
function banglaCompletionSource(context) {
  const word = context.matchBefore(IDENTIFIER_RE);
  if (!word || (word.from === word.to && !context.explicit)) {
    return null;
  }

  const docText = context.state.doc.toString();
  const { variables, functions } = extractDeclaredNames(docText);

  const dynamicCompletions = [
    ...variables.map((name) => ({ label: name, type: "variable", detail: "variable" })),
    ...functions.map((name) => ({ label: name, type: "function", detail: "function" })),
  ];

  return {
    from: word.from,
    options: [...KEYWORD_COMPLETIONS, ...dynamicCompletions],
    validFor: IDENTIFIER_RE,
  };
}

/**
 * Extension bundle to drop into CodeMirror's `extensions` array.
 * Overrides the default source with the BanglaLang-aware one.
 */
export function banglaAutocomplete() {
  return autocompletion({ override: [banglaCompletionSource] });
}

export default { banglaAutocomplete, extractDeclaredNames };