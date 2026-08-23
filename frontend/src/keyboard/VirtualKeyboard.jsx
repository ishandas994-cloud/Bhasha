// frontend/src/keyboard/VirtualKeyboard.jsx
//
// REDESIGNED: each key now inserts its exact glyph directly at the
// cursor - no hidden phonetic transliteration buffer. This fixes the
// core confusion of the old design: pressing "অ" (the independent
// vowel "o-sound") looked almost identical to "আ" (the "a-sound"
// needed to start a word like আমি), but only one of them produced the
// output you wanted, with no visual cue why.
//
// Bengali Unicode already auto-renders a consonant immediately followed
// by a dependent vowel sign (matra) as one visual syllable - ম + ি
// displays as "মি" automatically, no special combining code needed.
// So the keyboard just needs a row of vowel SIGNS (া ি ী ু ূ ে ৈ ো ৌ)
// separate from the row of independent vowel LETTERS (অ আ ই ঈ উ ঊ এ ঐ ও ঔ),
// and typing আমি becomes: click আ, click ম, click ি - each click's
// result is immediately visible, no composition step to get wrong.
//
// Contract with the parent (CodeEditor.jsx):
//   <VirtualKeyboard
//     onInsert={(text) => ...}     // insert this exact string at the cursor
//     onBackspace={() => ...}      // delete one character before the cursor
//   />

// Independent vowel LETTERS - used to start a word or stand alone (আমি, এটা)
const VOWEL_LETTERS = ["অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও", "ঔ"];

// Dependent vowel SIGNS (matra) - attach visually to the consonant before them.
// "্" (hasanta) is included here too - it removes a consonant's inherent
// vowel, which is how conjuncts are built (ক + ্ + ষ -> ক্ষ).
const VOWEL_SIGNS = [
  { glyph: "া", label: "া (আ-কার)" },
  { glyph: "ি", label: "ি (ই-কার)" },
  { glyph: "ী", label: "ী (ঈ-কার)" },
  { glyph: "ু", label: "ু (উ-কার)" },
  { glyph: "ূ", label: "ূ (ঊ-কার)" },
  { glyph: "ৃ", label: "ৃ (ঋ-কার)" },
  { glyph: "ে", label: "ে (এ-কার)" },
  { glyph: "ৈ", label: "ৈ (ঐ-কার)" },
  { glyph: "ো", label: "ো (ও-কার)" },
  { glyph: "ৌ", label: "ৌ (ঔ-কার)" },
  { glyph: "্", label: "্ (হসন্ত - যুক্তাক্ষরের জন্য)" },
  { glyph: "ং", label: "ং (অনুস্বার)" },
  { glyph: "ঃ", label: "ঃ (বিসর্গ)" },
  { glyph: "ঁ", label: "ঁ (চন্দ্রবিন্দু)" },
];

const CONSONANT_ROW_1 = ["ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ"];
const CONSONANT_ROW_2 = ["ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন"];
const CONSONANT_ROW_3 = ["প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ"];
const CONSONANT_ROW_4 = ["স", "হ", "ড়", "ঢ়", "য়", "ৎ"];

const DIGIT_ROW = ["১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০"];

// BanglaLang syntax symbols - these are language punctuation, not Bengali text
const SYMBOL_ROW = ["{", "}", "(", ")", ";", "=", '"', "+", "-", "*", "/", "।"];

export default function VirtualKeyboard({ onInsert, onBackspace }) {
  const key = (glyph, title) => (
    <button
      key={glyph}
      type="button"
      className="vk-key"
      title={title || glyph}
      onClick={() => onInsert?.(glyph)}
    >
      {glyph}
    </button>
  );

  return (
    <div className="virtual-keyboard" role="group" aria-label="Bengali keyboard">
      <div className="vk-section-label vk-label-vowels">স্বরবর্ণ (Vowel letters - start a word with these)</div>
      <div className="vk-row vk-row-vowels">{VOWEL_LETTERS.map((g) => key(g))}</div>

      <div className="vk-section-label vk-label-signs">কার (Vowel signs - attach these AFTER a consonant)</div>
      <div className="vk-row vk-row-signs">
        {VOWEL_SIGNS.map((v) => key(v.glyph, v.label))}
      </div>

      <div className="vk-section-label vk-label-consonants">ব্যঞ্জনবর্ণ (Consonants)</div>
      <div className="vk-row vk-row-consonants">{CONSONANT_ROW_1.map((g) => key(g))}</div>
      <div className="vk-row vk-row-consonants">{CONSONANT_ROW_2.map((g) => key(g))}</div>
      <div className="vk-row vk-row-consonants">{CONSONANT_ROW_3.map((g) => key(g))}</div>
      <div className="vk-row vk-row-consonants">{CONSONANT_ROW_4.map((g) => key(g))}</div>

      <div className="vk-section-label vk-label-digits">সংখ্যা ও চিহ্ন (Digits & symbols)</div>
      <div className="vk-row vk-row-digits">{DIGIT_ROW.map((g) => key(g))}</div>
      <div className="vk-row vk-row-symbols">{SYMBOL_ROW.map((g) => key(g))}</div>

      <div className="vk-row vk-row-controls">
        <button type="button" className="vk-key vk-key-wide" onClick={() => onBackspace?.()}>
          ⌫ Backspace
        </button>
        <button type="button" className="vk-key vk-key-wide" onClick={() => onInsert?.(" ")}>
          Space
        </button>
        <button type="button" className="vk-key vk-key-wide" onClick={() => onInsert?.("\n")}>
          ↵ Enter
        </button>
      </div>
    </div>
  );
}