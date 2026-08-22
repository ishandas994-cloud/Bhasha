// backend/src/main/java/com/banglalang/parser/Parser.java
package com.banglalang.parser;

import com.banglalang.lexer.Token;
import com.banglalang.lexer.TokenType;
import com.banglalang.parser.ast.Expr;
import com.banglalang.parser.ast.Stmt;

import java.util.ArrayList;
import java.util.List;

import static com.banglalang.lexer.TokenType.*;

/**
 * Recursive-descent parser for BanglaLang. Consumes the flat Token list
 * produced by Lexer and builds a list of Stmt (the program's AST).
 *
 * Grammar (highest to lowest precedence at the bottom, as is conventional):
 *
 *   program     -> declaration* EOF
 *   declaration -> funDecl | varDecl | statement
 *   funDecl     -> "ফাংশন" IDENTIFIER "(" parameters? ")" block
 *   varDecl     -> "ধরি" IDENTIFIER ( "=" expression )? ";"
 *   statement   -> exprStmt | printStmt | block | ifStmt | whileStmt
 *                | forStmt | returnStmt | breakStmt | continueStmt
 *   forStmt     -> "জন্য" "(" (varDecl | exprStmt | ";")
 *                          expression? ";" expression? ")" statement
 *   ifStmt      -> "যদি" "(" expression ")" statement ( "নাহলে" statement )?
 *   whileStmt   -> "যতক্ষণ" "(" expression ")" statement
 *   block       -> "{" declaration* "}"
 *   expression  -> assignment
 *   assignment  -> IDENTIFIER "=" assignment | logic_or
 *   logic_or    -> logic_and ( "||" logic_and )*
 *   logic_and   -> equality ( "&&" equality )*
 *   equality    -> comparison ( ( "==" | "!=" ) comparison )*
 *   comparison  -> term ( ( "<" | "<=" | ">" | ">=" ) term )*
 *   term        -> factor ( ( "+" | "-" ) factor )*
 *   factor      -> unary ( ( "*" | "/" | "%" ) unary )*
 *   unary       -> ( "!" | "-" ) unary | call
 *   call        -> primary ( "(" arguments? ")" )*
 *   primary     -> NUMBER | STRING | "সত্য" | "মিথ্যা"
 *                | "(" expression ")" | IDENTIFIER
 */
public final class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** Parses the whole token stream into a list of top-level statements. */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    // ---- Declarations ----

    private Stmt declaration() {
        if (check(FUNCTION)) return function();
        if (check(DHORI)) return varDeclaration();
        return statement();
    }

    private Stmt function() {
        advance(); // consume ফাংশন
        Token name = consume(IDENTIFIER, "Expected function name after 'ফাংশন'.");
        consume(LEFT_PAREN, "Expected '(' after function name.");

        List<Token> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                params.add(consume(IDENTIFIER, "Expected parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')' after parameters.");
        consume(LEFT_BRACE, "Expected '{' before function body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, params, body);
    }

    private Stmt varDeclaration() {
        advance(); // consume ধরি
        Token name = consume(IDENTIFIER, "Expected variable name after 'ধরি'.");

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    // ---- Statements ----

    private Stmt statement() {
        if (check(LIKH)) return printStatement();
        if (check(LEFT_BRACE)) return new Stmt.Block(block());
        if (check(JODI)) return ifStatement();
        if (check(JOTOKKHON)) return whileStatement();
        if (check(JONNO)) return forStatement();
        if (check(PHEROT)) return returnStatement();
        if (check(THAMO)) return breakStatement();
        if (check(CHALIYE_JAO)) return continueStatement();
        return expressionStatement();
    }

    private Stmt printStatement() {
        advance(); // consume লিখ
        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(value);
    }

    private List<Stmt> block() {
        advance(); // consume {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt ifStatement() {
        advance(); // consume যদি
        consume(LEFT_PAREN, "Expected '(' after 'যদি'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (check(NAHOLE)) {
            advance();
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        advance(); // consume যতক্ষণ
        consume(LEFT_PAREN, "Expected '(' after 'যতক্ষণ'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    /** জন্য (init; condition; increment) body - desugars into a Block containing
     *  the init statement followed by a While loop whose body appends the
     *  increment. This keeps the Interpreter from needing a separate For node. */
    private Stmt forStatement() {
        advance(); // consume জন্য
        consume(LEFT_PAREN, "Expected '(' after 'জন্য'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (check(DHORI)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = check(SEMICOLON) ? null : expression();
        consume(SEMICOLON, "Expected ';' after loop condition.");

        Expr increment = check(RIGHT_PAREN) ? null : expression();
        consume(RIGHT_PAREN, "Expected ')' after for clauses.");

        Stmt body = statement();

        if (increment != null) {
            List<Stmt> bodyAndIncrement = new ArrayList<>();
            bodyAndIncrement.add(body);
            bodyAndIncrement.add(new Stmt.Expression(increment));
            body = new Stmt.Block(bodyAndIncrement);
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            List<Stmt> initAndLoop = new ArrayList<>();
            initAndLoop.add(initializer);
            initAndLoop.add(body);
            body = new Stmt.Block(initAndLoop);
        }

        return body;
    }

    private Stmt returnStatement() {
        Token keyword = advance(); // consume ফেরত
        Expr value = check(SEMICOLON) ? null : expression();
        consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt breakStatement() {
        Token keyword = advance(); // consume থামো
        consume(SEMICOLON, "Expected ';' after 'থামো'.");
        return new Stmt.Break(keyword);
    }

    private Stmt continueStatement() {
        Token keyword = advance(); // consume চালিয়ে_যাও
        consume(SEMICOLON, "Expected ';' after 'চালিয়ে_যাও'.");
        return new Stmt.Continue(keyword);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // ---- Expressions (precedence climbing) ----

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable variable) {
                return new Expr.Assign(variable.name, value);
            }
            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(EQUAL_EQUAL, NOT_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(STAR, SLASH, PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(SHOTTO)) return new Expr.Literal(true);
        if (match(MITHYA)) return new Expr.Literal(false);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().getLiteral());
        if (match(IDENTIFIER)) return new Expr.Variable(previous());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected an expression.");
    }

    // ---- Token stream helpers ----

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseException error(Token token, String message) {
        String where = token.getType() == EOF ? "end of file" : "'" + token.getLexeme() + "'";
        return new ParseException(message + " (at " + where + ", line " + token.getLine() + ")");
    }

    /** Recovers after a parse error by skipping to the next likely statement boundary. */
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().getType() == SEMICOLON) return;
            switch (peek().getType()) {
                case FUNCTION, DHORI, JODI, JOTOKKHON, JONNO, LIKH, PHEROT -> {
                    return;
                }
                default -> advance();
            }
        }
    }
}