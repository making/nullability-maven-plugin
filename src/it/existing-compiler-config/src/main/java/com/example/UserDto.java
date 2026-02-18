package com.example;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class UserDto {

	private String name = "";

	private @Nullable String email;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public @Nullable String getEmail() {
		return this.email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
	}

}
