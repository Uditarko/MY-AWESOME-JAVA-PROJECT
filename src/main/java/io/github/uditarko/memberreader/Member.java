package io.github.uditarko.memberreader;

/**
 * Immutable representation of a single row in a member CSV file.
 *
 * <p>Fields map directly to the CSV columns:
 * {@code id, first_name, last_name, email, gender, ip_address}.
 *
 * <p>Instances are produced by {@link MemberReader} and are safe to use across threads.
 *
 * @param id        unique numeric identifier
 * @param firstName member's first name
 * @param lastName  member's last name
 * @param email     member's email address
 * @param gender    member's self-reported gender
 * @param ipAddress IP address recorded at registration time
 */
public record Member(
        int id,
        String firstName,
        String lastName,
        String email,
        String gender,
        String ipAddress) {
}
