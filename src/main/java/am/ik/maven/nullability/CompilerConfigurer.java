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
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Modifies the {@code maven-compiler-plugin} configuration in a Maven project to enable
 * ErrorProne and NullAway.
 */
public final class CompilerConfigurer {

	private static final String COMPILER_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

	private static final String COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin";

	private static final String ERROR_PRONE_GROUP_ID = "com.google.errorprone";

	private static final String ERROR_PRONE_ARTIFACT_ID = "error_prone_core";

	private static final String NULLAWAY_GROUP_ID = "com.uber.nullaway";

	private static final String NULLAWAY_ARTIFACT_ID = "nullaway";

	/**
	 * JVM module flags required by ErrorProne.
	 */
	private static final List<String> JVM_MODULE_FLAGS = List.of(
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
			"-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
			"-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
			"-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED");

	private CompilerConfigurer() {
	}

	/**
	 * Configures the maven-compiler-plugin for the given project.
	 * @param project the Maven project to configure
	 * @param config the nullability configuration
	 */
	public static void configure(MavenProject project, NullabilityConfiguration config) {
		if (!config.mainChecking() && !config.testChecking()) {
			return;
		}
		Plugin compilerPlugin = findOrCreateCompilerPlugin(project);

		// Configure at plugin level
		Xpp3Dom pluginConfig = getOrCreateConfiguration(compilerPlugin);
		applyBaseConfiguration(pluginConfig, config);
		configureMainCompilerArgs(pluginConfig, config);

		// Also configure at default-compile execution level to ensure Maven picks up
		// the changes (MojoExecutionConfigurator reads execution-level config first)
		configureDefaultCompileExecution(compilerPlugin, config);

		if (config.testChecking()) {
			configureTestCompileExecution(compilerPlugin, config);
		}
	}

	private static void applyBaseConfiguration(Xpp3Dom config, NullabilityConfiguration nullabilityConfig) {
		setChild(config, "fork", "true");
		configureAnnotationProcessorPaths(config, nullabilityConfig);
	}

	private static void configureMainCompilerArgs(Xpp3Dom config, NullabilityConfiguration nullabilityConfig) {
		Xpp3Dom compilerArgs = getOrCreateChild(config, "compilerArgs");
		addCompilerArgs(compilerArgs, false, nullabilityConfig);
	}

	private static void configureDefaultCompileExecution(Plugin compilerPlugin, NullabilityConfiguration config) {
		PluginExecution execution = findOrCreateExecution(compilerPlugin, "default-compile");
		Xpp3Dom execConfig = getOrCreateExecutionConfiguration(execution);
		applyBaseConfiguration(execConfig, config);
		Xpp3Dom compilerArgs = getOrCreateChild(execConfig, "compilerArgs");
		addCompilerArgs(compilerArgs, false, config);
	}

	private static void configureTestCompileExecution(Plugin compilerPlugin, NullabilityConfiguration config) {
		PluginExecution execution = findOrCreateExecution(compilerPlugin, "default-testCompile");
		Xpp3Dom execConfig = getOrCreateExecutionConfiguration(execution);
		applyBaseConfiguration(execConfig, config);
		Xpp3Dom compilerArgs = getOrCreateChild(execConfig, "compilerArgs");
		addCompilerArgs(compilerArgs, true, config);
	}

	private static void addCompilerArgs(Xpp3Dom compilerArgs, boolean forTests,
			NullabilityConfiguration nullabilityConfig) {
		addArgIfAbsent(compilerArgs, "-XDcompilePolicy=simple");
		addArgIfAbsent(compilerArgs, "--should-stop=ifError=FLOW");

		addOrMergeErrorProneArg(compilerArgs, forTests, nullabilityConfig);

		for (String flag : JVM_MODULE_FLAGS) {
			addArgIfAbsent(compilerArgs, flag);
		}
	}

	private static void addOrMergeErrorProneArg(Xpp3Dom compilerArgs, boolean forTests,
			NullabilityConfiguration config) {
		Xpp3Dom existingArg = findArgByPrefix(compilerArgs, "-Xplugin:ErrorProne");
		if (existingArg == null) {
			String errorProneArg = NullAwayArgsBuilder.build(forTests, config);
			addArgIfAbsent(compilerArgs, errorProneArg);
		}
		else {
			mergeNullAwayOptions(existingArg, forTests, config);
		}
	}

	private static void mergeNullAwayOptions(Xpp3Dom existingArg, boolean forTests, NullabilityConfiguration config) {
		String value = existingArg.getValue();
		StringBuilder sb = new StringBuilder(value);
		for (String option : NullAwayArgsBuilder.buildNullAwayOptions(forTests, config)) {
			String prefix = extractOptionPrefix(option);
			if (!value.contains(prefix)) {
				sb.append(" ").append(option);
			}
		}
		existingArg.setValue(sb.toString());
	}

	static String extractOptionPrefix(String option) {
		if (option.startsWith("-XepExcludedPaths:")) {
			return "-XepExcludedPaths:";
		}
		int equalsIndex = option.indexOf('=');
		if (equalsIndex >= 0) {
			return option.substring(0, equalsIndex + 1);
		}
		// For -Xep:Name:LEVEL patterns, use up to and including the second-to-last colon
		int lastColon = option.lastIndexOf(':');
		if (lastColon > 0) {
			return option.substring(0, lastColon + 1);
		}
		return option;
	}

	private static Xpp3Dom findArgByPrefix(Xpp3Dom compilerArgs, String prefix) {
		for (Xpp3Dom child : compilerArgs.getChildren("arg")) {
			if (child.getValue() != null && child.getValue().startsWith(prefix)) {
				return child;
			}
		}
		return null;
	}

	private static Plugin findOrCreateCompilerPlugin(MavenProject project) {
		Build build = project.getBuild();
		if (build == null) {
			build = new Build();
			project.setBuild(build);
		}
		for (Plugin plugin : build.getPlugins()) {
			if (COMPILER_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())
					&& (plugin.getGroupId() == null || COMPILER_PLUGIN_GROUP_ID.equals(plugin.getGroupId()))) {
				return plugin;
			}
		}
		Plugin plugin = new Plugin();
		plugin.setGroupId(COMPILER_PLUGIN_GROUP_ID);
		plugin.setArtifactId(COMPILER_PLUGIN_ARTIFACT_ID);
		build.addPlugin(plugin);
		return plugin;
	}

	private static Xpp3Dom getOrCreateConfiguration(Plugin plugin) {
		Object existing = plugin.getConfiguration();
		if (existing instanceof Xpp3Dom dom) {
			return dom;
		}
		Xpp3Dom config = new Xpp3Dom("configuration");
		plugin.setConfiguration(config);
		return config;
	}

	private static void configureAnnotationProcessorPaths(Xpp3Dom config, NullabilityConfiguration nullabilityConfig) {
		Xpp3Dom paths = getOrCreateChild(config, "annotationProcessorPaths");

		if (!hasAnnotationProcessorPath(paths, ERROR_PRONE_GROUP_ID, ERROR_PRONE_ARTIFACT_ID)) {
			paths.addChild(createPathElement(ERROR_PRONE_GROUP_ID, ERROR_PRONE_ARTIFACT_ID,
					nullabilityConfig.errorProneVersion()));
		}

		if (!hasAnnotationProcessorPath(paths, NULLAWAY_GROUP_ID, NULLAWAY_ARTIFACT_ID)) {
			paths.addChild(
					createPathElement(NULLAWAY_GROUP_ID, NULLAWAY_ARTIFACT_ID, nullabilityConfig.nullAwayVersion()));
		}
	}

	private static boolean hasAnnotationProcessorPath(Xpp3Dom paths, String groupId, String artifactId) {
		return Arrays.stream(paths.getChildren("path"))
			.anyMatch(path -> hasChildValue(path, "groupId", groupId) && hasChildValue(path, "artifactId", artifactId));
	}

	private static boolean hasChildValue(Xpp3Dom parent, String childName, String value) {
		Xpp3Dom child = parent.getChild(childName);
		return child != null && value.equals(child.getValue());
	}

	private static Xpp3Dom createPathElement(String groupId, String artifactId, String version) {
		Xpp3Dom path = new Xpp3Dom("path");
		setChild(path, "groupId", groupId);
		setChild(path, "artifactId", artifactId);
		setChild(path, "version", version);
		return path;
	}

	private static PluginExecution findOrCreateExecution(Plugin plugin, String executionId) {
		for (PluginExecution execution : plugin.getExecutions()) {
			if (executionId.equals(execution.getId())) {
				return execution;
			}
		}
		PluginExecution execution = new PluginExecution();
		execution.setId(executionId);
		plugin.addExecution(execution);
		return execution;
	}

	private static Xpp3Dom getOrCreateExecutionConfiguration(PluginExecution execution) {
		Object existing = execution.getConfiguration();
		if (existing instanceof Xpp3Dom dom) {
			return dom;
		}
		Xpp3Dom config = new Xpp3Dom("configuration");
		execution.setConfiguration(config);
		return config;
	}

	private static Xpp3Dom getOrCreateChild(Xpp3Dom parent, String name) {
		Xpp3Dom child = parent.getChild(name);
		if (child == null) {
			child = new Xpp3Dom(name);
			parent.addChild(child);
		}
		return child;
	}

	private static void addArgIfAbsent(Xpp3Dom compilerArgs, String value) {
		for (Xpp3Dom child : compilerArgs.getChildren("arg")) {
			if (value.equals(child.getValue())) {
				return;
			}
		}
		Xpp3Dom arg = new Xpp3Dom("arg");
		arg.setValue(value);
		compilerArgs.addChild(arg);
	}

	private static void setChild(Xpp3Dom parent, String name, String value) {
		Xpp3Dom child = parent.getChild(name);
		if (child == null) {
			child = new Xpp3Dom(name);
			parent.addChild(child);
		}
		child.setValue(value);
	}

}
