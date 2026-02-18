package com.example;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class UserEntity {

	private final String name;

	private final @Nullable String email;

	public UserEntity(String name, @Nullable String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return this.name;
	}

	public @Nullable String getEmail() {
		return this.email;
	}

}
