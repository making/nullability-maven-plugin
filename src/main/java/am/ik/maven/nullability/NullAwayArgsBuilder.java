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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the {@code -Xplugin:ErrorProne} argument string for main or test compilation.
 */
public final class NullAwayArgsBuilder {

	/**
	 * Prefix of a NullAway option passed to ErrorProne.
	 */
	static final String NULLAWAY_OPTION_PREFIX = "-XepOpt:NullAway:";

	private NullAwayArgsBuilder() {
	}

	/**
	 * Builds the {@code -Xplugin:ErrorProne} argument string. The excluded paths are
	 * included in this argument to avoid shell escaping issues with parentheses and pipe
	 * characters when the compiler is forked.
	 * @param forTests whether this is for test compilation
	 * @param config the nullability configuration
	 * @return the argument string
	 */
	public static String build(boolean forTests, NullabilityConfiguration config) {
		StringBuilder sb = new StringBuilder("-Xplugin:ErrorProne");
		sb.append(" -XepDisableAllChecks");
		for (String option : buildNullAwayOptions(forTests, config)) {
			sb.append(" ").append(option);
		}
		return sb.toString();
	}

	/**
	 * Builds the NullAway-specific options without the {@code -Xplugin:ErrorProne} prefix
	 * or {@code -XepDisableAllChecks}. These options can be merged into an existing
	 * {@code -Xplugin:ErrorProne} argument.
	 * @param forTests whether this is for test compilation
	 * @param config the nullability configuration
	 * @return a list of NullAway-specific ErrorProne options
	 */
	static List<String> buildNullAwayOptions(boolean forTests, NullabilityConfiguration config) {
		List<String> options = new ArrayList<>();
		buildNullAwayOptionMap(forTests, config)
			.forEach((name, value) -> options.add(NULLAWAY_OPTION_PREFIX + name + "=" + value));

		options.add("-Xep:NullAway:" + config.nullAwaySeverity().name());

		if (config.requireExplicitNullMarking()) {
			options.add("-Xep:RequireExplicitNullMarking:" + config.requireExplicitNullMarkingSeverity().name());
		}

		String excludedPaths = buildExcludedPaths(forTests, config);
		if (!excludedPaths.isEmpty()) {
			options.add("-XepExcludedPaths:" + excludedPaths);
		}

		return options;
	}

	/**
	 * Builds the {@code -XepOpt:NullAway:*} options keyed by option name. The options
	 * configured via {@link NullabilityConfiguration#nullAwayOptions()} are applied last
	 * so that they override the ones derived from the other parameters.
	 * @param forTests whether this is for test compilation
	 * @param config the nullability configuration
	 * @return the NullAway options keyed by option name, in emission order
	 */
	private static Map<String, String> buildNullAwayOptionMap(boolean forTests, NullabilityConfiguration config) {
		Map<String, String> options = new LinkedHashMap<>();
		options.put("OnlyNullMarked", "true");
		options.put("CheckContracts", "true");
		if (config.jspecifyMode()) {
			options.put("JSpecifyMode", "true");
		}

		if (config.customContractAnnotations() != null && !config.customContractAnnotations().isEmpty()) {
			options.put("CustomContractAnnotations", config.customContractAnnotations());
		}

		if (forTests) {
			options.put("HandleTestAssertionLibraries", "true");
		}

		options.putAll(config.nullAwayOptions());
		return options;
	}

	static String buildExcludedPaths(boolean forTests, NullabilityConfiguration config) {
		List<String> patterns = new ArrayList<>();
		if (!forTests) {
			patterns.add(".*/test/java/.*");
		}
		if (config.excludedPaths() != null && !config.excludedPaths().isEmpty()) {
			patterns.add(config.excludedPaths());
		}
		if (patterns.isEmpty()) {
			return "";
		}
		return "(" + String.join("|", patterns) + ")";
	}

}
