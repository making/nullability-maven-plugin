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

import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * No-op goal that serves as the entry point for the nullability plugin. The actual
 * configuration is performed by {@link NullabilityLifecycleParticipant} which runs before
 * the lifecycle execution plan is computed.
 */
@Mojo(name = "configure", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class ConfigureMojo extends AbstractMojo {

	/**
	 * Creates a new {@code ConfigureMojo}. Instantiated by Maven; not intended for direct
	 * use.
	 */
	public ConfigureMojo() {
	}

	/**
	 * The ErrorProne version to use.
	 */
	@Parameter(property = "nullability.errorProneVersion")
	private String errorProneVersion;

	/**
	 * The NullAway version to use.
	 */
	@Parameter(property = "nullability.nullAwayVersion")
	private String nullAwayVersion;

	/**
	 * Checking mode: {@code main} (default), {@code tests}, or {@code disabled}.
	 */
	@Parameter(property = "nullability.checking", defaultValue = "main")
	private String checking;

	/**
	 * Whether to enable the {@code RequireExplicitNullMarking} check.
	 */
	@Parameter(property = "nullability.requireExplicitNullMarking", defaultValue = "true")
	private boolean requireExplicitNullMarking;

	/**
	 * Comma-separated fully qualified class names of additional contract annotations for
	 * NullAway. Since NullAway 0.12.11, annotations named {@code @Contract} are
	 * auto-recognized regardless of package, so this is only needed for non-standard
	 * annotation names.
	 */
	@Parameter(property = "nullability.customContractAnnotations", defaultValue = "")
	private String customContractAnnotations;

	/**
	 * Whether to enable NullAway's JSpecify mode. When enabled, NullAway uses JSpecify
	 * semantics for nullability checking. Requires JDK 22+, or JDK 17.0.19+ / JDK 21.0.8+
	 * with the {@code -XDaddTypeAnnotationsToSymbol=true} flag (OpenJDK-based
	 * distributions only). Disabling this allows running on older JDKs at the cost of
	 * reduced checking capabilities.
	 */
	@Parameter(property = "nullability.jspecifyMode", defaultValue = "true")
	private boolean jspecifyMode;

	/**
	 * Whether to add the {@code -XDaddTypeAnnotationsToSymbol=true} javac argument when
	 * NullAway's JSpecify mode is enabled. Required on JDK 17.0.19+ and JDK 21.0.8+
	 * (OpenJDK-based distributions only) so that NullAway can read type-use annotations
	 * from bytecodes. No-op on JDK 22+. Set to {@code false} when running on a JDK that
	 * does not support this flag (e.g., Oracle JDK 17 or 21).
	 *
	 * @since 0.4.0
	 */
	@Parameter(property = "nullability.addTypeAnnotationsToSymbol", defaultValue = "true")
	private boolean addTypeAnnotationsToSymbol;

	/**
	 * Regex pattern for paths to exclude from checking.
	 */
	@Parameter(property = "nullability.excludedPaths", defaultValue = NullabilityConfiguration.DEFAULT_EXCLUDED_PATHS)
	private String excludedPaths;

	/**
	 * Severity level for the NullAway check. Accepted values: {@code error} (default),
	 * {@code warn}, {@code off}.
	 *
	 * @since 0.4.0
	 */
	@Parameter(property = "nullability.nullAwaySeverity", defaultValue = "error")
	private String nullAwaySeverity;

	/**
	 * Severity level for the {@code RequireExplicitNullMarking} check. Accepted values:
	 * {@code error} (default), {@code warn}, {@code off}.
	 *
	 * @since 0.4.0
	 */
	@Parameter(property = "nullability.requireExplicitNullMarkingSeverity", defaultValue = "error")
	private String requireExplicitNullMarkingSeverity;

	/**
	 * Additional NullAway options keyed by option name. Each entry is passed to
	 * ErrorProne as {@code -XepOpt:NullAway:<name>=<value>}, so that any NullAway option
	 * can be set without configuring the {@code maven-compiler-plugin} by hand. An entry
	 * overrides the option of the same name derived from the other parameters. Option
	 * names and values must not contain whitespace.
	 *
	 * @since 0.5.0
	 */
	@Parameter
	private Map<String, String> nullAwayOptions;

	/**
	 * Whether to skip the plugin execution.
	 */
	@Parameter(property = "nullability.skip", defaultValue = "false")
	private boolean skip;

	@Override
	public void execute() {
		// Configuration is handled by NullabilityLifecycleParticipant.
		// This Mojo exists to provide parameter documentation and a goal for the
		// execution.
		if (this.skip) {
			getLog().info("Nullability plugin is skipped.");
		}
	}

}
