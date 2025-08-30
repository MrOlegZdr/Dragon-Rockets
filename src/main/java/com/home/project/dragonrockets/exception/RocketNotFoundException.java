package com.home.project.dragonrockets.exception;

public class RocketNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 3399588656132675480L;

	public RocketNotFoundException(String message) {
		super(message);
	}

	public RocketNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
