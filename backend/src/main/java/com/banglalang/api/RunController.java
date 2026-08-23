// backend/src/main/java/com/banglalang/api/RunController.java
package com.banglalang.api;

import com.banglalang.interpreter.Interpreter;
import com.banglalang.interpreter.RuntimeError;
import com.banglalang.lexer.Lexer;
import com.banglalang.lexer.Token;
import com.banglalang.parser.ast.ParseException;
import com.banglalang.parser.Parser;
import com.banglalang.parser.ast.Stmt;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The single endpoint the frontend editor talks to: POST /api/run.
 *
 * Runs the full pipeline - Lexer -> Parser -> Interpreter - and catches
 * each stage's own error type separately, so the message that comes
 * back always tells you which stage failed (a wrong character is a very
 * different problem from a wrong keyword order, which is different again
 * from a runtime type error) rather than one generic "something broke".
 *
 * CORS: no @CrossOrigin here - cross-origin access for local dev is
 * configured centrally in WebConfig (next file), so every controller
 * gets consistent CORS behavior instead of scattering @CrossOrigin
 * annotations across every endpoint we add later.
 */
@RestController
public class RunController {

    @PostMapping("/api/run")
    public RunResponse run(@Valid @RequestBody RunRequest request) {
        String source = request.getSource();

        List<Token> tokens;
        try {
            tokens = new Lexer(source).scanTokens();
        } catch (Lexer.LexError e) {
            return RunResponse.failure("", "লেক্সিং ত্রুটি (Lexing Error): " + e.getMessage());
        }

        List<Stmt> statements;
        try {
            statements = new Parser(tokens).parse();
        } catch (ParseException e) {
            return RunResponse.failure("", "পার্সিং ত্রুটি (Parsing Error): " + e.getMessage());
        }

        // Runtime errors are caught INSIDE Interpreter.interpret() itself
        // (so partial output before the crash point is preserved), but we
        // still guard here against any unexpected exception - e.g. a Java
        // StackOverflowError from unbounded recursion in a user's ফাংশন -
        // so the API never returns a raw 500 with a Java stack trace.
        try {
            Interpreter interpreter = new Interpreter();
            String output = interpreter.interpret(statements);

            boolean hadRuntimeError = output.contains("রানটাইম ত্রুটি");
            return hadRuntimeError ? RunResponse.failure(output, "Program stopped due to a runtime error.")
                                    : RunResponse.ok(output);
        } catch (RuntimeError e) {
            return RunResponse.failure("", "রানটাইম ত্রুটি (Runtime Error): " + e.getMessage());
        } catch (StackOverflowError e) {
            return RunResponse.failure("", "অসীম পুনরাবৃত্তি সনাক্ত হয়েছে (Infinite recursion detected).");
        }
    }
}