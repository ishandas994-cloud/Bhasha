// frontend/src/App.jsx
//
// Top-level component. Owns the source code state (the single source of
// truth for what's in the editor), sends it to the backend when the user
// clicks "Run", and hands the result to OutputPanel. CodeEditor never
// talks to the network itself - it only reports text changes upward via
// onChange - which keeps it reusable/testable independent of the API.

import { useState, useCallback } from "react";
import CodeEditor from "./editor/CodeEditor";
import OutputPanel from "./components/OutputPanel";
import { runCode } from "./api/runCode";

const STARTER_CODE = `# বাংলা ল্যাং - BanglaLang
# একটি নমুনা প্রোগ্রাম

ধরি নাম = "বিশ্ব";
লিখ "নমস্কার, " + নাম + "!";

ধরি সংখ্যা = ৫;
যদি (সংখ্যা > ৩) {
    লিখ "সংখ্যাটি তিনের চেয়ে বড়";
} নাহলে {
    লিখ "সংখ্যাটি তিনের চেয়ে ছোট বা সমান";
}
`;

export default function App() {
  const [code, setCode] = useState(STARTER_CODE);
  const [result, setResult] = useState(null); // { success, output, error } | null
  const [isRunning, setIsRunning] = useState(false);

  const handleRun = useCallback(async () => {
    setIsRunning(true);
    try {
      const response = await runCode(code);
      setResult(response);
    } catch (networkError) {
      setResult({
        success: false,
        output: "",
        error: "সার্ভারে সংযোগ করা যায়নি। (Could not reach the server - is the backend running?)",
      });
    } finally {
      setIsRunning(false);
    }
  }, [code]);

  return (
    <div className="app">
      <header className="app-header">
        <h1>বাংলা ল্যাং — BanglaLang</h1>
        <button
          type="button"
          className="run-button"
          onClick={handleRun}
          disabled={isRunning}
        >
          {isRunning ? "চলছে... (Running...)" : "▶ চালাও (Run)"}
        </button>
      </header>

      <main className="app-main">
        <CodeEditor value={code} onChange={setCode} />
        <OutputPanel result={result} isRunning={isRunning} />
      </main>
    </div>
  );
}