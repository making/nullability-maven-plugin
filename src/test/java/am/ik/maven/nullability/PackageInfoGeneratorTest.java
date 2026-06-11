/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.maven.nullability;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageInfoGeneratorTest {

	private FileSystem fs;

	@BeforeEach
	void setUp() {
		this.fs = Jimfs.newFileSystem(Configuration.unix());
	}

	@AfterEach
	void tearDown() throws IOException {
		this.fs.close();
	}

	@Test
	void generatesPackageInfoForPackageWithoutOne() throws IOException {
		Path srcDir = this.fs.getPath("/src/main/java");
		createJavaFile(srcDir, "com/example", "Foo.java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).containsExactly("com.example");
		Path generatedFile = outputDir.resolve("com/example/package-info.java");
		assertThat(generatedFile).exists();
		String content = Files.readString(generatedFile);
		assertThat(content).isEqualTo("""
				/**
				 * This package is null-marked: all types and their members are non-null by
				 * default unless explicitly annotated as {@code @Nullable}.
				 */
				@NullMarked
				package com.example;

				import org.jspecify.annotations.NullMarked;
				""");
	}

	@Test
	void skipsPackageWithExistingPackageInfo() throws IOException {
		Path srcDir = this.fs.getPath("/src/main/java");
		createJavaFile(srcDir, "com/example", "Foo.java");
		createJavaFile(srcDir, "com/example", "package-info.java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).isEmpty();
		assertThat(outputDir.resolve("com/example/package-info.java")).doesNotExist();
	}

	@Test
	void skipsDefaultPackage() throws IOException {
		Path srcDir = this.fs.getPath("/src/main/java");
		createJavaFile(srcDir, "", "Foo.java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).isEmpty();
	}

	@Test
	void handlesNestedPackagesIndividually() throws IOException {
		Path srcDir = this.fs.getPath("/src/main/java");
		createJavaFile(srcDir, "com/example", "Foo.java");
		createJavaFile(srcDir, "com/example/sub", "Bar.java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).containsExactlyInAnyOrder("com.example", "com.example.sub");
		assertThat(outputDir.resolve("com/example/package-info.java")).exists();
		assertThat(outputDir.resolve("com/example/sub/package-info.java")).exists();
	}

	@Test
	void detectsDuplicatesAcrossMultipleSourceDirectories() throws IOException {
		Path srcDir1 = this.fs.getPath("/src/main/java");
		Path srcDir2 = this.fs.getPath("/src/main/java2");
		createJavaFile(srcDir1, "com/example", "Foo.java");
		createJavaFile(srcDir2, "com/example", "package-info.java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir1, srcDir2), outputDir);

		assertThat(generated).isEmpty();
	}

	@Test
	void doesNotFailForNonExistentSourceDirectory() throws IOException {
		Path srcDir = this.fs.getPath("/nonexistent");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).isEmpty();
	}

	@Test
	void skipsDirectoriesWithoutJavaFiles() throws IOException {
		Path srcDir = this.fs.getPath("/src/main/java");
		Path emptyPkg = srcDir.resolve("com/example/empty");
		Files.createDirectories(emptyPkg);
		// Create a non-Java file
		Files.writeString(emptyPkg.resolve("readme.txt"), "not java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).isEmpty();
	}

	@Test
	void skipsPackageWhenOutputDirectoryAlreadyHasPackageInfo() throws IOException {
		Path srcDir = this.fs.getPath("/src/main/java");
		createJavaFile(srcDir, "com/example", "Foo.java");
		Path outputDir = this.fs.getPath("/target/generated-sources/nullability");
		// Pre-existing generated file
		Path existing = outputDir.resolve("com/example/package-info.java");
		Files.createDirectories(existing.getParent());
		Files.writeString(existing, "existing content");

		Set<String> generated = PackageInfoGenerator.generate(List.of(srcDir), outputDir);

		assertThat(generated).isEmpty();
		// Verify existing file was not overwritten
		assertThat(Files.readString(existing)).isEqualTo("existing content");
	}

	private void createJavaFile(Path sourceDir, String packagePath, String fileName) throws IOException {
		Path dir = packagePath.isEmpty() ? sourceDir : sourceDir.resolve(packagePath);
		Files.createDirectories(dir);
		Files.writeString(dir.resolve(fileName), "// placeholder");
	}

}
