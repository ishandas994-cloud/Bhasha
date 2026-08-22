// frontend/src/keyboard/VirtualKeyboard.jsx
//
// Clickable on-screen keyboard for typing Bengali phonetically without
// an OS-level IME. The user clicks Latin letter keys; this component
// accumulates them into a "current word" buffer, transliterates that
// buffer to Bengali on every keystroke (via phoneticMap), and reports
// the live result up to the parent editor so it can be shown inline.
//
// Contract with the parent (CodeEditor.jsx):
//   <VirtualKeyboard
//     onLiveChange={(bengaliText, latinBuffer) => ...}   // fires on every key
//     onCommitWord={(bengaliText) => ...}                // fires on space/enter
//     onBackspaceCommit={() => ...}                      // fires when buffer is
//                                                          // already empty and
//                                                          // backspace is pressed
//                                                          // (parent deletes 1
//                                                          // char from its own text)
//   />
//
// The parent owns the actual text buffer (CodeMirror doc). This component
// only owns the "word currently being composed" state.

import { useState, useCallback } from "react";
import { transliterateWord } from "./phoneticMap";

// Key layout - phonetic Latin keys grouped the way a Bengali typist expects.
// Each entry: { latin: string, label: string (Bengali char shown on keycap) }
const VOWEL_ROW = [
  { latin: "a", label: "অ" },
  { latin: "aa", label: "আ" },
  { latin: "i", label: "ই" },
  { latin: "ii", label: "ঈ" },
  { latin: "u", label: "উ" },
  { latin: "uu", label: "ঊ" },
  { latin: "e", label: "এ" },
  { latin: "oi", label: "ঐ" },
  { latin: "o", label: "ও" },
  { latin: "ou", label: "ঔ" },
];

const CONSONANT_ROW_1 = [
  { latin: "k", label: "ক" },
  { latin: "kh", label: "খ" },
  { latin: "g", label: "গ" },
  { latin: "gh", label: "ঘ" },
  { latin: "Ng", label: "ঙ" },
  { latin: "c", label: "চ" },
  { latin: "ch", label: "ছ" },
  { latin: "j", label: "জ" },
  { latin: "jh", label: "ঝ" },
  { latin: "T", label: "ট" },
];

const CONSONANT_ROW_2 = [
  { latin: "Th", label: "ঠ" },
  { latin: "D", label: "ড" },
  { latin: "Dh", label: "ঢ" },
  { latin: "N", label: "ণ" },
  { latin: "t", label: "ত" },
  { latin: "th", label: "থ" },
  { latin: "d", label: "দ" },
  { latin: "dh", label: "ধ" },
  { latin: "n", label: "ন" },
  { latin: "p", label: "প" },
];

const CONSONANT_ROW_3 = [
  { latin: "ph", label: "ফ" },
  { latin: "b", label: "ব" },
  { latin: "bh", label: "ভ" },
  { latin: "m", label: "ম" },
  { latin: "z", label: "য" },
  { latin: "r", label: "র" },
  { latin: "l", label: "ল" },
  { latin: "sh", label: "শ" },
  { latin: "Sh", label: "ষ" },
  { latin: "s", label: "স" },
  { latin: "h", label: "হ" },
];

const DIGIT_ROW = [
  { latin: "1", label: "১" },
  { latin: "2", label: "২" },
  { latin: "3", label: "৩" },
  { latin: "4", label: "৪" },
  { latin: "5", label: "৫" },
  { latin: "6", label: "৬" },
  { latin: "7", label: "৭" },
  { latin: "8", label: "৮" },
  { latin: "9", label: "৯" },
  { latin: "0", label: "০" },
];

const SYMBOL_ROW = [
  { latin: "{", label: "{" },
  { latin: "}", label: "}" },
  { latin: "(", label: "(" },
  { latin: ")", label: ")" },
  { latin: ";", label: ";" },
  { latin: "=", label: "=" },
  { latin: '"', label: '"' },
];

export default function VirtualKeyboard({ onLiveChange, onCommitWord, onBackspaceCommit }) {
  // The Latin keystrokes composing the "current word" (not yet committed).
  const [buffer, setBuffer] = useState("");

  const pushKey = useCallback(
    (latinChar) => {
      const nextBuffer = buffer + latinChar;
      setBuffer(nextBuffer);
      onLiveChange?.(transliterateWord(nextBuffer), nextBuffer);
    },
    [buffer, onLiveChange]
  );

  const pushSymbol = useCallback(
    (symbol) => {
      // Symbols aren't part of phonetic composition - commit whatever
      // word is pending first, then pass the symbol straight through.
      if (buffer) {
        onCommitWord?.(transliterateWord(buffer));
        setBuffer("");
      }
      onCommitWord?.(symbol);
    },
    [buffer, onCommitWord]
  );

  const handleSpace = useCallback(() => {
    if (buffer) {
      onCommitWord?.(transliterateWord(buffer) + " ");
      setBuffer("");
    } else {
      onCommitWord?.(" ");
    }
  }, [buffer, onCommitWord]);

  const handleEnter = useCallback(() => {
    if (buffer) {
      onCommitWord?.(transliterateWord(buffer) + "\n");
      setBuffer("");
    } else {
      onCommitWord?.("\n");
    }
  }, [buffer, onCommitWord]);

  const handleBackspace = useCallback(() => {
    if (buffer.length > 0) {
      const nextBuffer = buffer.slice(0, -1);
      setBuffer(nextBuffer);
      onLiveChange?.(transliterateWord(nextBuffer), nextBuffer);
    } else {
      // Nothing pending - let the parent delete a character from committed text
      onBackspaceCommit?.();
    }
  }, [buffer, onLiveChange, onBackspaceCommit]);

  const renderRow = (keys, rowClass) => (
    <div className={`vk-row ${rowClass}`}>
      {keys.map((k) => (
        <button
          key={k.latin}
          type="button"
          className="vk-key"
          onClick={() => pushKey(k.latin)}
          title={`Latin: ${k.latin}`}
        >
          {k.label}
        </button>
      ))}
    </div>
  );

  return (
    <div className="virtual-keyboard" role="group" aria-label="Bengali phonetic keyboard">
      <div className="vk-preview" aria-live="polite">
        {buffer ? transliterateWord(buffer) : <span className="vk-preview-placeholder">টাইপ করুন…</span>}
      </div>

      {renderRow(VOWEL_ROW, "vk-row-vowels")}
      {renderRow(CONSONANT_ROW_1, "vk-row-consonants")}
      {renderRow(CONSONANT_ROW_2, "vk-row-consonants")}
      {renderRow(CONSONANT_ROW_3, "vk-row-consonants")}
      {renderRow(DIGIT_ROW, "vk-row-digits")}
      {renderRow(SYMBOL_ROW, "vk-row-symbols")}

      <div className="vk-row vk-row-controls">
        <button type="button" className="vk-key vk-key-wide" onClick={handleBackspace}>
          ⌫ Backspace
        </button>
        <button type="button" className="vk-key vk-key-wide" onClick={handleSpace}>
          Space
        </button>
        <button type="button" className="vk-key vk-key-wide" onClick={handleEnter}>
          ↵ Enter
        </button>
      </div>
    </div>
  );
}