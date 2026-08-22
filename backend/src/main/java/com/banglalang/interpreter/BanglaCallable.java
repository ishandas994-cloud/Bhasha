// backend/src/main/java/com/banglalang/interpreter/BanglaCallable.java
package com.banglalang.interpreter;

import java.util.List;

/**
 * Anything that can be invoked with call-syntax `name(args...)` in
 * BanglaLang implements this - currently just user-defined functions
 * (ফাংশন), but it also gives us a clean seam to add native/built-in
 * functions later (e.g. a math library) without changing the Interpreter's
 * call-handling code at all.
 */
public interface BanglaCallable {

    /** Number of arguments this callable expects. */
    int arity();

    /** Executes the callable and returns its result (null if it returns nothing). */
    Object call(Interpreter interpreter, List<Object> arguments);
}