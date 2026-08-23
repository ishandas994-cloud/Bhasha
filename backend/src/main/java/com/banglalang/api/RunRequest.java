// backend/src/main/java/com/banglalang/api/RunRequest.java
package com.banglalang.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/run - just the BanglaLang source code the
 * editor wants executed. @NotBlank / @Size are enforced by Spring's
 * validation starter before RunController's method body even runs, so
 * an empty or absurdly large payload never reaches the Lexer.
 */
public final class RunRequest {

    @NotBlank(message = "কোড খালি থাকতে পারবে না। (Source code cannot be empty.)")
    @Size(max = 20_000, message = "কোড অনেক বড়। (Source code is too large - max 20,000 characters.)")
    private String source;

    public RunRequest() {
        // required no-arg constructor for JSON deserialization
    }

    public RunRequest(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}