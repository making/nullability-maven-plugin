package com.example;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class NullViolation {

	public String process(String input) {
		// This should trigger a NullAway error: returning null in a @NullMarked context
		return null;
	}

}
