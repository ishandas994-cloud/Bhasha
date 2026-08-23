// backend/src/main/java/com/banglalang/api/RunResponse.java
package com.banglalang.api;

/**
 * Response body for POST /api/run.
 *
 * Only one of (output) or (error) is meaningful for a given result:
 *  - success=true  -> output holds everything লিখ printed, error is null
 *  - success=false -> error holds a human-readable message (lex/parse/
 *                      runtime failure, already including a line number),
 *                      output holds whatever ran before the failure (may
 *                      be empty)
 *
 * Kept as a plain DTO (no Lombok) so it's obvious exactly what gets
 * serialized to JSON without needing to know an annotation-processing
 * library.
 */
public final class RunResponse {

    private boolean success;
    private String output;
    private String error;

    public RunResponse() {
        // required no-arg constructor for JSON serialization
    }

    private RunResponse(boolean success, String output, String error) {
        this.success = success;
        this.output = output;
        this.error = error;
    }

    public static RunResponse ok(String output) {
        return new RunResponse(true, output, null);
    }

    public static RunResponse failure(String partialOutput, String error) {
        return new RunResponse(false, partialOutput, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}