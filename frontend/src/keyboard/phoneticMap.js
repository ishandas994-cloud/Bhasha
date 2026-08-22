// frontend/src/keyboard/phoneticMap.js
//
// Phonetic (Avro-style) Latin -> Bengali transliteration engine.
// This powers the on-screen virtual keyboard: the user clicks Latin
// letters (or types them), and this module converts the accumulated
// "buffer" of Latin keystrokes into Bengali script, key by key.
//
// Design: rather than a static 1:1 character map (which can't handle
// conjuncts, vowel signs that attach to the previous consonant, etc.),
// we keep a ROMAN BUFFER per "word" and re-run transliterate() on it
// after every keystroke, similar to how Avro Phonetic works.

// ---- Independent vowels (used when a vowel starts a word / stands alone) ----
export const INDEPENDENT_VOWELS = {
  a: "অ",
  aa: "আ",
  i: "ই",
  ii: "ঈ",
  ee: "ঈ",
  u: "উ",
  uu: "ঊ",
  oo: "ঊ",
  rri: "ঋ",
  e: "এ",
  oi: "ঐ",
  o: "ও",
  ou: "ঔ",
};

// ---- Dependent vowel signs (kar) - attach to a preceding consonant ----
export const VOWEL_SIGNS = {
  a: "", // inherent vowel, no visible sign
  aa: "া",
  i: "ি",
  ii: "ী",
  ee: "ী",
  u: "ু",
  uu: "ূ",
  oo: "ূ",
  rri: "ৃ",
  e: "ে",
  oi: "ৈ",
  o: "ো",
  ou: "ৌ",
};

// ---- Consonants (base forms, without inherent vowel shown) ----
export const CONSONANTS = {
  k: "ক",
  kh: "খ",
  g: "গ",
  gh: "ঘ",
  Ng: "ঙ",
  c: "চ",
  ch: "ছ",
  j: "জ",
  jh: "ঝ",
  NG: "ঞ",
  T: "ট",
  Th: "ঠ",
  D: "ড",
  Dh: "ঢ",
  N: "ণ",
  t: "ত",
  th: "থ",
  d: "দ",
  dh: "ধ",
  n: "ন",
  p: "প",
  ph: "ফ",
  f: "ফ",
  b: "ব",
  bh: "ভ",
  v: "ভ",
  m: "ম",
  z: "য",
  r: "র",
  l: "ল",
  sh: "শ",
  Sh: "ষ",
  s: "স",
  h: "হ",
  y: "য়",
  R: "ড়",
  Rh: "ঢ়",
  t0: "ৎ",
  ng: "ং",
  H: "ঃ",
  cnd: "ঁ",
};

// ---- Digits ----
export const DIGITS = {
  0: "০",
  1: "১",
  2: "২",
  3: "৩",
  4: "৪",
  5: "৫",
  6: "৬",
  7: "৭",
  8: "৮",
  9: "৯",
};

// Longest-match-first key lists, so "kh" is tried before "k", etc.
const CONSONANT_KEYS = Object.keys(CONSONANTS).sort((a, b) => b.length - a.length);
const VOWEL_KEYS = Object.keys(INDEPENDENT_VOWELS).sort((a, b) => b.length - a.length);

const HASANTA = "্"; // virama - used to build conjuncts (e.g. k + HASANTA + t)

/**
 * Transliterate a single Latin "word" (no spaces) into Bengali script.
 * Handles: consonant clusters (conjuncts via hasanta), consonant+vowel
 * (dependent vowel sign), and standalone vowels (independent vowel form).
 *
 * Examples:
 *   "ami"   -> "আমি"
 *   "dhori" -> "ধরি"
 *   "jodi"  -> "যদি"
 *   "bidyaloy" -> "বিদ্যালয়"
 */
export function transliterateWord(latinWord) {
  if (!latinWord) return "";

  let i = 0;
  let output = "";
  let pendingConsonant = null; // Bengali char of a consonant awaiting its vowel/hasanta

  const input = latinWord;

  const matchAt = (keys, pos) => {
    for (const key of keys) {
      if (input.slice(pos, pos + key.length) === key) {
        return key;
      }
    }
    return null;
  };

  while (i < input.length) {
    // Digits pass through untouched into Bengali numerals
    if (DIGITS[input[i]]) {
      if (pendingConsonant) {
        output += pendingConsonant;
        pendingConsonant = null;
      }
      output += DIGITS[input[i]];
      i += 1;
      continue;
    }

    const consonantKey = matchAt(CONSONANT_KEYS, i);
    const vowelKey = !consonantKey ? matchAt(VOWEL_KEYS, i) : null;

    if (consonantKey) {
      if (pendingConsonant) {
        // Two consonants in a row with no vowel between them -> conjunct
        output += pendingConsonant + HASANTA;
      }
      pendingConsonant = CONSONANTS[consonantKey];
      i += consonantKey.length;
      continue;
    }

    if (vowelKey) {
      if (pendingConsonant) {
        // Attach dependent vowel sign to the pending consonant
        output += pendingConsonant + VOWEL_SIGNS[vowelKey];
        pendingConsonant = null;
      } else {
        // Standalone vowel -> independent vowel form
        output += INDEPENDENT_VOWELS[vowelKey];
      }
      i += vowelKey.length;
      continue;
    }

    // Unrecognized character (punctuation, etc.) - flush pending consonant, pass through
    if (pendingConsonant) {
      output += pendingConsonant;
      pendingConsonant = null;
    }
    output += input[i];
    i += 1;
  }

  if (pendingConsonant) {
    output += pendingConsonant;
  }

  return output;
}

/**
 * Transliterate a full line/buffer (may contain spaces, punctuation,
 * and BanglaLang symbols like { } ( ) ; = which should pass through
 * unchanged since they're part of the language's syntax, not text).
 */
export function transliterateBuffer(buffer) {
  // Split on whitespace but keep the separators so spacing is preserved.
  return buffer
    .split(/(\s+)/)
    .map((chunk) => (/\s+/.test(chunk) ? chunk : transliterateWord(chunk)))
    .join("");
}

// Characters the virtual keyboard should NOT try to transliterate -
// these are BanglaLang syntax tokens, passed straight through.
export const PASSTHROUGH_SYMBOLS = ["{", "}", "(", ")", ";", "=", "+", "-", "*", "/", ",", '"', "<", ">", "!"];

export default {
  transliterateWord,
  transliterateBuffer,
  CONSONANTS,
  INDEPENDENT_VOWELS,
  VOWEL_SIGNS,
  DIGITS,
  PASSTHROUGH_SYMBOLS,
};