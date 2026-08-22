// backend/src/main/java/com/banglalang/lexer/Token.java
package com.banglalang.lexer;

import java.util.Objects;

/**
 * A single token produced by the Lexer: its kind, the raw source text it
 * came from (lexeme), an optional resolved literal value (for numbers and
 * strings), and the line number it was found on (for error messages).
 *
 * Immutable by design - once the Lexer produces a Token it never changes;
 * the Parser only reads it.
 */
public final class Token {

    private final TokenType type;
    private final String lexeme;
    private final Object literal; // Double for NUMBER, String for STRING/IDENTIFIER, null otherwise
    private final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, '%s', %s, line=%d)", type, lexeme, literal, line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token other)) return false;
        return line == other.line
                && type == other.type
                && Objects.equals(lexeme, other.lexeme)
                && Objects.equals(literal, other.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, literal, line);
    }
}