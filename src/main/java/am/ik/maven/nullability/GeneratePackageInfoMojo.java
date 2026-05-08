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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates {@code @NullMarked} annotated {@code package-info.java} files for packages
 * that do not already have one. This is useful when
 * {@code requireExplicitNullMarking=true} (the default) so that every package is covered
 * by {@code @NullMarked} without manually creating {@code package-info.java} files.
 */
@Mojo(name = "generate-package-info", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class GeneratePackageInfoMojo extends AbstractMojo {

	/**
	 * Creates a new {@code GeneratePackageInfoMojo}. Instantiated by Maven; not intended
	 * for direct use.
	 */
	public GeneratePackageInfoMojo() {
	}

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/**
	 * Checking mode: {@code main} (default) or {@code tests}. When set to {@code tests},
	 * test source directories are also processed.
	 */
	@Parameter(property = "nullability.checking", defaultValue = "main")
	private String checking;

	/**
	 * Output directory for generated main source {@code package-info.java} files.
	 */
	@Parameter(property = "nullability.generatePackageInfo.outputDirectory",
			defaultValue = "${project.build.directory}/generated-sources/nullability")
	private File outputDirectory;

	/**
	 * Output directory for generated test source {@code package-info.java} files.
	 */
	@Parameter(property = "nullability.generatePackageInfo.testOutputDirectory",
			defaultValue = "${project.build.directory}/generated-test-sources/nullability")
	private File testOutputDirectory;

	/**
	 * Whether to skip the generation of {@code package-info.java} files.
	 */
	@Parameter(property = "nullability.skip", defaultValue = "false")
	private boolean skip;

	@Override
	public void execute() throws MojoExecutionException {
		if (this.skip) {
			getLog().info("Nullability plugin is skipped.");
			return;
		}

		Checking checkingMode = Checking.valueOf(this.checking.toUpperCase());
		Path mainOutputDir = this.outputDirectory.toPath();

		generateForMain(mainOutputDir);

		if (checkingMode.isTestChecking()) {
			Path testOutputDir = this.testOutputDirectory.toPath();
			generateForTests(testOutputDir);
		}
	}

	private void generateForMain(Path outputDir) throws MojoExecutionException {
		List<Path> sourceRoots = this.project.getCompileSourceRoots().stream().map(root -> Path.of(root)).toList();

		try {
			Set<String> generated = PackageInfoGenerator.generate(sourceRoots, outputDir);
			if (!generated.isEmpty()) {
				getLog().info("[nullability] Generated @NullMarked package-info.java for: " + generated);
			}
		}
		catch (IOException ex) {
			throw new MojoExecutionException("Failed to generate package-info.java files", ex);
		}

		this.project.addCompileSourceRoot(outputDir.toString());
	}

	private void generateForTests(Path outputDir) throws MojoExecutionException {
		List<Path> testSourceRoots = this.project.getTestCompileSourceRoots()
			.stream()
			.map(root -> Path.of(root))
			.toList();

		try {
			Set<String> generated = PackageInfoGenerator.generate(testSourceRoots, outputDir);
			if (!generated.isEmpty()) {
				getLog().info("[nullability] Generated @NullMarked test package-info.java for: " + generated);
			}
		}
		catch (IOException ex) {
			throw new MojoExecutionException("Failed to generate test package-info.java files", ex);
		}

		this.project.addTestCompileSourceRoot(outputDir.toString());
	}

}
