package com.example;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class GreeterTest {

	public String testGreeting() {
		// NullAway error: returning null in @NullMarked context
		return null;
	}

}
