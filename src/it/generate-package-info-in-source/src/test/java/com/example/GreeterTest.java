package com.example;

import org.junit.jupiter.api.Test;

class GreeterTest {

	@Test
	void greet() {
		Greeter greeter = new Greeter("World");
		String result = greeter.greet();
		assert result.equals("Hello, World!");
	}

}
