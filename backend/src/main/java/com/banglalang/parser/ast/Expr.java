// backend/src/main/java/com/banglalang/parser/ast/Expr.java
package com.banglalang.parser.ast;

import com.banglalang.lexer.Token;
import java.util.List;

/**
 * Base type for every expression node in the AST. Uses the classic
 * visitor pattern (as in "Crafting Interpreters") rather than Java 17
 * sealed-interface pattern matching, so the Interpreter can implement
 * one Visitor<Object> and get exhaustiveness checked by the compiler
 * (every visit method must be implemented) without needing preview
 * language features.
 */
public abstract class Expr {

    public interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitUnaryExpr(Unary expr);
        R visitLiteralExpr(Literal expr);
        R visitGroupingExpr(Grouping expr);
        R visitVariableExpr(Variable expr);
        R visitAssignExpr(Assign expr);
        R visitLogicalExpr(Logical expr);
        R visitCallExpr(Call expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    /** left OPERATOR right, e.g. a + b, a == b */
    public static final class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    /** OPERATOR right, e.g. -a, !flag */
    public static final class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    /** A raw literal value: number, string, true/false */
    public static final class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    /** ( expr ) - parenthesized sub-expression */
    public static final class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    /** Reference to a variable by name */
    public static final class Variable extends Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    /** name = value */
    public static final class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    /** left && right / left || right - kept separate from Binary so the
     *  interpreter can short-circuit evaluation */
    public static final class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    /** callee(arg1, arg2, ...) - function invocation */
    public static final class Call extends Expr {
        public final Expr callee;
        public final Token paren; // closing ')' - used for error line reporting
        public final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }
}