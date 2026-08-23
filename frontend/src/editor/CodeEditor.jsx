// frontend/src/editor/CodeEditor.jsx
//
// UPDATED for the redesigned VirtualKeyboard: since the keyboard now
// inserts exact glyphs directly (no phonetic composition buffer to
// replace-in-place), this component's job simplifies a lot - just
// insert whatever text the keyboard reports, at the current cursor
// position, and move the cursor past it.

import { useRef, useCallback } from "react";
import CodeMirror from "@uiw/react-codemirror";
import { EditorView } from "@codemirror/view";
import { oneDark } from "@codemirror/theme-one-dark";
import { bangla } from "./banglaLanguageDef";
import { banglaAutocomplete } from "./autocompleteProvider";
import VirtualKeyboard from "../keyboard/VirtualKeyboard";

export default function CodeEditor({ value, onChange }) {
  const viewRef = useRef(null);

  const handleCreateEditor = useCallback((view) => {
    viewRef.current = view;
  }, []);

  // Inserts `text` at the current cursor position and moves the cursor
  // past it - called for every virtual-keyboard key press (letters,
  // vowel signs, digits, symbols, space, and newline all go through here).
  const handleInsert = useCallback((text) => {
    const view = viewRef.current;
    if (!view) return;

    const cursor = view.state.selection.main.head;
    view.dispatch({
      changes: { from: cursor, to: cursor, insert: text },
      selection: { anchor: cursor + text.length },
    });
    view.focus();
  }, []);

  // Deletes one character immediately before the cursor.
  const handleBackspace = useCallback(() => {
    const view = viewRef.current;
    if (!view) return;

    const cursor = view.state.selection.main.head;
    if (cursor === 0) return;

    view.dispatch({
      changes: { from: cursor - 1, to: cursor, insert: "" },
      selection: { anchor: cursor - 1 },
    });
    view.focus();
  }, []);

  return (
    <div className="code-editor-wrapper">
      <CodeMirror
        value={value}
        height="420px"
        theme={oneDark}
        extensions={[bangla(), banglaAutocomplete(), EditorView.lineWrapping]}
        onChange={(val) => onChange?.(val)}
        onCreateEditor={handleCreateEditor}
        basicSetup={{
          lineNumbers: true,
          highlightActiveLine: true,
          autocompletion: true,
          bracketMatching: true,
          closeBrackets: true,
        }}
      />

      <VirtualKeyboard onInsert={handleInsert} onBackspace={handleBackspace} />
    </div>
  );
}