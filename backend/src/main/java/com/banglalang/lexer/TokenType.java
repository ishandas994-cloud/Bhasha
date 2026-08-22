// backend/src/main/java/com/banglalang/lexer/TokenType.java
package com.banglalang.lexer;

/**
 * Every distinct kind of token the Lexer can emit. Keeping this as a flat
 * enum (rather than raw strings) means the Parser can switch on token
 * kind safely and the compiler catches typos at compile time instead of
 * failing silently at runtime.
 */
public enum TokenType {

    // ---- Literals ----
    NUMBER,        // ৫, 5, ২৫.৫
    STRING,        // "কিছু লেখা"
    IDENTIFIER,    // variable / function names

    // ---- Keywords (Bengali) ----
    DHORI,         // ধরি   - let / variable declaration
    JODI,          // যদি   - if
    NAHOLE,        // নাহলে - else
    JOTOKKHON,     // যতক্ষণ - while
    JONNO,         // জন্য  - for
    FUNCTION,      // ফাংশন - function
    PHEROT,        // ফেরত  - return
    LIKH,          // লিখ   - print
    SHOTTO,        // সত্য   - true
    MITHYA,        // মিথ্যা - false
    THAMO,         // থামো  - break
    CHALIYE_JAO,   // চালিয়ে_যাও - continue

    // ---- Operators ----
    PLUS, MINUS, STAR, SLASH, PERCENT,
    ASSIGN,          // =
    EQUAL_EQUAL,     // ==
    NOT_EQUAL,       // !=
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,
    AND,             // &&
    OR,              // ||
    NOT,             // !

    // ---- Punctuation ----
    LEFT_PAREN, RIGHT_PAREN,   // ( )
    LEFT_BRACE, RIGHT_BRACE,   // { }
    SEMICOLON,                 // ;
    COMMA,                     // ,

    // ---- Control ----
    EOF
}