package com.home.project.dragonrockets.exception;

public class MissionNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -4497669512254846169L;

	public MissionNotFoundException(String message) {
		super(message);
	}

	public MissionNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
