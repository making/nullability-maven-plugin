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

/**
 * Holds the resolved configuration for the nullability plugin.
 *
 * @param errorProneVersion the ErrorProne version to use
 * @param nullAwayVersion the NullAway version to use
 * @param mainChecking whether nullability checking is enabled for main sources
 * @param testChecking whether nullability checking is enabled for test sources
 * @param requireExplicitNullMarking whether to enable the
 * {@code RequireExplicitNullMarking} check
 * @param springContractSupport whether to add {@code org.springframework.lang.Contract}
 * to custom contract annotations
 * @param excludedPaths regex pattern for paths to exclude from checking
 */
public record NullabilityConfiguration(String errorProneVersion, String nullAwayVersion, boolean mainChecking,
		boolean testChecking, boolean requireExplicitNullMarking, boolean springContractSupport, String excludedPaths) {

	/**
	 * Default ErrorProne version.
	 */
	public static final String DEFAULT_ERROR_PRONE_VERSION = "2.47.0";

	/**
	 * Default NullAway version.
	 */
	public static final String DEFAULT_NULLAWAY_VERSION = "0.13.1";

	/**
	 * Default excluded paths pattern (generated sources).
	 */
	public static final String DEFAULT_EXCLUDED_PATHS = ".*/target/generated-sources/.*";

	/**
	 * Creates a configuration with default values.
	 * @return a new {@link NullabilityConfiguration} with defaults
	 */
	public static NullabilityConfiguration defaults() {
		return new NullabilityConfiguration(DEFAULT_ERROR_PRONE_VERSION, DEFAULT_NULLAWAY_VERSION, true, false, true,
				true, DEFAULT_EXCLUDED_PATHS);
	}

}
