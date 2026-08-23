// backend/src/main/java/com/banglalang/cli/CliMain.java
package com.banglalang.cli;

import com.banglalang.interpreter.Interpreter;
import com.banglalang.lexer.Lexer;
import com.banglalang.lexer.Token;
import com.banglalang.parser.Parser;
import com.banglalang.parser.ast.ParseException;
import com.banglalang.parser.ast.Stmt;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command-line runner for BanglaLang - the "gcc" of this language.
 *
 * Usage:
 *   bangla run program.bangla     execute a source file
 *   bangla --help                 show usage
 *   bangla --version              show version
 *
 * Deliberately depends on NOTHING outside the JDK: only the lexer,
 * parser, and interpreter packages are packaged into the CLI binary,
 * so it works as a plain java -jar or a jpackaged native launcher.
 *
 * Exit codes follow Unix convention:
 *   0 - program ran to completion (even if the *program* had a runtime
 *       error? no - runtime errors exit 70 per sysexits.h convention,
 *       because scripts calling bangla need a reliable success signal)
 *   64 - bad command-line usage
 *   66 - input file not found / unreadable
 *   65 - lexing or parsing failure in the user's program
 *   70 - runtime error while executing the user's program
 */
public final class CliMain {

    public static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        // Force UTF-8 on the console streams. On Windows, System.out
        // defaults to the legacy console codepage and every Bengali glyph
        // would print as '?'. Re-wrapping the raw stdout/stderr file
        // descriptors with UTF-8 PrintStreams makes output correct on any
        // terminal without users needing -Dfile.encoding flags.
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            printUsage(args.length == 0);
            System.exit(args.length == 0 ? 64 : 0);
        }

        if (args[0].equals("--version") || args[0].equals("-v")) {
            System.out.println("BanglaLang " + VERSION);
            System.exit(0);
        }

        if (!args[0].equals("run")) {
            System.err.println("বাংলা ল্যাং: অজানা কমান্ড '" + args[0] + "' (unknown command)");
            printUsage(true);
            System.exit(64);
        }

        if (args.length < 2) {
            System.err.println("বাংলা ল্যাং: 'run' এর পরে একটি ফাইলের নাম দিন (expected a file after 'run')");
            System.exit(64);
        }

        System.exit(runFile(Path.of(args[1])));
    }

    private static int runFile(Path path) {
        String source;
        try {
            source = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("বাংলা ল্যাং: ফাইল পড়া যায়নি '" + path + "' (" + e.getMessage() + ")");
            return 66;
        }

        // ---- Stage 1: Lexing ----
        List<Token> tokens;
        try {
            tokens = new Lexer(source).scanTokens();
        } catch (Lexer.LexError e) {
            System.err.println("লেক্সিং ত্রুটি (Lexing Error): " + e.getMessage());
            return 65;
        }

        // ---- Stage 2: Parsing ----
        List<Stmt> statements;
        try {
            statements = new Parser(tokens).parse();
        } catch (ParseException e) {
            System.err.println("পার্সিং ত্রুটি (Parsing Error): " + e.getMessage());
            return 65;
        }

        // ---- Stage 3: Interpretation ----
        // interpret() deliberately does NOT throw on user-program runtime
        // errors - it appends a "রানটাইম ত্রুটি..." tag to the returned
        // output so partial results survive (same contract the REST API
        // relies on). We print everything it produced, then map the tag
        // to an exit code so shell scripts can detect failure.
        String output;
        try {
            output = new Interpreter().interpret(statements);
        } catch (StackOverflowError e) {
            System.err.println("অসীম পুনরাবৃত্তি সনাক্ত হয়েছে (Infinite recursion detected).");
            return 70;
        }

        System.out.print(output);
        return output.contains("রানটাইম ত্রুটি") ? 70 : 0;
    }

    private static void printUsage(boolean asError) {
        String text = """
                বাংলা ল্যাং — BanglaLang %s

                ব্যবহার (Usage):
                  bangla run <file.bangla>    বাংলা প্রোগ্রাম চালান (execute a program)
                  bangla --version            সংস্করণ দেখান (show version)
                  bangla --help               এই বার্তা দেখান (show this help)
                """.formatted(VERSION);
        (asError ? System.err : System.out).print(text);
    }
}
