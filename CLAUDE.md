# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

**Build Commands:**

```bash
./mvnw clean spring-javaformat:apply compile                    # Compile application
./mvnw spring-javaformat:apply test                             # Run all tests
```

## Design Requirements
- **Package**: `am.ik.maven.nullability` - Main package

## Implemented Features

- Automatic configuration of ErrorProne and NullAway for nullability checking
- Lifecycle participant (`NullabilityLifecycleParticipant`) injects compiler configuration before build plan is computed
- No-op Mojo (`ConfigureMojo`) at `initialize` phase for plugin declaration and parameter documentation
- Configurable checking modes: MAIN, TESTS, DISABLED
- Support for `RequireExplicitNullMarking` check
- Spring Contract and AssertJ `@Contract` custom contract annotations
- Excludes test paths and generated sources by default
- Preserves existing `maven-compiler-plugin` configuration
- Integration tests via `maven-invoker-plugin`

## Development Requirements

### Prerequisites

- Java 17+ (for compiling the plugin itself)
- JDK 22+ (for running ITs, due to ErrorProne 2.43.0+ requiring JDK 21+ and NullAway JSpecify mode requiring JDK 22+)

### Code Standards

- No external dependencies except for testing libraries
- Use builder pattern if the number of arguments is more than two
- Write javadoc and comments in English
- Spring Java Format enforced via Maven plugin
- All code must pass formatting validation before commit
- Use Java 17 compatible features (avoid Java 21+ specific APIs)
- Use modern Java technics as much as possible like Java Records, Pattern Matching, Text Block etc ...
- Be sure to avoid circular references between classes and packages.

### Documentation

- Specify when this library should be used
- The explanations are written from the perspective of the API user, with plenty of code examples to make usage easy to understand.
- No need for excessive advertising
- There is no need to use emojis or flashy expressions, just write simply and honestly.

### Testing Strategy

- JUnit 5 with AssertJ
- All tests must pass before completing tasks
- Code examples in the README must be tested to ensure they work.

### After Task completion

- Ensure all code is formatted using `./mvnw spring-javaformat:apply`
- Run full test suite with `./mvnw verify`
- For every task, notify that the task is complete and ready for review by the following command:

```
osascript -e 'display notification "<Message Body>" with title "<Message Title>"’
```
