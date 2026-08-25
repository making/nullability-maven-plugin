package com.example;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class Service {

	private String greeting;

	// Without -XepOpt:NullAway:KnownInitializers, NullAway reports that the field
	// 'greeting' is not initialized.
	public void init() {
		this.greeting = "Hello";
	}

	public String greet(String name) {
		return this.greeting + ", " + name + "!";
	}

}
