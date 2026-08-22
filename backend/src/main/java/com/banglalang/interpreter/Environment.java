// backend/src/main/java/com/banglalang/interpreter/Environment.java
package com.banglalang.interpreter;

import com.banglalang.lexer.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * A single lexical scope: a map of variable name -> value, plus a link
 * to the enclosing scope it's nested inside (null for the global scope).
 *
 * Every block ({ ... }), function call, and loop iteration in BanglaLang
 * gets its own Environment chained to the one it's defined in, which is
 * what gives the language proper block scoping and closures.
 */
public final class Environment {

    private final Environment enclosing; // null for the global scope
    private final Map<String, Object> values = new HashMap<>();

    /** Creates the top-level global scope. */
    public Environment() {
        this.enclosing = null;
    }

    /** Creates a new scope nested inside {@code enclosing}. */
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /** Declares a new variable in THIS scope (used by ধরি). Shadows an outer variable of the same name. */
    public void define(String name, Object value) {
        values.put(name, value);
    }

    /** Looks up a variable's value, walking outward through enclosing scopes if not found here. */
    public Object get(Token name) {
        if (values.containsKey(name.getLexeme())) {
            return values.get(name.getLexeme());
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'.");
    }

    /** Assigns to an EXISTING variable (does not create a new one). Walks outward like get(). */
    public void assign(Token name, Object value) {
        if (values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "'. Declare it first with 'ধরি'.");
    }
}
