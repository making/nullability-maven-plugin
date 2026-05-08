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

import java.util.Locale;

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

	private static final String PLUGIN_GROUP_ID = "am.ik.maven";

	private static final String PLUGIN_ARTIFACT_ID = "nullability-maven-plugin";

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

	private NullabilityConfiguration parseConfiguration(Plugin plugin, MavenProject project) {
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
			.build();
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
