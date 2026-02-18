package com.example;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class Greeter {

	private final String name;

	public Greeter(String name) {
		this.name = name;
	}

	public String greet() {
		return "Hello, " + this.name + "!";
	}

	@Override
	public String toString() {
		return "Greeter[name=" + this.name + "]";
	}

}
