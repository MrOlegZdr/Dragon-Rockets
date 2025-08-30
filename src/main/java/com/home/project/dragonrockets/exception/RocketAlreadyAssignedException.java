package com.home.project.dragonrockets.exception;

public class RocketAlreadyAssignedException extends RuntimeException {

	private static final long serialVersionUID = -8374817028201000955L;

	public RocketAlreadyAssignedException(String message) {
		super(message);
	}

	public RocketAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}
}
