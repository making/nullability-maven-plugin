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
 * Defines the checking mode for the nullability plugin.
 *
 * <ul>
 * <li>{@link #MAIN} - Check main sources only (default)</li>
 * <li>{@link #TESTS} - Check both main and test sources</li>
 * <li>{@link #DISABLED} - Disable all checking</li>
 * </ul>
 */
public enum Checking {

	/**
	 * Check main sources only.
	 */
	MAIN,

	/**
	 * Check both main and test sources.
	 */
	TESTS,

	/**
	 * Disable all checking.
	 */
	DISABLED;

	/**
	 * Returns {@code true} if main source checking is enabled.
	 * @return {@code true} unless this is {@link #DISABLED}
	 */
	public boolean isMainChecking() {
		return this != DISABLED;
	}

	/**
	 * Returns {@code true} if test source checking is enabled.
	 * @return {@code true} if this is {@link #TESTS}
	 */
	public boolean isTestChecking() {
		return this == TESTS;
	}

}
