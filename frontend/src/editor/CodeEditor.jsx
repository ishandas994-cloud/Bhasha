// frontend/src/editor/CodeEditor.jsx
//
// The actual code editor surface. Wraps @uiw/react-codemirror with the
// BanglaLang language support (syntax highlighting) and bridges it to
// VirtualKeyboard: the keyboard doesn't touch the document directly,
// it reports Bengali text up via callbacks, and THIS component is the
// one that inserts that text into the CodeMirror document at the
// current cursor position.
//
// It also exposes the current document text upward (onChange) so
// App.jsx can send it to the backend /api/run endpoint, and downward
// to autocompleteProvider.js so variable-name suggestions can be
// derived from what's actually been typed.

import { useRef, useCallback, useState } from "react";
import CodeMirror from "@uiw/react-codemirror";
import { EditorView } from "@codemirror/view";
import { oneDark } from "@codemirror/theme-one-dark";
import { bangla } from "./banglaLanguageDef";
import { banglaAutocomplete } from "./autocompleteProvider";
import VirtualKeyboard from "../keyboard/VirtualKeyboard";

export default function CodeEditor({ value, onChange }) {
  const viewRef = useRef(null);
  // Tracks the length of the most recent "live preview" insertion so the
  // next keystroke can replace it instead of appending beside it -
  // this is what makes phonetic composition (d -> dh -> dho -> dhor -> dhori)
  // look like a single word growing/correcting in place, not five separate
  // words stacking up.
  const [livePreviewLength, setLivePreviewLength] = useState(0);

  const handleCreateEditor = useCallback((view) => {
    viewRef.current = view;
  }, []);

  const replaceLivePreview = useCallback(
    (newText) => {
      const view = viewRef.current;
      if (!view) return;

      const cursor = view.state.selection.main.head;
      const from = cursor - livePreviewLength;

      view.dispatch({
        changes: { from: Math.max(from, 0), to: cursor, insert: newText },
        selection: { anchor: Math.max(from, 0) + newText.length },
      });
    },
    [livePreviewLength]
  );

  // Fired on every virtual-keyboard keystroke while a word is being composed
  const handleLiveChange = useCallback(
    (bengaliText) => {
      replaceLivePreview(bengaliText);
      setLivePreviewLength(bengaliText.length);
    },
    [replaceLivePreview]
  );

  // Fired when a word is finalized (space/enter) or a symbol is inserted
  const handleCommitWord = useCallback(
    (finalText) => {
      replaceLivePreview(finalText);
      setLivePreviewLength(0);
    },
    [replaceLivePreview]
  );

  // Fired when backspace is pressed with no pending phonetic buffer -
  // delete one character from the committed document instead.
  const handleBackspaceCommit = useCallback(() => {
    const view = viewRef.current;
    if (!view) return;
    const cursor = view.state.selection.main.head;
    if (cursor === 0) return;
    view.dispatch({
      changes: { from: cursor - 1, to: cursor, insert: "" },
      selection: { anchor: cursor - 1 },
    });
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

      <VirtualKeyboard
        onLiveChange={handleLiveChange}
        onCommitWord={handleCommitWord}
        onBackspaceCommit={handleBackspaceCommit}
      />
    </div>
  );
}