# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew build          # compile + test + javadoc (full build)
./gradlew test           # tests only
./gradlew test --tests "io.github.uditarko.memberreader.MemberReaderTest.fromString_parsesAllFields"  # single test
./gradlew javadoc        # generate Javadoc → build/docs/javadoc/
./gradlew publish        # publish to GitHub Packages (requires GITHUB_TOKEN)
```

## Architecture

This is a single-module Gradle library (`java-library` plugin). There is no application entry point — the artifact is a JAR consumed as a dependency.

**Source layout:**
- `src/main/java/io/github/uditarko/memberreader/` — the public API
- `src/test/java/io/github/uditarko/memberreader/` — JUnit 5 tests

**Public API (3 classes):**
- `Member` — Java record mapping the 6 CSV columns (`id`, `firstName`, `lastName`, `email`, `gender`, `ipAddress`)
- `MemberReader` — four static factory methods (`fromPath`, `fromInputStream`, `fromReader`, `fromString`); all return `Collections.unmodifiableList`
- `MalformedCsvException extends IOException` — thrown on structural CSV errors; exposes `getLineNumber()` for the 1-based line where parsing failed

**CSV parsing** is done inline in `MemberReader.parseLine()` — a minimal RFC 4180 state machine that handles quoted fields and `""` escape sequences. No external dependencies.

## Publishing

Group: `io.github.uditarko`, artifact: `member-csv-reader`. Published to GitHub Packages on every GitHub Release via `.github/workflows/publish.yml`. CI runs on every push/PR via `.github/workflows/build.yml`.

To publish locally, set `GITHUB_TOKEN` (or `gpr.key` in `~/.gradle/gradle.properties`) then run `./gradlew publish`.

## Java version

The library targets Java 21 bytecode (`sourceCompatibility`/`targetCompatibility = 21`). The Gradle wrapper uses Gradle 9.5.1. The local machine has Java 25 installed — that is fine since the build compiles down to Java 21 class files.
