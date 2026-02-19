package com.example;

public class Greeter {

	private final String name;

	public Greeter(String name) {
		this.name = name;
	}

	public String greet() {
		return "Hello, " + this.name + "!";
	}

}
