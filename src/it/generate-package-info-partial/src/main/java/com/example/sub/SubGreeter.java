package com.example.sub;

public class SubGreeter {

	private final String name;

	public SubGreeter(String name) {
		this.name = name;
	}

	public String greet() {
		return "Hi, " + this.name + "!";
	}

}
