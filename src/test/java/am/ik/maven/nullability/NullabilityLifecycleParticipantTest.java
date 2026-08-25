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

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class NullabilityLifecycleParticipantTest {

	private final NullabilityLifecycleParticipant participant = new NullabilityLifecycleParticipant();

	@Test
	void noNullAwayOptionsByDefault() throws Exception {
		NullabilityConfiguration config = this.participant.parseConfiguration(new Plugin(), new MavenProject());
		assertThat(config.nullAwayOptions()).isEmpty();
	}

	@Test
	void nullAwayOptionsFromConfiguration() throws Exception {
		Plugin plugin = pluginWithNullAwayOptions(
				new String[][] { { "KnownInitializers", "com.example.Service.init,com.example.Other.setUp" },
						{ "TreatGeneratedAsUnannotated", "true" } });

		NullabilityConfiguration config = this.participant.parseConfiguration(plugin, new MavenProject());

		assertThat(config.nullAwayOptions()).containsExactly(
				entry("KnownInitializers", "com.example.Service.init,com.example.Other.setUp"),
				entry("TreatGeneratedAsUnannotated", "true"));
	}

	@Test
	void nullAwayOptionValuesAreTrimmed() throws Exception {
		Plugin plugin = pluginWithNullAwayOptions(
				new String[][] { { "KnownInitializers", "\n\tcom.example.Service.init\n" } });

		NullabilityConfiguration config = this.participant.parseConfiguration(plugin, new MavenProject());

		assertThat(config.nullAwayOptions()).containsExactly(entry("KnownInitializers", "com.example.Service.init"));
	}

	@Test
	void nullAwayOptionsFromProjectProperties() throws Exception {
		MavenProject project = new MavenProject();
		project.getProperties()
			.setProperty("nullability.nullAwayOptions.KnownInitializers", "com.example.Service.init");

		NullabilityConfiguration config = this.participant.parseConfiguration(new Plugin(), project);

		assertThat(config.nullAwayOptions()).containsExactly(entry("KnownInitializers", "com.example.Service.init"));
	}

	@Test
	void configurationOverridesProjectProperty() throws Exception {
		MavenProject project = new MavenProject();
		project.getProperties().setProperty("nullability.nullAwayOptions.KnownInitializers", "com.example.Other.setUp");
		Plugin plugin = pluginWithNullAwayOptions(
				new String[][] { { "KnownInitializers", "com.example.Service.init" } });

		NullabilityConfiguration config = this.participant.parseConfiguration(plugin, project);

		assertThat(config.nullAwayOptions()).containsExactly(entry("KnownInitializers", "com.example.Service.init"));
	}

	@Test
	void rejectsNullAwayOptionWithoutValue() {
		Plugin plugin = pluginWithNullAwayOptions(new String[][] { { "KnownInitializers", null } });

		assertThatThrownBy(() -> this.participant.parseConfiguration(plugin, new MavenProject()))
			.isInstanceOf(MavenExecutionException.class)
			.hasMessageContaining("must have a name and a value");
	}

	@Test
	void rejectsNullAwayOptionValueWithWhitespace() {
		Plugin plugin = pluginWithNullAwayOptions(
				new String[][] { { "KnownInitializers", "com.example.Service.init com.example.Other.setUp" } });

		assertThatThrownBy(() -> this.participant.parseConfiguration(plugin, new MavenProject()))
			.isInstanceOf(MavenExecutionException.class)
			.hasMessageContaining("contains")
			.hasMessageContaining("whitespace");
	}

	private static Plugin pluginWithNullAwayOptions(String[][] options) {
		Xpp3Dom configuration = new Xpp3Dom("configuration");
		Xpp3Dom nullAwayOptions = new Xpp3Dom("nullAwayOptions");
		for (String[] option : options) {
			Xpp3Dom child = new Xpp3Dom(option[0]);
			child.setValue(option[1]);
			nullAwayOptions.addChild(child);
		}
		configuration.addChild(nullAwayOptions);
		Plugin plugin = new Plugin();
		plugin.setConfiguration(configuration);
		return plugin;
	}

}
