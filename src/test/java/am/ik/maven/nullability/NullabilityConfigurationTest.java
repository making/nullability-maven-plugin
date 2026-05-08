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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NullabilityConfigurationTest {

	@Test
	void defaultsHaveExpectedValues() {
		NullabilityConfiguration config = NullabilityConfiguration.defaults();
		assertThat(config.errorProneVersion()).isEqualTo(NullabilityConfiguration.DEFAULT_ERROR_PRONE_VERSION);
		assertThat(config.nullAwayVersion()).isEqualTo(NullabilityConfiguration.DEFAULT_NULLAWAY_VERSION);
		assertThat(config.checking()).isEqualTo(Checking.MAIN);
		assertThat(config.requireExplicitNullMarking()).isTrue();
		assertThat(config.customContractAnnotations()).isEmpty();
		assertThat(config.jspecifyMode()).isTrue();
		assertThat(config.excludedPaths()).isEqualTo(".*/target/generated-sources/.*");
		assertThat(config.addTypeAnnotationsToSymbol()).isTrue();
	}

	@Test
	void customValuesArePreserved() {
		NullabilityConfiguration config = NullabilityConfiguration.builder()
			.errorProneVersion("2.46.0")
			.nullAwayVersion("0.12.0")
			.checking(Checking.TESTS)
			.requireExplicitNullMarking(false)
			.customContractAnnotations("com.example.MyContract")
			.jspecifyMode(false)
			.excludedPaths(".*/generated/.*")
			.addTypeAnnotationsToSymbol(false)
			.build();
		assertThat(config.errorProneVersion()).isEqualTo("2.46.0");
		assertThat(config.nullAwayVersion()).isEqualTo("0.12.0");
		assertThat(config.checking()).isEqualTo(Checking.TESTS);
		assertThat(config.requireExplicitNullMarking()).isFalse();
		assertThat(config.customContractAnnotations()).isEqualTo("com.example.MyContract");
		assertThat(config.jspecifyMode()).isFalse();
		assertThat(config.excludedPaths()).isEqualTo(".*/generated/.*");
		assertThat(config.addTypeAnnotationsToSymbol()).isFalse();
	}

}
