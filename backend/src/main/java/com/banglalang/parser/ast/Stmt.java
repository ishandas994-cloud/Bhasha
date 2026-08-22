// backend/src/main/java/com/banglalang/parser/ast/Stmt.java
package com.banglalang.parser.ast;

import com.banglalang.lexer.Token;
import java.util.List;

/**
 * Base type for every statement node in the AST. Mirrors Expr's visitor
 * pattern. Statements are things executed for effect (declare a variable,
 * run a loop, print) as opposed to Expr nodes, which evaluate to a value.
 */
public abstract class Stmt {

    public interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitFunctionStmt(Function stmt);
        R visitReturnStmt(Return stmt);
        R visitBreakStmt(Break stmt);
        R visitContinueStmt(Continue stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    /** A bare expression evaluated for its side effect, e.g. a call or assignment, followed by ';' */
    public static final class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    /** লিখ expr; */
    public static final class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    /** ধরি name = initializer;  (initializer may be null -> defaults to nil) */
    public static final class Var extends Stmt {
        public final Token name;
        public final Expr initializer;

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    /** { statements... } - introduces a new lexical scope */
    public static final class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    /** যদি (condition) thenBranch [নাহলে elseBranch] */
    public static final class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch; // nullable

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    /** যতক্ষণ (condition) body   - জন্য (for) desugars into this in the Parser */
    public static final class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    /** ফাংশন name(params...) { body } */
    public static final class Function extends Stmt {
        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    /** ফেরত [expr]; */
    public static final class Return extends Stmt {
        public final Token keyword;
        public final Expr value; // nullable

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    /** থামো; - exits the nearest enclosing loop */
    public static final class Break extends Stmt {
        public final Token keyword;

        public Break(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }
    }

    /** চালিয়ে_যাও; - skips to the next iteration of the nearest enclosing loop */
    public static final class Continue extends Stmt {
        public final Token keyword;

        public Continue(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStmt(this);
        }
    }
}