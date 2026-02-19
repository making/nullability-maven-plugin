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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Generates {@code @NullMarked} annotated {@code package-info.java} files for packages
 * that do not already have one.
 */
public final class PackageInfoGenerator {

	private static final String PACKAGE_INFO = "package-info.java";

	private PackageInfoGenerator() {
	}

	/**
	 * Scans the given source directories and generates {@code package-info.java} files in
	 * the output directory for packages that do not already have one.
	 * @param sourceDirectories directories to scan for Java source files
	 * @param outputDirectory directory to write generated {@code package-info.java} files
	 * @return the set of package names for which files were generated
	 * @throws IOException if an I/O error occurs
	 */
	public static Set<String> generate(List<Path> sourceDirectories, Path outputDirectory) throws IOException {
		Set<String> packagesWithJava = new LinkedHashSet<>();
		Set<String> packagesWithPackageInfo = new LinkedHashSet<>();

		for (Path sourceDir : sourceDirectories) {
			if (!Files.isDirectory(sourceDir)) {
				continue;
			}
			try (Stream<Path> walk = Files.walk(sourceDir, FileVisitOption.FOLLOW_LINKS)) {
				walk.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().endsWith(".java"))
					.forEach(javaFile -> {
						Path relativePath = sourceDir.relativize(javaFile.getParent());
						String packageName = toPackageName(relativePath);
						if (!packageName.isEmpty()) {
							packagesWithJava.add(packageName);
							if (javaFile.getFileName().toString().equals(PACKAGE_INFO)) {
								packagesWithPackageInfo.add(packageName);
							}
						}
					});
			}
		}

		// Also check the output directory for already-generated package-info.java
		if (Files.isDirectory(outputDirectory)) {
			try (Stream<Path> walk = Files.walk(outputDirectory, FileVisitOption.FOLLOW_LINKS)) {
				walk.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().equals(PACKAGE_INFO))
					.forEach(packageInfoFile -> {
						Path relativePath = outputDirectory.relativize(packageInfoFile.getParent());
						String packageName = toPackageName(relativePath);
						if (!packageName.isEmpty()) {
							packagesWithPackageInfo.add(packageName);
						}
					});
			}
		}

		Set<String> generated = new LinkedHashSet<>();
		for (String packageName : packagesWithJava) {
			if (packagesWithPackageInfo.contains(packageName)) {
				continue;
			}
			Path packageDir = outputDirectory.resolve(packageName.replace('.', '/'));
			Files.createDirectories(packageDir);
			Path packageInfoPath = packageDir.resolve(PACKAGE_INFO);
			String content = "@NullMarked\npackage " + packageName
					+ ";\n\nimport org.jspecify.annotations.NullMarked;\n";
			Files.writeString(packageInfoPath, content);
			generated.add(packageName);
		}

		return generated;
	}

	private static String toPackageName(Path relativePath) {
		if (relativePath.toString().isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < relativePath.getNameCount(); i++) {
			if (i > 0) {
				sb.append('.');
			}
			sb.append(relativePath.getName(i));
		}
		return sb.toString();
	}

}
