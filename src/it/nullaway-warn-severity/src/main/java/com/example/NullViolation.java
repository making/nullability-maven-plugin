package com.example;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class NullViolation {

	public String process(String input) {
		// This produces a NullAway warning (not error) because severity is set to WARN.
		return null;
	}

}
