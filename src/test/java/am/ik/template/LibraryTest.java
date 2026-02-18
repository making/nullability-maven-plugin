package am.ik.template;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryTest {

	@Test
	void hello() {
		assertThat(Library.hello()).isEqualTo("Hello World!");
	}

}