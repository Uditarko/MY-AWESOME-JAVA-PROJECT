package io.github.uditarko.memberreader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads {@link Member} records from CSV data.
 *
 * <p>The expected format is RFC 4180-compliant CSV with a header row:
 * <pre>
 * id,first_name,last_name,email,gender,ip_address
 * 1,John,Doe,john@example.com,Male,192.168.1.1
 * </pre>
 *
 * <p>Four entry points cover the most common input sources. All produce an
 * unmodifiable {@code List<Member>} and throw {@link MalformedCsvException}
 * (a subtype of {@link IOException}) when the input violates the expected format.
 * Streams and Readers passed to {@link #fromInputStream} / {@link #fromReader}
 * are <em>not</em> closed by this class — callers retain that responsibility.
 *
 * <p>The parser handles RFC 4180 quoted fields (fields that contain commas or
 * embedded quotes), so names like {@code "Smith, Jr."} parse correctly.
 *
 * <p>Usage example:
 * <pre>{@code
 * List<Member> members = MemberReader.fromPath(Path.of("members.csv"));
 *
 * members.stream()
 *     .filter(m -> "Female".equals(m.gender()))
 *     .map(m -> m.firstName() + " " + m.lastName())
 *     .forEach(System.out::println);
 * }</pre>
 */
public final class MemberReader {

    private static final String EXPECTED_HEADER = "id,first_name,last_name,email,gender,ip_address";
    private static final int EXPECTED_FIELD_COUNT = 6;

    private MemberReader() {}

    /**
     * Reads members from a file.
     *
     * @param path path to the CSV file; must be readable
     * @return unmodifiable list of members in file order
     * @throws MalformedCsvException if the CSV is structurally invalid
     * @throws IOException           if the file cannot be read
     */
    public static List<Member> fromPath(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return fromReader(reader);
        }
    }

    /**
     * Reads members from a UTF-8 encoded input stream.
     *
     * <p>The stream is not closed by this method.
     *
     * @param inputStream source stream; must not be null
     * @return unmodifiable list of members in stream order
     * @throws MalformedCsvException if the CSV is structurally invalid
     * @throws IOException           if the stream cannot be read
     */
    public static List<Member> fromInputStream(InputStream inputStream) throws IOException {
        return fromReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Reads members from a raw CSV string.
     *
     * @param csv full CSV content including the header row
     * @return unmodifiable list of members in document order
     * @throws MalformedCsvException if the CSV is structurally invalid
     * @throws IOException           propagated from underlying I/O (unlikely for string input)
     */
    public static List<Member> fromString(String csv) throws IOException {
        return fromReader(new StringReader(csv));
    }

    /**
     * Reads members from any {@link Reader}.
     *
     * <p>The reader is not closed by this method.
     *
     * @param reader source reader; must not be null
     * @return unmodifiable list of members in reader order
     * @throws MalformedCsvException if the CSV is structurally invalid
     * @throws IOException           if the reader cannot be read
     */
    public static List<Member> fromReader(Reader reader) throws IOException {
        List<Member> members = new ArrayList<>();
        BufferedReader br = reader instanceof BufferedReader b ? b : new BufferedReader(reader);

        String headerLine = br.readLine();
        if (headerLine == null) {
            return Collections.emptyList();
        }
        if (!EXPECTED_HEADER.equalsIgnoreCase(headerLine.strip())) {
            throw new MalformedCsvException(
                    "Unexpected header \"" + headerLine.strip() + "\""
                            + " — expected \"" + EXPECTED_HEADER + "\"", 1);
        }

        String line;
        int lineNumber = 2;
        while ((line = br.readLine()) != null) {
            if (line.isBlank()) {
                lineNumber++;
                continue;
            }
            List<String> fields = parseLine(line);
            if (fields.size() != EXPECTED_FIELD_COUNT) {
                throw new MalformedCsvException(
                        "Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.size(),
                        lineNumber);
            }
            try {
                members.add(new Member(
                        Integer.parseInt(fields.get(0).strip()),
                        fields.get(1).strip(),
                        fields.get(2).strip(),
                        fields.get(3).strip(),
                        fields.get(4).strip(),
                        fields.get(5).strip()));
            } catch (NumberFormatException e) {
                throw new MalformedCsvException(
                        "Invalid id value \"" + fields.get(0).strip() + "\"", lineNumber);
            }
            lineNumber++;
        }

        return Collections.unmodifiableList(members);
    }

    /**
     * Minimal RFC 4180 single-line parser.
     * Handles quoted fields that contain commas and doubled-quote escapes ({@code ""}).
     */
    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString());
                    field.setLength(0);
                } else {
                    field.append(c);
                }
            }
        }
        fields.add(field.toString());
        return fields;
    }
}
