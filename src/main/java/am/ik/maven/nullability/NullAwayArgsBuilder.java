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
import java.util.List;
import java.util.StringJoiner;

/**
 * Builds the {@code -Xplugin:ErrorProne} argument string for main or test compilation.
 */
public final class NullAwayArgsBuilder {

	private static final String SPRING_CONTRACT = "org.springframework.lang.Contract";

	private static final String ASSERTJ_CONTRACT = "org.assertj.core.api.ThrowableAssert.ThrowingCallable";

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
		options.add("-XepOpt:NullAway:OnlyNullMarked=true");
		options.add("-XepOpt:NullAway:CheckContracts=true");
		options.add("-XepOpt:NullAway:JSpecifyMode=true");

		String customContracts = buildCustomContractAnnotations(forTests, config.springContractSupport());
		if (!customContracts.isEmpty()) {
			options.add("-XepOpt:NullAway:CustomContractAnnotations=" + customContracts);
		}

		if (forTests) {
			options.add("-XepOpt:NullAway:HandleTestAssertionLibraries=true");
		}

		options.add("-Xep:NullAway:ERROR");

		if (config.requireExplicitNullMarking()) {
			options.add("-Xep:RequireExplicitNullMarking:ERROR");
		}

		String excludedPaths = buildExcludedPaths(forTests, config);
		if (!excludedPaths.isEmpty()) {
			options.add("-XepExcludedPaths:" + excludedPaths);
		}

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

	private static String buildCustomContractAnnotations(boolean forTests, boolean springContractSupport) {
		StringJoiner joiner = new StringJoiner(",");
		if (springContractSupport) {
			joiner.add(SPRING_CONTRACT);
		}
		if (forTests) {
			joiner.add(ASSERTJ_CONTRACT);
		}
		return joiner.toString();
	}

}
