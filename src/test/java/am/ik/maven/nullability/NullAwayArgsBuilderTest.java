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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NullAwayArgsBuilderTest {

	@Test
	void mainModeContainsBaseOptions() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).startsWith("-Xplugin:ErrorProne");
		assertThat(result).contains("-XepDisableAllChecks");
		assertThat(result).contains("-XepOpt:NullAway:OnlyNullMarked=true");
		assertThat(result).contains("-XepOpt:NullAway:CheckContracts=true");
		assertThat(result).contains("-XepOpt:NullAway:JSpecifyMode=true");
		assertThat(result).contains("-Xep:NullAway:ERROR");
	}

	@Test
	void mainModeWithRequireExplicitNullMarking() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-Xep:RequireExplicitNullMarking:ERROR");
	}

	@Test
	void mainModeWithoutRequireExplicitNullMarking() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().requireExplicitNullMarking(false).build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).doesNotContain("RequireExplicitNullMarking");
	}

	@Test
	void customContractAnnotationsEmpty() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).doesNotContain("CustomContractAnnotations");
	}

	@Test
	void customContractAnnotationsSet() {
		NullabilityConfiguration config = NullabilityConfiguration.builder()
			.customContractAnnotations("com.example.MyContract")
			.build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-XepOpt:NullAway:CustomContractAnnotations=com.example.MyContract");
	}

	@Test
	void mainModeDoesNotIncludeTestOptions() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).doesNotContain("HandleTestAssertionLibraries");
	}

	@Test
	void testsModeIncludesTestOptions() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(true, config);
		assertThat(result).contains("-XepOpt:NullAway:HandleTestAssertionLibraries=true");
	}

	@Test
	void mainModeIncludesExcludedPathsInXpluginArg() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-XepExcludedPaths:(.*/test/java/.*|.*/target/generated-sources/.*)");
	}

	@Test
	void testsModeExcludesTestPathsFromExcludedPaths() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(true, config);
		assertThat(result).contains("-XepExcludedPaths:(.*/target/generated-sources/.*)");
		assertThat(result).doesNotContain(".*/test/java/.*");
	}

	@Test
	void buildExcludedPathsForMainModeIncludesTestPaths() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.buildExcludedPaths(false, config);
		assertThat(result).isEqualTo("(.*/test/java/.*|.*/target/generated-sources/.*)");
	}

	@Test
	void buildExcludedPathsWithEmptyExcludedPaths() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().excludedPaths("").build();
		String result = NullAwayArgsBuilder.buildExcludedPaths(false, config);
		assertThat(result).isEqualTo("(.*/test/java/.*)");
	}

	@Test
	void buildExcludedPathsForTestsModeWithEmptyExcludedPaths() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().excludedPaths("").build();
		String result = NullAwayArgsBuilder.buildExcludedPaths(true, config);
		assertThat(result).isEmpty();
	}

	@Test
	void jspecifyModeDisabledExcludesJSpecifyOption() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().jspecifyMode(false).build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).doesNotContain("JSpecifyMode");
	}

	@Test
	void jspecifyModeDisabledExcludesJSpecifyOptionFromBuildNullAwayOptions() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().jspecifyMode(false).build();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).noneMatch(opt -> opt.contains("JSpecifyMode"));
	}

	@Test
	void buildNullAwayOptionsContainsBaseOptions() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).contains("-XepOpt:NullAway:OnlyNullMarked=true", "-XepOpt:NullAway:CheckContracts=true",
				"-XepOpt:NullAway:JSpecifyMode=true", "-Xep:NullAway:ERROR");
	}

	@Test
	void buildNullAwayOptionsDoesNotContainDisableAllChecks() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).noneMatch(opt -> opt.contains("XepDisableAllChecks"));
	}

	@Test
	void buildNullAwayOptionsDoesNotContainXpluginPrefix() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).noneMatch(opt -> opt.startsWith("-Xplugin:"));
	}

	@Test
	void buildNullAwayOptionsIncludesRequireExplicitNullMarking() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).contains("-Xep:RequireExplicitNullMarking:ERROR");
	}

	@Test
	void buildNullAwayOptionsExcludesRequireExplicitNullMarkingWhenDisabled() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().requireExplicitNullMarking(false).build();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).noneMatch(opt -> opt.contains("RequireExplicitNullMarking"));
	}

	@Test
	void buildNullAwayOptionsForTestsIncludesTestOptions() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(true, config);
		assertThat(options).contains("-XepOpt:NullAway:HandleTestAssertionLibraries=true");
	}

	@Test
	void buildNullAwayOptionsIncludesExcludedPaths() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		List<String> options = NullAwayArgsBuilder.buildNullAwayOptions(false, config);
		assertThat(options).anyMatch(opt -> opt.startsWith("-XepExcludedPaths:") && opt.contains(".*/test/java/.*"));
	}

	@Test
	void nullAwaySeverityDefaultsToError() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-Xep:NullAway:ERROR");
	}

	@Test
	void nullAwaySeverityWarn() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().nullAwaySeverity(Severity.WARN).build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-Xep:NullAway:WARN");
		assertThat(result).doesNotContain("-Xep:NullAway:ERROR");
	}

	@Test
	void nullAwaySeverityOff() {
		NullabilityConfiguration config = NullabilityConfiguration.builder().nullAwaySeverity(Severity.OFF).build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-Xep:NullAway:OFF");
		assertThat(result).doesNotContain("-Xep:NullAway:ERROR");
	}

	@Test
	void requireExplicitNullMarkingSeverityDefaultsToError() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-Xep:RequireExplicitNullMarking:ERROR");
	}

	@Test
	void requireExplicitNullMarkingSeverityWarn() {
		NullabilityConfiguration config = NullabilityConfiguration.builder()
			.requireExplicitNullMarkingSeverity(Severity.WARN)
			.build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).contains("-Xep:RequireExplicitNullMarking:WARN");
		assertThat(result).doesNotContain("-Xep:RequireExplicitNullMarking:ERROR");
	}

	@Test
	void requireExplicitNullMarkingSeverityIgnoredWhenDisabled() {
		NullabilityConfiguration config = NullabilityConfiguration.builder()
			.requireExplicitNullMarking(false)
			.requireExplicitNullMarkingSeverity(Severity.WARN)
			.build();
		String result = NullAwayArgsBuilder.build(false, config);
		assertThat(result).doesNotContain("RequireExplicitNullMarking");
	}

}
