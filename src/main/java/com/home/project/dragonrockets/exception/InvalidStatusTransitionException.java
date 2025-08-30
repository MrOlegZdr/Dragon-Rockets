package com.home.project.dragonrockets.exception;

public class InvalidStatusTransitionException extends RuntimeException {

	private static final long serialVersionUID = 333995681059391445L;

	public InvalidStatusTransitionException(String message) {
		super(message);
	}

	public InvalidStatusTransitionException(String message, Throwable cause) {
		super(message, cause);
	}
}
