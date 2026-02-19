# Nullability Maven Plugin

A Maven plugin that configures [ErrorProne](https://errorprone.info/) and [NullAway](https://github.com/uber/NullAway) for nullability checking, inspired by [spring-gradle-plugins/nullability-plugin](https://github.com/spring-gradle-plugins/nullability-plugin). It replaces the ~40 lines of `maven-compiler-plugin` boilerplate typically required to set up NullAway with a single plugin declaration.

## When to use this plugin

If you are using JSpecify's `@NullMarked` / `@Nullable` annotations and want NullAway to enforce them at compile time, this plugin handles all the configuration automatically:

- Adds ErrorProne and NullAway as annotation processor paths
- Sets the required compiler arguments (`-Xplugin:ErrorProne`, `-XDcompilePolicy=simple`, etc.)
- Adds the JVM module flags needed by ErrorProne (`--add-exports`, `--add-opens`)
- Configures `fork=true` on `maven-compiler-plugin`
- Excludes test sources and generated sources from checking by default

## Usage

Add the plugin to your `pom.xml`. If you are using Maven 3.8.x, you must also declare `maven-compiler-plugin` 3.5+ (see [Requirements](#requirements)):

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.15.0</version>
</plugin>
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>nullability-maven-plugin</artifactId>
    <version>0.2.0</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <goals>
                <goal>configure</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

You also need the JSpecify dependency:

```xml
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then annotate your packages (`package-info.java`) or classes with `@NullMarked`:

```java
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MyService {

    private final String name;

    public MyService(String name) {
        this.name = name;
    }

    public String greet() {
        return "Hello, " + this.name + "!";
    }

}
```

NullAway will report an error if you try to return `null` from a method in a `@NullMarked` context without declaring the return type as `@Nullable`.

### Generating `package-info.java` automatically

By placing `@NullMarked` in a `package-info.java` file, the annotation applies to the entire package, so you don't need to annotate each class individually. However, `@NullMarked` in `package-info.java` does not apply to sub-packages -- you need a `package-info.java` in every sub-package as well, which can be tedious to maintain by hand. The `generate-package-info` goal auto-generates these files during the build:

```xml
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>nullability-maven-plugin</artifactId>
    <version>0.2.0</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <goals>
                <goal>configure</goal>
                <goal>generate-package-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The goal scans your source directories and generates `@NullMarked` annotated `package-info.java` files for any package that doesn't already have one. Generated files are placed in `target/generated-sources/nullability` by default and added as a compile source root automatically.

If `checking` is set to `tests`, test source directories are also processed and generated files are placed in `target/generated-test-sources/nullability`.

Existing `package-info.java` files are never overwritten. The goal is idempotent: running it multiple times produces the same result.

You can also generate `package-info.java` files directly in your source tree by configuring the output directories:

```xml
<configuration>
    <checking>tests</checking>
    <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
    <testOutputDirectory>${project.basedir}/src/test/java</testOutputDirectory>
</configuration>
```

See [`generate-package-info` goal configuration](#generate-package-info-goal-configuration) for all available parameters.

## Configuration

All configuration parameters can be set either in the plugin `<configuration>` block or as Maven properties.

| Parameter                    | Property                                 | Default                          | Description                                                            |
|------------------------------|------------------------------------------|----------------------------------|------------------------------------------------------------------------|
| `errorProneVersion`          | `nullability.errorProneVersion`          | (managed)                        | ErrorProne version                                                     |
| `nullAwayVersion`            | `nullability.nullAwayVersion`            | (managed)                        | NullAway version                                                       |
| `checking`                   | `nullability.checking`                   | `main`                           | Checking mode: `main`, `tests`, or `disabled`                          |
| `requireExplicitNullMarking` | `nullability.requireExplicitNullMarking` | `true`                           | Enable `RequireExplicitNullMarking` check                              |
| `springContractSupport`      | `nullability.springContractSupport`      | `true`                           | Add `org.springframework.lang.Contract` to custom contract annotations |
| `jspecifyMode`               | `nullability.jspecifyMode`               | `true`                           | Enable NullAway's JSpecify mode (requires JDK 22+)                     |
| `excludedPaths`              | `nullability.excludedPaths`              | `.*/target/generated-sources/.*` | Regex pattern for paths to exclude                                     |
| `skip`                       | `nullability.skip`                       | `false`                          | Skip the plugin                                                        |

### `generate-package-info` goal configuration

The `generate-package-info` goal accepts the following additional parameters:

| Parameter             | Property                                              | Default                                                         | Description                                             |
|-----------------------|-------------------------------------------------------|-----------------------------------------------------------------|---------------------------------------------------------|
| `outputDirectory`     | `nullability.generatePackageInfo.outputDirectory`     | `${project.build.directory}/generated-sources/nullability`      | Output directory for generated main `package-info.java` |
| `testOutputDirectory` | `nullability.generatePackageInfo.testOutputDirectory`  | `${project.build.directory}/generated-test-sources/nullability`  | Output directory for generated test `package-info.java` |

The `checking` and `skip` parameters from the table above also apply to this goal.

### Example: Custom configuration

```xml
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>nullability-maven-plugin</artifactId>
    <version>0.2.0</version>
    <extensions>true</extensions>
    <configuration>
        <requireExplicitNullMarking>false</requireExplicitNullMarking>
        <checking>tests</checking>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>configure</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Example: Using Maven properties

```xml
<properties>
    <nullability.requireExplicitNullMarking>false</nullability.requireExplicitNullMarking>
    <nullability.checking>tests</nullability.checking>
</properties>
```

### Test checking

When `checking` is set to `tests`, the plugin adds a separate configuration for `default-testCompile` with test-specific NullAway options:

- `HandleTestAssertionLibraries=true`
- AssertJ `@Contract` as a custom contract annotation (requires AssertJ 3.27.4+)

### Existing compiler configuration

If you already have a `maven-compiler-plugin` configuration in your `pom.xml`, this plugin preserves it and merges its settings alongside yours:

- **`annotationProcessorPaths`**: ErrorProne and NullAway are appended to your existing paths. Your processors (e.g., MapStruct, Lombok) remain unchanged.
- **`compilerArgs`**: The plugin adds its required arguments (`-XDcompilePolicy=simple`, `-Xplugin:ErrorProne ...`, JVM module flags, etc.) only if they are not already present. Your custom arguments are preserved as-is.
- **Other settings** (`<source>`, `<target>`, `<release>`, `<encoding>`, `<parameters>`, etc.): Not modified by this plugin.

For example, if you have MapStruct and a custom compiler argument:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.15.0</version>
    <configuration>
        <parameters>true</parameters>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.6.3</version>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <arg>-Xlint:all</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

After the nullability plugin runs, the effective configuration will contain all three annotation processors (MapStruct, ErrorProne, NullAway) and all compiler arguments (`-Xlint:all` plus the ErrorProne/NullAway flags).

#### Existing ErrorProne configuration

If you already have ErrorProne configured with your own checks (e.g., `-Xplugin:ErrorProne -Xep:MissingOverride:ERROR`), the plugin will merge NullAway options into your existing `-Xplugin:ErrorProne` argument instead of adding a duplicate one. Your existing checks and settings are preserved:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.15.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.google.errorprone</groupId>
                <artifactId>error_prone_core</artifactId>
                <version>2.47.0</version>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <arg>-XDcompilePolicy=simple</arg>
            <arg>-Xplugin:ErrorProne -Xep:MissingOverride:ERROR</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

After the nullability plugin runs, the `-Xplugin:ErrorProne` argument will contain both your original options and the NullAway-specific options. The plugin will not add `-XepDisableAllChecks` when merging, so your existing ErrorProne checks remain active.

## Requirements

- **JDK 22+** (recommended)
- **`maven-compiler-plugin` 3.5+**

This plugin uses `annotationProcessorPaths` to configure ErrorProne and NullAway, which requires `maven-compiler-plugin` 3.5 or later. Maven 3.9+ includes a sufficient default version, but Maven 3.8.x defaults to 3.1 which does not support `annotationProcessorPaths`. The plugin cannot override this version at runtime.

| Maven version | Default `maven-compiler-plugin` | Works without declaration?    |
|---------------|---------------------------------|-------------------------------|
| 3.9.x         | 3.10+                           | Yes                           |
| 3.8.x         | 3.1                             | **No** (requires declaration) |

If you are using Maven 3.8.x, a minimal declaration in `<pluginManagement>` is sufficient:

```xml
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.15.0</version>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

This plugin itself is compiled for Java 17, but the default dependencies have higher JDK requirements:

- **ErrorProne 2.43.0+** requires JDK 21+ to run. ErrorProne 2.42.0 was the last version to support JDK 17.
- **NullAway's JSpecify mode** (`JSpecifyMode=true`, enabled by default) requires JDK 22+, or JDK 21 with the additional flag `-XDaddTypeAnnotationsToSymbol=true` (OpenJDK-based distributions such as Temurin or Zulu; Oracle JDK 21 does not support this flag). See the [NullAway JSpecify Support](https://github.com/uber/NullAway/wiki/JSpecify-Support#supported-jdk-versions) documentation for details.

These are constraints imposed by ErrorProne and NullAway, not by this plugin. You can use `--release 17` (or lower) to target older Java versions while building with JDK 22+.

The following table summarizes the minimum JDK version required to **run the build** for each configuration combination:

| ErrorProne version | JSpecifyMode     | Minimum JDK |
|--------------------|------------------|-------------|
| 2.43.0+ (default)  | `true` (default) | **JDK 22+** |
| 2.42.0             | `true` (default) | JDK 22+     |
| 2.43.0+ (default)  | `false`          | JDK 21+     |
| 2.42.0             | `false`          | JDK 17+     |

To use an older JDK, adjust the configuration accordingly:

```xml
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>nullability-maven-plugin</artifactId>
    <version>0.2.0</version>
    <extensions>true</extensions>
    <configuration>
        <errorProneVersion>2.42.0</errorProneVersion>
        <jspecifyMode>false</jspecifyMode>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>configure</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Or equivalently, using Maven properties:

```xml
<properties>
    <nullability.errorProneVersion>2.42.0</nullability.errorProneVersion>
    <nullability.jspecifyMode>false</nullability.jspecifyMode>
</properties>
```

Note that disabling JSpecify mode loses some of NullAway's advanced nullability checking capabilities. See the [NullAway JSpecify Support](https://github.com/uber/NullAway/wiki/JSpecify-Support) documentation for details on what JSpecify mode provides.

## Known Limitations

### Incompatibility with Maven Tiles

This plugin relies on `AbstractMavenLifecycleParticipant` registered as a build extension (`<extensions>true</extensions>`). This architecture is fundamentally incompatible with [Maven Tiles](https://github.com/repaint-io/maven-tiles).

- Since v2.14, tiles explicitly reject `<extensions>true</extensions>` in tile definitions.
- Even without this validation, Maven discovers and loads extensions during POM model building, but Tiles merges tile content during `afterProjectsRead()`. By the time tiles are merged, extension discovery has already completed.
- This is an architectural limitation in Maven itself and cannot be solved by the tiles-maven-plugin. See [tiles-maven-plugin#94](https://github.com/repaint-io/maven-tiles/issues/94).

**Workaround**: Declare the plugin directly in the project POM or parent POM.

## License

Licensed under the Apache License, Version 2.0.
