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
 * ErrorProne check severity level.
 *
 * <ul>
 * <li>{@link #ERROR} - Treat violations as compilation errors (default)</li>
 * <li>{@link #WARN} - Report violations as warnings without failing the build</li>
 * <li>{@link #OFF} - Disable the check</li>
 * </ul>
 *
 * @since 0.4.0
 */
public enum Severity {

	/**
	 * Treat violations as compilation errors.
	 */
	ERROR,

	/**
	 * Report violations as warnings without failing the build.
	 */
	WARN,

	/**
	 * Disable the check.
	 */
	OFF

}
