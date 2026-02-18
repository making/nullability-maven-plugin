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

import java.util.Arrays;

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompilerConfigurerTest {

	@Test
	void createsCompilerPluginWhenAbsent() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Plugin compilerPlugin = findCompilerPlugin(project);
		assertThat(compilerPlugin).isNotNull();
		assertThat(compilerPlugin.getArtifactId()).isEqualTo("maven-compiler-plugin");
	}

	@Test
	void setsForkToTrue() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom config = (Xpp3Dom) findCompilerPlugin(project).getConfiguration();
		assertThat(config.getChild("fork").getValue()).isEqualTo("true");
	}

	@Test
	void addsAnnotationProcessorPaths() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom config = (Xpp3Dom) findCompilerPlugin(project).getConfiguration();
		Xpp3Dom paths = config.getChild("annotationProcessorPaths");
		assertThat(paths).isNotNull();
		assertThat(paths.getChildren("path")).hasSize(2);

		Xpp3Dom errorProne = paths.getChildren("path")[0];
		assertThat(errorProne.getChild("groupId").getValue()).isEqualTo("com.google.errorprone");
		assertThat(errorProne.getChild("artifactId").getValue()).isEqualTo("error_prone_core");
		assertThat(errorProne.getChild("version").getValue()).isEqualTo("2.47.0");

		Xpp3Dom nullAway = paths.getChildren("path")[1];
		assertThat(nullAway.getChild("groupId").getValue()).isEqualTo("com.uber.nullaway");
		assertThat(nullAway.getChild("artifactId").getValue()).isEqualTo("nullaway");
		assertThat(nullAway.getChild("version").getValue()).isEqualTo("0.13.1");
	}

	@Test
	void addsCompilerArgs() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom config = (Xpp3Dom) findCompilerPlugin(project).getConfiguration();
		Xpp3Dom compilerArgs = config.getChild("compilerArgs");
		assertThat(compilerArgs).isNotNull();

		String[] argValues = Arrays.stream(compilerArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.toArray(String[]::new);

		assertThat(argValues).contains("-XDcompilePolicy=simple", "--should-stop=ifError=FLOW");
		assertThat(argValues)
			.anyMatch(arg -> arg.startsWith("-Xplugin:ErrorProne") && arg.contains("-XepExcludedPaths:"));
		assertThat(argValues).contains("-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
	}

	@Test
	void preservesExistingAnnotationProcessorPaths() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());

		Plugin compilerPlugin = new Plugin();
		compilerPlugin.setGroupId("org.apache.maven.plugins");
		compilerPlugin.setArtifactId("maven-compiler-plugin");
		Xpp3Dom config = new Xpp3Dom("configuration");
		Xpp3Dom paths = new Xpp3Dom("annotationProcessorPaths");
		Xpp3Dom existingPath = new Xpp3Dom("path");
		Xpp3Dom gid = new Xpp3Dom("groupId");
		gid.setValue("com.example");
		existingPath.addChild(gid);
		Xpp3Dom aid = new Xpp3Dom("artifactId");
		aid.setValue("my-processor");
		existingPath.addChild(aid);
		Xpp3Dom ver = new Xpp3Dom("version");
		ver.setValue("1.0");
		existingPath.addChild(ver);
		paths.addChild(existingPath);
		config.addChild(paths);
		compilerPlugin.setConfiguration(config);
		project.getBuild().addPlugin(compilerPlugin);

		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom updatedPaths = ((Xpp3Dom) compilerPlugin.getConfiguration()).getChild("annotationProcessorPaths");
		assertThat(updatedPaths.getChildren("path")).hasSize(3);
		assertThat(updatedPaths.getChildren("path")[0].getChild("artifactId").getValue()).isEqualTo("my-processor");
	}

	@Test
	void preservesExistingCompilerArgs() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());

		Plugin compilerPlugin = new Plugin();
		compilerPlugin.setGroupId("org.apache.maven.plugins");
		compilerPlugin.setArtifactId("maven-compiler-plugin");
		Xpp3Dom config = new Xpp3Dom("configuration");
		Xpp3Dom compilerArgs = new Xpp3Dom("compilerArgs");
		Xpp3Dom existingArg = new Xpp3Dom("arg");
		existingArg.setValue("-Xlint:all");
		compilerArgs.addChild(existingArg);
		config.addChild(compilerArgs);
		compilerPlugin.setConfiguration(config);
		project.getBuild().addPlugin(compilerPlugin);

		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom updatedArgs = ((Xpp3Dom) compilerPlugin.getConfiguration()).getChild("compilerArgs");
		String[] argValues = Arrays.stream(updatedArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.toArray(String[]::new);

		// Existing arg is preserved
		assertThat(argValues).contains("-Xlint:all");
		// Plugin args are also added
		assertThat(argValues).contains("-XDcompilePolicy=simple", "--should-stop=ifError=FLOW");
		assertThat(argValues)
			.anyMatch(arg -> arg.startsWith("-Xplugin:ErrorProne") && arg.contains("-XepExcludedPaths:"));
	}

	@Test
	void doesNotDuplicateAnnotationProcessorPaths() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom config = (Xpp3Dom) findCompilerPlugin(project).getConfiguration();
		Xpp3Dom paths = config.getChild("annotationProcessorPaths");
		assertThat(paths.getChildren("path")).hasSize(2);
	}

	@Test
	void doesNotDuplicateCompilerArgs() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom config = (Xpp3Dom) findCompilerPlugin(project).getConfiguration();
		Xpp3Dom compilerArgs = config.getChild("compilerArgs");
		long count = Arrays.stream(compilerArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.filter("-XDcompilePolicy=simple"::equals)
			.count();
		assertThat(count).isEqualTo(1);
	}

	@Test
	void configuresTestCompileExecutionWhenTestCheckingEnabled() {
		NullabilityConfiguration config = new NullabilityConfiguration("2.47.0", "0.13.1", true, true, true, true,
				".*/target/generated-sources/.*");
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, config);

		Plugin compilerPlugin = findCompilerPlugin(project);
		PluginExecution testExecution = compilerPlugin.getExecutions()
			.stream()
			.filter(e -> "default-testCompile".equals(e.getId()))
			.findFirst()
			.orElse(null);
		assertThat(testExecution).isNotNull();

		Xpp3Dom execConfig = (Xpp3Dom) testExecution.getConfiguration();
		assertThat(execConfig).isNotNull();
		Xpp3Dom compilerArgs = execConfig.getChild("compilerArgs");
		assertThat(compilerArgs).isNotNull();

		String[] argValues = Arrays.stream(compilerArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.toArray(String[]::new);
		assertThat(argValues).anyMatch(arg -> arg.contains("HandleTestAssertionLibraries=true"));
	}

	@Test
	void noTestCompileExecutionWhenTestCheckingDisabled() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Plugin compilerPlugin = findCompilerPlugin(project);
		boolean hasTestExecution = compilerPlugin.getExecutions()
			.stream()
			.anyMatch(e -> "default-testCompile".equals(e.getId()));
		assertThat(hasTestExecution).isFalse();
	}

	@Test
	void disabledMainAndTestDoesNotModifyProject() {
		NullabilityConfiguration config = new NullabilityConfiguration("2.47.0", "0.13.1", false, false, true, true,
				"");
		MavenProject project = new MavenProject();
		project.setBuild(new Build());
		CompilerConfigurer.configure(project, config);

		assertThat(project.getBuild().getPlugins()).isEmpty();
	}

	@Test
	void mergesNullAwayIntoExistingErrorProneConfig() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());

		Plugin compilerPlugin = new Plugin();
		compilerPlugin.setGroupId("org.apache.maven.plugins");
		compilerPlugin.setArtifactId("maven-compiler-plugin");
		Xpp3Dom config = new Xpp3Dom("configuration");
		Xpp3Dom compilerArgs = new Xpp3Dom("compilerArgs");
		Xpp3Dom existingArg = new Xpp3Dom("arg");
		existingArg.setValue("-Xplugin:ErrorProne -Xep:MissingOverride:ERROR");
		compilerArgs.addChild(existingArg);
		config.addChild(compilerArgs);
		compilerPlugin.setConfiguration(config);
		project.getBuild().addPlugin(compilerPlugin);

		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom updatedArgs = ((Xpp3Dom) compilerPlugin.getConfiguration()).getChild("compilerArgs");
		String[] argValues = Arrays.stream(updatedArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.toArray(String[]::new);

		// Should have exactly one -Xplugin:ErrorProne arg (merged, not duplicated)
		long errorProneArgCount = Arrays.stream(argValues).filter(arg -> arg.startsWith("-Xplugin:ErrorProne")).count();
		assertThat(errorProneArgCount).isEqualTo(1);

		// The merged arg should contain both original and NullAway options
		String mergedArg = Arrays.stream(argValues)
			.filter(arg -> arg.startsWith("-Xplugin:ErrorProne"))
			.findFirst()
			.orElseThrow();
		assertThat(mergedArg).contains("-Xep:MissingOverride:ERROR");
		assertThat(mergedArg).contains("-XepOpt:NullAway:OnlyNullMarked=true");
		assertThat(mergedArg).contains("-Xep:NullAway:ERROR");
		assertThat(mergedArg).contains("-XepExcludedPaths:");
	}

	@Test
	void doesNotAddXepDisableAllChecksWhenMergingIntoExistingErrorProne() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());

		Plugin compilerPlugin = new Plugin();
		compilerPlugin.setGroupId("org.apache.maven.plugins");
		compilerPlugin.setArtifactId("maven-compiler-plugin");
		Xpp3Dom config = new Xpp3Dom("configuration");
		Xpp3Dom compilerArgs = new Xpp3Dom("compilerArgs");
		Xpp3Dom existingArg = new Xpp3Dom("arg");
		existingArg.setValue("-Xplugin:ErrorProne -Xep:MissingOverride:ERROR");
		compilerArgs.addChild(existingArg);
		config.addChild(compilerArgs);
		compilerPlugin.setConfiguration(config);
		project.getBuild().addPlugin(compilerPlugin);

		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom updatedArgs = ((Xpp3Dom) compilerPlugin.getConfiguration()).getChild("compilerArgs");
		String mergedArg = Arrays.stream(updatedArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.filter(arg -> arg.startsWith("-Xplugin:ErrorProne"))
			.findFirst()
			.orElseThrow();
		assertThat(mergedArg).doesNotContain("-XepDisableAllChecks");
	}

	@Test
	void doesNotOverrideExistingNullAwayOptions() {
		MavenProject project = new MavenProject();
		project.setBuild(new Build());

		Plugin compilerPlugin = new Plugin();
		compilerPlugin.setGroupId("org.apache.maven.plugins");
		compilerPlugin.setArtifactId("maven-compiler-plugin");
		Xpp3Dom config = new Xpp3Dom("configuration");
		Xpp3Dom compilerArgs = new Xpp3Dom("compilerArgs");
		Xpp3Dom existingArg = new Xpp3Dom("arg");
		existingArg.setValue("-Xplugin:ErrorProne -XepOpt:NullAway:OnlyNullMarked=false -Xep:NullAway:WARN");
		compilerArgs.addChild(existingArg);
		config.addChild(compilerArgs);
		compilerPlugin.setConfiguration(config);
		project.getBuild().addPlugin(compilerPlugin);

		CompilerConfigurer.configure(project, NullabilityConfiguration.defaults());

		Xpp3Dom updatedArgs = ((Xpp3Dom) compilerPlugin.getConfiguration()).getChild("compilerArgs");
		String mergedArg = Arrays.stream(updatedArgs.getChildren("arg"))
			.map(Xpp3Dom::getValue)
			.filter(arg -> arg.startsWith("-Xplugin:ErrorProne"))
			.findFirst()
			.orElseThrow();
		// Existing user values should be preserved (not overridden)
		assertThat(mergedArg).contains("-XepOpt:NullAway:OnlyNullMarked=false");
		assertThat(mergedArg).doesNotContain("-XepOpt:NullAway:OnlyNullMarked=true");
		assertThat(mergedArg).contains("-Xep:NullAway:WARN");
		// Options not already present should be added
		assertThat(mergedArg).contains("-XepOpt:NullAway:CheckContracts=true");
	}

	@Test
	void extractOptionPrefixForEqualsOption() {
		assertThat(CompilerConfigurer.extractOptionPrefix("-XepOpt:NullAway:OnlyNullMarked=true"))
			.isEqualTo("-XepOpt:NullAway:OnlyNullMarked=");
	}

	@Test
	void extractOptionPrefixForExcludedPaths() {
		assertThat(CompilerConfigurer.extractOptionPrefix("-XepExcludedPaths:(.*/test/java/.*)"))
			.isEqualTo("-XepExcludedPaths:");
	}

	@Test
	void extractOptionPrefixForXepOption() {
		assertThat(CompilerConfigurer.extractOptionPrefix("-Xep:NullAway:ERROR")).isEqualTo("-Xep:NullAway:");
	}

	private Plugin findCompilerPlugin(MavenProject project) {
		return project.getBuild()
			.getPlugins()
			.stream()
			.filter(p -> "maven-compiler-plugin".equals(p.getArtifactId()))
			.findFirst()
			.orElse(null);
	}

}
