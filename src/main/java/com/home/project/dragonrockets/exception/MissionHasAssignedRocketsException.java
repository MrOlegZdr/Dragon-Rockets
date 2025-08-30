package com.home.project.dragonrockets.exception;

public class MissionHasAssignedRocketsException extends RuntimeException {

	private static final long serialVersionUID = -7424351131967086272L;

	public MissionHasAssignedRocketsException(String message) {
		super(message);
	}

	public MissionHasAssignedRocketsException(String message, Throwable cause) {
		super(message, cause);
	}
}
