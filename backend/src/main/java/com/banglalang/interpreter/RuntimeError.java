// backend/src/main/java/com/banglalang/interpreter/RuntimeError.java
package com.banglalang.interpreter;

import com.banglalang.lexer.Token;

/**
 * Thrown for any error that only becomes apparent while the program is
 * actually running (undefined variable, wrong operand types, calling a
 * non-function, wrong argument count, etc.) - as opposed to LexError and
 * ParseException, which are caught before execution ever starts.
 *
 * Carries the offending Token so the API layer can report a line number
 * back to the editor, the same way parse errors do.
 */
public final class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " (line " + token.getLine() + ")";
    }
}