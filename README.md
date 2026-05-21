# member-csv-reader

A zero-dependency Java 21 library for reading member data from CSV files.

Parses RFC 4180-compliant CSV (quoted fields with embedded commas and escaped quotes are handled correctly) and exposes a typed `Member` record with full Javadoc.

## Installation

> **GitHub Packages requires authentication even for public packages.** Add your credentials once (see step 1), then add the dependency normally.

### Step 1 — configure credentials

Add to `~/.gradle/gradle.properties` (never commit this file):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Generate a token at **GitHub → Settings → Developer settings → Personal access tokens** with the `read:packages` scope.

### Step 2 — add the repository

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/uditarko/MY-AWESOME-JAVA-PROJECT")
        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.key") as String?
        }
    }
}
```

**Gradle (Groovy DSL)**
```groovy
repositories {
    maven {
        url 'https://maven.pkg.github.com/uditarko/MY-AWESOME-JAVA-PROJECT'
        credentials {
            username project.findProperty('gpr.user')
            password project.findProperty('gpr.key')
        }
    }
}
```

**Maven**
```xml
<repository>
  <id>github</id>
  <url>https://maven.pkg.github.com/uditarko/MY-AWESOME-JAVA-PROJECT</url>
</repository>
```
Add credentials to `~/.m2/settings.xml` using the `github` server id.

### Step 3 — add the dependency

**Gradle**
```kotlin
implementation("io.github.uditarko:member-csv-reader:1.0.0")
```

**Maven**
```xml
<dependency>
  <groupId>io.github.uditarko</groupId>
  <artifactId>member-csv-reader</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

```java
import io.github.uditarko.memberreader.Member;
import io.github.uditarko.memberreader.MemberReader;
import io.github.uditarko.memberreader.MalformedCsvException;

// Read from a file
List<Member> members = MemberReader.fromPath(Path.of("members.csv"));

// Stream, filter, and transform
members.stream()
    .filter(m -> "Female".equals(m.gender()))
    .map(m -> m.firstName() + " " + m.lastName())
    .forEach(System.out::println);

// Error handling with line-level detail
try {
    List<Member> members = MemberReader.fromPath(path);
} catch (MalformedCsvException e) {
    System.err.println("Bad CSV at line " + e.getLineNumber() + ": " + e.getMessage());
} catch (IOException e) {
    System.err.println("Could not read file: " + e.getMessage());
}
```

## API

### `MemberReader`

All methods return an **unmodifiable** `List<Member>` in document order.
Streams and Readers are **not** closed by the library — callers retain that responsibility.

| Method | Description |
|---|---|
| `fromPath(Path)` | Read from a file path |
| `fromInputStream(InputStream)` | Read from a UTF-8 encoded stream |
| `fromReader(Reader)` | Read from any `Reader` |
| `fromString(String)` | Read from a raw CSV string |

All four throw `MalformedCsvException` (a subtype of `IOException`) when the CSV is structurally invalid, and plain `IOException` when the source cannot be read.

### `Member`

Java record — all fields are non-null strings (except `id` which is an `int`).

| Accessor | Type | CSV column |
|---|---|---|
| `id()` | `int` | `id` |
| `firstName()` | `String` | `first_name` |
| `lastName()` | `String` | `last_name` |
| `email()` | `String` | `email` |
| `gender()` | `String` | `gender` |
| `ipAddress()` | `String` | `ip_address` |

### `MalformedCsvException`

Extends `IOException`. Use `getLineNumber()` to get the 1-based line number where the parse failure occurred.

## Expected CSV format

```
id,first_name,last_name,email,gender,ip_address
1,John,Doe,john@example.com,Male,192.168.1.1
2,"Smith, Jr.",Jane,jane@example.com,Female,10.0.0.1
```

The header row is required and validated. Blank lines between data rows are ignored.

## Building from source

```bash
git clone https://github.com/uditarko/MY-AWESOME-JAVA-PROJECT.git
cd MY-AWESOME-JAVA-PROJECT
./gradlew build        # compile + test + javadoc
./gradlew test         # tests only
./gradlew javadoc      # generate docs → build/docs/javadoc/
```

## Publishing a new release

1. Bump `version` in `build.gradle.kts`
2. Commit and push
3. Create a GitHub Release — the `publish` workflow runs automatically and uploads the JAR, sources JAR, and Javadoc JAR to GitHub Packages
