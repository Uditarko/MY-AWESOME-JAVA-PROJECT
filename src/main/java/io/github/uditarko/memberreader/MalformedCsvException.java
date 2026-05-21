package io.github.uditarko.memberreader;

import java.io.IOException;

/**
 * Thrown when CSV input does not conform to the expected member format.
 *
 * <p>The {@link #getLineNumber()} method returns the 1-based line number
 * where the parse failure occurred, making it easy to surface actionable
 * error messages to end users.
 *
 * <p>Example:
 * <pre>{@code
 * try {
 *     List<Member> members = MemberReader.fromPath(path);
 * } catch (MalformedCsvException e) {
 *     System.err.println("Parse failed at line " + e.getLineNumber() + ": " + e.getMessage());
 * }
 * }</pre>
 */
public class MalformedCsvException extends IOException {

    private final int lineNumber;

    /**
     * @param message    description of the parse failure
     * @param lineNumber 1-based line number where the error was detected
     */
    public MalformedCsvException(String message, int lineNumber) {
        super(message + " (line " + lineNumber + ")");
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the 1-based line number in the CSV source where the parse failure was detected.
     *
     * @return 1-based line number
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
