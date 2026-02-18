package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

@NullMarked
public class GreeterTest {

	@Test
	void assertThatIsNotNullNarrowsType() {
		// HandleTestAssertionLibraries=true enables NullAway to understand
		// that assertThat(...).isNotNull() acts as a null check
		@Nullable String name = getNullableName();
		assertThat(name).isNotNull();
		// Without HandleTestAssertionLibraries, NullAway would flag this as
		// passing @Nullable to @NonNull parameter
		Greeter greeter = new Greeter(name);
		assertThat(greeter.greet()).contains("World");
	}

	@Test
	void failMethodContractNarrowsType() {
		// AssertJ @Contract("_ -> fail") tells NullAway that fail() never returns,
		// so code after fail() is unreachable and null narrowing applies
		@Nullable String name = getNullableName();
		if (name == null) {
			fail("name must not be null");
		}
		Greeter greeter = new Greeter(name);
		assertThat(greeter.greet()).contains("World");
	}

	private @Nullable String getNullableName() {
		return "World";
	}

}
