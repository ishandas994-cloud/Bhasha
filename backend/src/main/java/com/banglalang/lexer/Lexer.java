// backend/src/main/java/com/banglalang/lexer/Lexer.java
package com.banglalang.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hand-written scanner for BanglaLang source code. Walks the source
 * string one character at a time and produces a flat list of Tokens
 * for the Parser to consume.
 *
 * Handles:
 *  - Bengali-script keywords (ধরি, যদি, নাহলে, ...)
 *  - Identifiers made of Bengali script (U+0980-U+09FF) and/or ASCII
 *  - Bengali numerals (০-৯) as well as ASCII digits, including decimals
 *  - String literals with basic escape sequences
 *  - Line comments starting with '#'
 *  - Single- and multi-character operators
 */
public final class Lexer {

    // Bengali keyword text -> token type. This table is the Java-side
    // twin of KEYWORDS in the frontend's banglaLanguageDef.js - keep
    // them in sync if the language grows.
    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("ধরি", TokenType.DHORI),
            Map.entry("যদি", TokenType.JODI),
            Map.entry("নাহলে", TokenType.NAHOLE),
            Map.entry("যতক্ষণ", TokenType.JOTOKKHON),
            Map.entry("জন্য", TokenType.JONNO),
            Map.entry("ফাংশন", TokenType.FUNCTION),
            Map.entry("ফেরত", TokenType.PHEROT),
            Map.entry("লিখ", TokenType.LIKH),
            Map.entry("সত্য", TokenType.SHOTTO),
            Map.entry("মিথ্যা", TokenType.MITHYA),
            Map.entry("থামো", TokenType.THAMO),
            Map.entry("চালিয়ে_যাও", TokenType.CHALIYE_JAO)
    );

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;   // start of the token currently being scanned
    private int current = 0; // current cursor position
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    /** Scans the whole source and returns the resulting token list, ending in EOF. */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case ' ', '\r', '\t' -> { /* ignore whitespace */ }
            case '\n' -> line++;

            case '#' -> {
                // Line comment - skip to end of line
                while (peek() != '\n' && !isAtEnd()) advance();
            }

            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ',' -> addToken(TokenType.COMMA);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);
            case '%' -> addToken(TokenType.PERCENT);

            case '/' -> addToken(TokenType.SLASH);

            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.ASSIGN);
            case '!' -> addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);

            case '&' -> {
                if (match('&')) addToken(TokenType.AND);
                else throw error("Unexpected character '&' (did you mean '&&'?)");
            }
            case '|' -> {
                if (match('|')) addToken(TokenType.OR);
                else throw error("Unexpected character '|' (did you mean '||'?)");
            }

            case '"' -> scanString();

            default -> {
                if (isDigit(c)) {
                    scanNumber();
                } else if (isIdentifierStart(c)) {
                    scanIdentifierOrKeyword();
                } else {
                    throw error("Unexpected character: '" + c + "'");
                }
            }
        }
    }

    // ---- Literal scanners ----

    private void scanString() {
        StringBuilder value = new StringBuilder();
        while (peek() != '"' && !isAtEnd()) {
            char c = advance();
            if (c == '\n') line++;
            if (c == '\\' && !isAtEnd()) {
                char next = advance();
                value.append(switch (next) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> next;
                });
            } else {
                value.append(c);
            }
        }

        if (isAtEnd()) {
            throw error("Unterminated string literal");
        }

        advance(); // closing "
        addToken(TokenType.STRING, value.toString());
    }

    private void scanNumber() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        }

        String raw = source.substring(start, current);
        double value = Double.parseDouble(toAsciiDigits(raw));
        addToken(TokenType.NUMBER, value);
    }

    private void scanIdentifierOrKeyword() {
        while (isIdentifierPart(peek())) advance();

        String text = source.substring(start, current);
        TokenType keywordType = KEYWORDS.get(text);

        if (keywordType != null) {
            addToken(keywordType);
        } else {
            addToken(TokenType.IDENTIFIER, text);
        }
    }

    // ---- Character classification (Bengali-aware) ----

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isBengaliDigit(char c) {
        return c >= '\u09E6' && c <= '\u09EF'; // ০-৯
    }

    private static boolean isDigit(char c) {
        return isAsciiDigit(c) || isBengaliDigit(c);
    }

    private static boolean isBengaliScript(char c) {
        return c >= '\u0980' && c <= '\u09FF';
    }

    private static boolean isAsciiAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isIdentifierStart(char c) {
        return isAsciiAlpha(c) || isBengaliScript(c);
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || isDigit(c);
    }

    /** Converts Bengali numeral digits within a numeric literal to ASCII so Double.parseDouble works. */
    private static String toAsciiDigits(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (char c : raw.toCharArray()) {
            if (isBengaliDigit(c)) {
                sb.append((char) ('0' + (c - '\u09E6')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---- Cursor helpers ----

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private LexError error(String message) {
        return new LexError(message + " (line " + line + ")");
    }

    /** Thrown for any character sequence the lexer cannot tokenize. */
    public static final class LexError extends RuntimeException {
        public LexError(String message) {
            super(message);
        }
    }
}