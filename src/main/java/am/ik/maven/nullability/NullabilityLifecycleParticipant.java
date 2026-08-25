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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle participant that configures the {@code maven-compiler-plugin} with ErrorProne
 * and NullAway before the lifecycle execution plan is computed. This ensures that the
 * compiler plugin picks up the configuration changes.
 */
@Named("nullability")
@Singleton
public class NullabilityLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	/**
	 * Creates a new {@code NullabilityLifecycleParticipant}. Instantiated by the Maven
	 * Sisu/Plexus container; not intended for direct use.
	 */
	public NullabilityLifecycleParticipant() {
	}

	private static final String PLUGIN_GROUP_ID = "am.ik.maven";

	private static final String PLUGIN_ARTIFACT_ID = "nullability-maven-plugin";

	private static final String NULLAWAY_OPTIONS_PROPERTY_PREFIX = "nullability.nullAwayOptions.";

	private final Logger logger = LoggerFactory.getLogger(NullabilityLifecycleParticipant.class);

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		for (MavenProject project : session.getProjects()) {
			configureProject(project);
		}
	}

	private void configureProject(MavenProject project) throws MavenExecutionException {
		Plugin nullabilityPlugin = findNullabilityPlugin(project);
		if (nullabilityPlugin == null) {
			return;
		}

		NullabilityConfiguration config = parseConfiguration(nullabilityPlugin, project);
		if (config == null) {
			this.logger.info("[nullability] Plugin is skipped for " + project.getArtifactId());
			return;
		}

		this.logger.info("[nullability] Configuring ErrorProne " + config.errorProneVersion() + " and NullAway "
				+ config.nullAwayVersion() + " for " + project.getArtifactId() + " (checking=" + config.checking()
				+ ")");

		CompilerConfigurer.configure(project, config);
	}

	private Plugin findNullabilityPlugin(MavenProject project) {
		if (project.getBuild() == null) {
			return null;
		}
		for (Plugin plugin : project.getBuild().getPlugins()) {
			if (PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())
					&& (plugin.getGroupId() == null || PLUGIN_GROUP_ID.equals(plugin.getGroupId()))) {
				return plugin;
			}
		}
		return null;
	}

	NullabilityConfiguration parseConfiguration(Plugin plugin, MavenProject project) throws MavenExecutionException {
		Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();

		if (getBooleanValue(config, "skip", resolveProperty(project, "nullability.skip", "false"))) {
			return null;
		}

		return NullabilityConfiguration.builder()
			.errorProneVersion(getStringValue(config, "errorProneVersion",
					resolveProperty(project, "nullability.errorProneVersion",
							NullabilityConfiguration.DEFAULT_ERROR_PRONE_VERSION)))
			.nullAwayVersion(getStringValue(config, "nullAwayVersion",
					resolveProperty(project, "nullability.nullAwayVersion",
							NullabilityConfiguration.DEFAULT_NULLAWAY_VERSION)))
			.checking(Checking
				.valueOf(getStringValue(config, "checking", resolveProperty(project, "nullability.checking", "main"))
					.toUpperCase(Locale.ROOT)))
			.requireExplicitNullMarking(getBooleanValue(config, "requireExplicitNullMarking",
					resolveProperty(project, "nullability.requireExplicitNullMarking", "true")))
			.customContractAnnotations(getStringValue(config, "customContractAnnotations",
					resolveProperty(project, "nullability.customContractAnnotations", "")))
			.jspecifyMode(getBooleanValue(config, "jspecifyMode",
					resolveProperty(project, "nullability.jspecifyMode", "true")))
			.excludedPaths(getStringValue(config, "excludedPaths",
					resolveProperty(project, "nullability.excludedPaths",
							NullabilityConfiguration.DEFAULT_EXCLUDED_PATHS)))
			.nullAwaySeverity(Severity.valueOf(getStringValue(config, "nullAwaySeverity",
					resolveProperty(project, "nullability.nullAwaySeverity", "error"))
				.toUpperCase(Locale.ROOT)))
			.requireExplicitNullMarkingSeverity(
					Severity.valueOf(getStringValue(config, "requireExplicitNullMarkingSeverity",
							resolveProperty(project, "nullability.requireExplicitNullMarkingSeverity", "error"))
						.toUpperCase(Locale.ROOT)))
			.addTypeAnnotationsToSymbol(getBooleanValue(config, "addTypeAnnotationsToSymbol",
					resolveProperty(project, "nullability.addTypeAnnotationsToSymbol", "true")))
			.nullAwayOptions(parseNullAwayOptions(config, project))
			.build();
	}

	/**
	 * Collects the additional NullAway options from the {@code <nullAwayOptions>}
	 * configuration element and from the {@code nullability.nullAwayOptions.*} project
	 * properties. The configuration element wins over the property of the same option
	 * name.
	 * @param config the plugin configuration, may be {@code null}
	 * @param project the Maven project
	 * @return the additional NullAway options keyed by option name
	 * @throws MavenExecutionException if an option name or value is not usable as an
	 * ErrorProne option
	 */
	private Map<String, String> parseNullAwayOptions(Xpp3Dom config, MavenProject project)
			throws MavenExecutionException {
		Map<String, String> options = new LinkedHashMap<>();
		for (String propertyName : project.getProperties().stringPropertyNames()) {
			if (propertyName.startsWith(NULLAWAY_OPTIONS_PROPERTY_PREFIX)) {
				options.put(propertyName.substring(NULLAWAY_OPTIONS_PROPERTY_PREFIX.length()),
						project.getProperties().getProperty(propertyName));
			}
		}
		Xpp3Dom optionsConfig = (config != null) ? config.getChild("nullAwayOptions") : null;
		if (optionsConfig != null) {
			for (Xpp3Dom option : optionsConfig.getChildren()) {
				options.put(option.getName(), option.getValue());
			}
		}
		Map<String, String> validated = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : options.entrySet()) {
			String name = trimToEmpty(entry.getKey());
			String value = trimToEmpty(entry.getValue());
			validateNullAwayOption(name, value, project);
			validated.put(name, value);
		}
		return validated;
	}

	private void validateNullAwayOption(String name, String value, MavenProject project)
			throws MavenExecutionException {
		if (name.isEmpty() || value.isEmpty()) {
			throw new MavenExecutionException("[nullability] A nullAwayOptions entry must have a name and a value"
					+ " but was '" + name + "'='" + value + "'.", project.getFile());
		}
		if (containsWhitespace(name) || containsWhitespace(value) || name.indexOf('=') >= 0) {
			throw new MavenExecutionException(
					"[nullability] The nullAwayOptions entry '" + name + "'='" + value
							+ "' cannot be passed to ErrorProne because the option name or value contains"
							+ " whitespace or '='. Options are appended to a single -Xplugin:ErrorProne argument.",
					project.getFile());
		}
	}

	private static boolean containsWhitespace(String value) {
		return value.chars().anyMatch(Character::isWhitespace);
	}

	private static String trimToEmpty(String value) {
		return (value != null) ? value.trim() : "";
	}

	private String resolveProperty(MavenProject project, String propertyName, String defaultValue) {
		String value = project.getProperties().getProperty(propertyName);
		return (value != null) ? value : defaultValue;
	}

	private static String getStringValue(Xpp3Dom config, String name, String defaultValue) {
		if (config == null) {
			return defaultValue;
		}
		Xpp3Dom child = config.getChild(name);
		return (child != null && child.getValue() != null) ? child.getValue() : defaultValue;
	}

	private static boolean getBooleanValue(Xpp3Dom config, String name, String defaultValue) {
		return Boolean.parseBoolean(getStringValue(config, name, defaultValue));
	}

}
