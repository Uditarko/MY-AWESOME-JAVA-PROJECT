package io.github.uditarko.memberreader;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemberReaderTest {

    private static final String VALID_CSV = """
            id,first_name,last_name,email,gender,ip_address
            1,John,Doe,john@example.com,Male,192.168.1.1
            2,Jane,Smith,jane@example.com,Female,10.0.0.1
            """;

    // --- happy path ---

    @Test
    void fromString_parsesAllFields() throws IOException {
        List<Member> members = MemberReader.fromString(VALID_CSV);

        assertEquals(2, members.size());
        Member first = members.get(0);
        assertEquals(1, first.id());
        assertEquals("John", first.firstName());
        assertEquals("Doe", first.lastName());
        assertEquals("john@example.com", first.email());
        assertEquals("Male", first.gender());
        assertEquals("192.168.1.1", first.ipAddress());
    }

    @Test
    void fromString_returnsAllRows() throws IOException {
        List<Member> members = MemberReader.fromString(VALID_CSV);
        assertEquals(2, members.size());
        assertEquals(2, members.get(1).id());
        assertEquals("Jane", members.get(1).firstName());
    }

    @Test
    void fromString_blankLinesAreSkipped() throws IOException {
        String csv = """
                id,first_name,last_name,email,gender,ip_address

                1,A,B,a@b.com,Male,1.2.3.4

                """;
        assertEquals(1, MemberReader.fromString(csv).size());
    }

    @Test
    void fromString_quotedFieldWithEmbeddedComma_parsesCorrectly() throws IOException {
        String csv = """
                id,first_name,last_name,email,gender,ip_address
                1,"Smith, Jr.",Doe,a@b.com,Male,1.2.3.4
                """;
        List<Member> members = MemberReader.fromString(csv);
        assertEquals("Smith, Jr.", members.get(0).firstName());
    }

    @Test
    void fromString_quotedFieldWithEscapedQuote_parsesCorrectly() throws IOException {
        String csv = """
                id,first_name,last_name,email,gender,ip_address
                1,"O""Brien",Doe,a@b.com,Male,1.2.3.4
                """;
        assertEquals("O\"Brien", MemberReader.fromString(csv).get(0).firstName());
    }

    @Test
    void fromString_emptyBody_returnsEmptyList() throws IOException {
        String csv = "id,first_name,last_name,email,gender,ip_address\n";
        assertTrue(MemberReader.fromString(csv).isEmpty());
    }

    @Test
    void fromString_emptyInput_returnsEmptyList() throws IOException {
        assertTrue(MemberReader.fromString("").isEmpty());
    }

    @Test
    void fromPath_readsFile() throws IOException {
        Path tmp = Files.createTempFile("members", ".csv");
        try {
            Files.writeString(tmp, VALID_CSV);
            assertEquals(2, MemberReader.fromPath(tmp).size());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void fromInputStream_readsStream() throws IOException {
        byte[] bytes = VALID_CSV.getBytes(StandardCharsets.UTF_8);
        List<Member> members = MemberReader.fromInputStream(new ByteArrayInputStream(bytes));
        assertEquals(2, members.size());
    }

    @Test
    void returnedList_isUnmodifiable() throws IOException {
        List<Member> members = MemberReader.fromString(VALID_CSV);
        assertThrows(UnsupportedOperationException.class, () -> members.add(null));
    }

    // --- error cases ---

    @Test
    void fromString_wrongHeader_throwsMalformedCsvException() {
        String csv = "wrong,header\n1,A,B,a@b.com,Male,1.2.3.4\n";
        MalformedCsvException ex = assertThrows(MalformedCsvException.class,
                () -> MemberReader.fromString(csv));
        assertEquals(1, ex.getLineNumber());
    }

    @Test
    void fromString_tooFewFields_throwsMalformedCsvException() {
        String csv = "id,first_name,last_name,email,gender,ip_address\n1,John,Doe\n";
        MalformedCsvException ex = assertThrows(MalformedCsvException.class,
                () -> MemberReader.fromString(csv));
        assertEquals(2, ex.getLineNumber());
    }

    @Test
    void fromString_invalidId_throwsMalformedCsvException() {
        String csv = "id,first_name,last_name,email,gender,ip_address\nnot-a-number,A,B,a@b.com,Male,1.2.3.4\n";
        MalformedCsvException ex = assertThrows(MalformedCsvException.class,
                () -> MemberReader.fromString(csv));
        assertEquals(2, ex.getLineNumber());
    }

    @Test
    void malformedCsvException_messageContainsLineNumber() {
        String csv = "id,first_name,last_name,email,gender,ip_address\n1,John,Doe\n";
        MalformedCsvException ex = assertThrows(MalformedCsvException.class,
                () -> MemberReader.fromString(csv));
        assertTrue(ex.getMessage().contains("2"), "message should reference line number 2");
    }
}
