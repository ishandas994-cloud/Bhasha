// frontend/src/api/runCode.js
//
// Thin wrapper around the backend's POST /api/run endpoint. Kept as its
// own module (rather than an inline fetch() in App.jsx) so the request
// shape and error handling live in exactly one place - if the API
// contract changes, this is the only file that needs to know.

const RUN_ENDPOINT = "/api/run"; // proxied to http://localhost:8080 by vite.config.js in dev

/**
 * Sends BanglaLang source code to the backend and returns the parsed
 * result. Throws if the network request itself fails (server down,
 * DNS failure, etc.) - App.jsx is responsible for catching that and
 * showing a "couldn't reach the server" message, since a network
 * failure is a different situation from the *program* failing to run.
 *
 * @param {string} source - the BanglaLang source code to execute
 * @returns {Promise<{success: boolean, output: string, error: string|null}>}
 */
export async function runCode(source) {
  const response = await fetch(RUN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ source }),
  });

  if (!response.ok) {
    // Covers validation failures (400 from @NotBlank/@Size on RunRequest)
    // as well as any unexpected 5xx - try to read the body for a message,
    // but don't let a malformed error body crash this function too.
    let message = `Server responded with status ${response.status}.`;
    try {
      const body = await response.json();
      if (body?.error) message = body.error;
      else if (body?.message) message = body.message;
    } catch {
      // response body wasn't JSON - fall back to the generic message above
    }
    return { success: false, output: "", error: message };
  }

  return response.json();
}