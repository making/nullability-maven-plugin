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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Holds the resolved configuration for the nullability plugin.
 *
 * @param errorProneVersion the ErrorProne version to use
 * @param nullAwayVersion the NullAway version to use
 * @param checking the checking mode ({@link Checking#MAIN}, {@link Checking#TESTS}, or
 * {@link Checking#DISABLED})
 * @param requireExplicitNullMarking whether to enable the
 * {@code RequireExplicitNullMarking} check
 * @param customContractAnnotations comma-separated FQCNs of additional contract
 * annotations for NullAway
 * @param jspecifyMode whether to enable NullAway's JSpecify mode (requires JDK 22+, or
 * JDK 17.0.19+ / JDK 21.0.8+ on OpenJDK builds with the
 * {@code -XDaddTypeAnnotationsToSymbol=true} javac flag)
 * @param excludedPaths regex pattern for paths to exclude from checking
 * @param nullAwaySeverity severity level for the NullAway check (since 0.4.0)
 * @param requireExplicitNullMarkingSeverity severity level for the
 * {@code RequireExplicitNullMarking} check (since 0.4.0)
 * @param addTypeAnnotationsToSymbol whether to add the
 * {@code -XDaddTypeAnnotationsToSymbol=true} javac argument when JSpecify mode is enabled
 * (since 0.4.0)
 */
public record NullabilityConfiguration(String errorProneVersion, String nullAwayVersion, Checking checking,
		boolean requireExplicitNullMarking, String customContractAnnotations, boolean jspecifyMode,
		String excludedPaths, Severity nullAwaySeverity, Severity requireExplicitNullMarkingSeverity,
		boolean addTypeAnnotationsToSymbol) {

	private static final Properties DEFAULTS = loadDefaults();

	private static Properties loadDefaults() {
		Properties props = new Properties();
		try (InputStream is = NullabilityConfiguration.class.getResourceAsStream("defaults.properties")) {
			if (is == null) {
				throw new IllegalStateException(
						"defaults.properties not found. Run 'mvn process-resources' to generate it.");
			}
			props.load(is);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to load defaults.properties", ex);
		}
		return props;
	}

	/**
	 * Default ErrorProne version.
	 */
	public static final String DEFAULT_ERROR_PRONE_VERSION = DEFAULTS.getProperty("errorprone.version");

	/**
	 * Default NullAway version.
	 */
	public static final String DEFAULT_NULLAWAY_VERSION = DEFAULTS.getProperty("nullaway.version");

	/**
	 * Default excluded paths pattern (generated sources).
	 */
	public static final String DEFAULT_EXCLUDED_PATHS = ".*/target/generated-sources/.*";

	/**
	 * Creates a configuration with default values.
	 * @return a new {@link NullabilityConfiguration} with defaults
	 */
	public static NullabilityConfiguration defaults() {
		return builder().build();
	}

	/**
	 * Creates a new builder with default values.
	 * @return a new {@link Builder}
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for {@link NullabilityConfiguration}.
	 */
	public static final class Builder {

		private String errorProneVersion = DEFAULT_ERROR_PRONE_VERSION;

		private String nullAwayVersion = DEFAULT_NULLAWAY_VERSION;

		private Checking checking = Checking.MAIN;

		private boolean requireExplicitNullMarking = true;

		private String customContractAnnotations = "";

		private boolean jspecifyMode = true;

		private String excludedPaths = DEFAULT_EXCLUDED_PATHS;

		private Severity nullAwaySeverity = Severity.ERROR;

		private Severity requireExplicitNullMarkingSeverity = Severity.ERROR;

		private boolean addTypeAnnotationsToSymbol = true;

		private Builder() {
		}

		/**
		 * Sets the ErrorProne version.
		 * @param errorProneVersion the ErrorProne version to use
		 * @return this builder
		 */
		public Builder errorProneVersion(String errorProneVersion) {
			this.errorProneVersion = errorProneVersion;
			return this;
		}

		/**
		 * Sets the NullAway version.
		 * @param nullAwayVersion the NullAway version to use
		 * @return this builder
		 */
		public Builder nullAwayVersion(String nullAwayVersion) {
			this.nullAwayVersion = nullAwayVersion;
			return this;
		}

		/**
		 * Sets the checking mode.
		 * @param checking the checking mode
		 * @return this builder
		 */
		public Builder checking(Checking checking) {
			this.checking = checking;
			return this;
		}

		/**
		 * Sets whether to enable the {@code RequireExplicitNullMarking} check.
		 * @param requireExplicitNullMarking {@code true} to enable the check
		 * @return this builder
		 */
		public Builder requireExplicitNullMarking(boolean requireExplicitNullMarking) {
			this.requireExplicitNullMarking = requireExplicitNullMarking;
			return this;
		}

		/**
		 * Sets the comma-separated FQCNs of additional contract annotations for NullAway.
		 * @param customContractAnnotations comma-separated fully qualified class names
		 * @return this builder
		 */
		public Builder customContractAnnotations(String customContractAnnotations) {
			this.customContractAnnotations = customContractAnnotations;
			return this;
		}

		/**
		 * Sets whether to enable NullAway's JSpecify mode.
		 * @param jspecifyMode {@code true} to enable JSpecify mode
		 * @return this builder
		 */
		public Builder jspecifyMode(boolean jspecifyMode) {
			this.jspecifyMode = jspecifyMode;
			return this;
		}

		/**
		 * Sets the regex pattern for paths to exclude from checking.
		 * @param excludedPaths regex pattern for paths to exclude
		 * @return this builder
		 */
		public Builder excludedPaths(String excludedPaths) {
			this.excludedPaths = excludedPaths;
			return this;
		}

		/**
		 * Sets the severity level for the NullAway check.
		 * @param nullAwaySeverity severity level for NullAway
		 * @return this builder
		 * @since 0.4.0
		 */
		public Builder nullAwaySeverity(Severity nullAwaySeverity) {
			this.nullAwaySeverity = nullAwaySeverity;
			return this;
		}

		/**
		 * Sets the severity level for the {@code RequireExplicitNullMarking} check.
		 * @param requireExplicitNullMarkingSeverity severity level for
		 * {@code RequireExplicitNullMarking}
		 * @return this builder
		 * @since 0.4.0
		 */
		public Builder requireExplicitNullMarkingSeverity(Severity requireExplicitNullMarkingSeverity) {
			this.requireExplicitNullMarkingSeverity = requireExplicitNullMarkingSeverity;
			return this;
		}

		/**
		 * Sets whether to add the {@code -XDaddTypeAnnotationsToSymbol=true} javac
		 * argument when JSpecify mode is enabled.
		 * @param addTypeAnnotationsToSymbol {@code true} to add the flag
		 * @return this builder
		 * @since 0.4.0
		 */
		public Builder addTypeAnnotationsToSymbol(boolean addTypeAnnotationsToSymbol) {
			this.addTypeAnnotationsToSymbol = addTypeAnnotationsToSymbol;
			return this;
		}

		/**
		 * Builds a new {@link NullabilityConfiguration} from the current builder state.
		 * @return a new {@link NullabilityConfiguration}
		 */
		public NullabilityConfiguration build() {
			return new NullabilityConfiguration(this.errorProneVersion, this.nullAwayVersion, this.checking,
					this.requireExplicitNullMarking, this.customContractAnnotations, this.jspecifyMode,
					this.excludedPaths, this.nullAwaySeverity, this.requireExplicitNullMarkingSeverity,
					this.addTypeAnnotationsToSymbol);
		}

	}

}
