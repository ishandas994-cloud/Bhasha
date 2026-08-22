// backend/src/main/java/com/banglalang/parser/ParseException.java
package com.banglalang.parser;

/**
 * Thrown when the Parser encounters a token sequence that doesn't match
 * BanglaLang's grammar. Carries a human-readable message (including line
 * number) so the API layer can surface it directly in the editor's
 * output panel instead of a raw stack trace.
 */
public final class ParseException extends RuntimeException {
    public ParseException(String message) {
        super(message);
    }
}