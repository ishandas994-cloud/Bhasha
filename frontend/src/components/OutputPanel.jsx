// frontend/src/components/OutputPanel.jsx
//
// Purely presentational - just renders whatever App.jsx's last run
// produced. Three states: nothing run yet, currently running, or a
// result (success or failure) to display.

export default function OutputPanel({ result, isRunning }) {
  return (
    <div className="output-panel">
      <div className="output-panel-header">আউটপুট (Output)</div>

      <div className="output-panel-body">
        {isRunning && <div className="output-status">চলছে... (Running...)</div>}

        {!isRunning && result === null && (
          <div className="output-placeholder">
            কোড চালাতে উপরের "চালাও" বাটনে ক্লিক করুন।
            <br />
            (Click "Run" above to execute your code.)
          </div>
        )}

        {!isRunning && result !== null && (
          <>
            {result.output && (
              <pre className="output-text">{result.output}</pre>
            )}

            {!result.success && result.error && (
              <div className="output-error">{result.error}</div>
            )}

            {result.success && !result.output && (
              <div className="output-placeholder">
                প্রোগ্রাম চলেছে কিন্তু কিছু প্রিন্ট হয়নি। "লিখ" ব্যবহার করুন আউটপুট দেখতে।
                <br />
                (Program ran but printed nothing. Use "লিখ" to print output.)
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}