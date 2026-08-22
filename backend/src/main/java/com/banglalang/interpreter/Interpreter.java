// backend/src/main/java/com/banglalang/interpreter/Interpreter.java
package com.banglalang.interpreter;

import com.banglalang.lexer.Token;
import com.banglalang.lexer.TokenType;
import com.banglalang.parser.ast.Expr;
import com.banglalang.parser.ast.Stmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree-walking interpreter. Implements the visitor interfaces from Expr
 * and Stmt, so evaluating the program is just calling accept() on each
 * top-level statement and letting the visit* methods recurse.
 *
 * Output (from লিখ) is accumulated into a StringBuilder rather than
 * printed to System.out, since this runs inside a web request - the
 * caller (RunController) reads getOutput() after interpret() returns
 * and sends it back to the browser as JSON.
 */
public final class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private final Environment globals = new Environment();
    private Environment environment = globals;
    private final StringBuilder output = new StringBuilder();

    /**
     * Runs a whole program. Catches RuntimeError so one bad statement
     * doesn't crash the API request - the error message (with line
     * number) is appended to the output instead.
     */
    public String interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            output.append("রানটাইম ত্রুটি (Runtime Error): ").append(error.getMessage()).append("\n");
        }
        return output.toString();
    }

    // ---- Stmt.Visitor ----

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        output.append(stringify(value)).append("\n");
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = stmt.initializer != null ? evaluate(stmt.initializer) : null;
        environment.define(stmt.name.getLexeme(), value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakSignal signal) {
                break;
            } catch (ContinueSignal signal) {
                // continue just skips the rest of this iteration's body;
                // the while loop's own condition re-check handles the rest
            }
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        BanglaFunction function = new BanglaFunction(stmt, environment);
        environment.define(stmt.name.getLexeme(), function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = stmt.value != null ? evaluate(stmt.value) : null;
        throw new ReturnSignal(value);
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakSignal();
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        throw new ContinueSignal();
    }

    // ---- Expr.Visitor ----

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        // Short-circuit: don't evaluate the right side unless necessary
        if (expr.operator.getType() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else { // AND
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.getType()) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            case NOT -> !isTruthy(right);
            default -> null; // unreachable
        };
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        Token op = expr.operator;

        return switch (op.getType()) {
            case PLUS -> {
                if (left instanceof Double l && right instanceof Double r) yield l + r;
                if (left instanceof String || right instanceof String) yield stringify(left) + stringify(right);
                throw new RuntimeError(op, "Operands to '+' must both be numbers, or at least one must be a string.");
            }
            case MINUS -> {
                checkNumberOperands(op, left, right);
                yield (double) left - (double) right;
            }
            case STAR -> {
                checkNumberOperands(op, left, right);
                yield (double) left * (double) right;
            }
            case SLASH -> {
                checkNumberOperands(op, left, right);
                if ((double) right == 0.0) throw new RuntimeError(op, "Division by zero.");
                yield (double) left / (double) right;
            }
            case PERCENT -> {
                checkNumberOperands(op, left, right);
                if ((double) right == 0.0) throw new RuntimeError(op, "Division by zero.");
                yield (double) left % (double) right;
            }
            case GREATER -> {
                checkNumberOperands(op, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(op, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield (double) left <= (double) right;
            }
            case EQUAL_EQUAL -> isEqual(left, right);
            case NOT_EQUAL -> !isEqual(left, right);
            default -> null; // unreachable
        };
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof BanglaCallable function)) {
            throw new RuntimeError(expr.paren, "Can only call functions.");
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " argument(s) but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    // ---- Execution helpers ----

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /** Runs a list of statements inside a given scope, always restoring the previous scope afterward. */
    void executeBlock(List<Stmt> statements, Environment blockEnvironment) {
        Environment previous = this.environment;
        try {
            this.environment = blockEnvironment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    // ---- Value semantics ----

    /** Everything is truthy except `false` and `null` (uninitialized vars are null). */
    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /** Converts any runtime value to its printed representation for লিখ. */
    private String stringify(Object value) {
        if (value == null) return "নাল";
        if (value instanceof Boolean b) return b ? "সত্য" : "মিথ্যা";
        if (value instanceof Double d) {
            if (d == Math.floor(d) && !d.isInfinite()) {
                return String.valueOf(d.longValue());
            }
            return String.valueOf(d);
        }
        return value.toString();
    }

    // ---- Control-flow signals (internal, never escape interpret()) ----

    /** Unwinds the call stack back to the enclosing BanglaFunction.call(), carrying the return value. */
    static final class ReturnSignal extends RuntimeException {
        final Object value;

        ReturnSignal(Object value) {
            super(null, null, false, false); // skip stack trace capture - this is control flow, not an error
            this.value = value;
        }
    }

    /** Unwinds to the nearest enclosing loop and stops it. */
    static final class BreakSignal extends RuntimeException {
        BreakSignal() {
            super(null, null, false, false);
        }
    }

    /** Unwinds to the nearest enclosing loop and skips to its next iteration. */
    static final class ContinueSignal extends RuntimeException {
        ContinueSignal() {
            super(null, null, false, false);
        }
    }

    // ---- User-defined functions ----

    /** A ফাংশন declaration turned into something callable, closing over the environment it was defined in. */
    final class BanglaFunction implements BanglaCallable {
        private final Stmt.Function declaration;
        private final Environment closure;

        BanglaFunction(Stmt.Function declaration, Environment closure) {
            this.declaration = declaration;
            this.closure = closure;
        }

        @Override
        public int arity() {
            return declaration.params.size();
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Environment callEnvironment = new Environment(closure);
            for (int i = 0; i < declaration.params.size(); i++) {
                callEnvironment.define(declaration.params.get(i).getLexeme(), arguments.get(i));
            }

            try {
                interpreter.executeBlock(declaration.body, callEnvironment);
            } catch (ReturnSignal returnSignal) {
                return returnSignal.value;
            }

            return null; // functions with no explicit ফেরত return null
        }

        @Override
        public String toString() {
            return "<ফাংশন " + declaration.name.getLexeme() + ">";
        }
    }
}