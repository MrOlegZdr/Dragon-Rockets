package com.home.project.dragonrockets.model;

public enum MissionStatus {

	SCHEDULED("Scheduled"),
	PENDING("Pending"),
	IN_PROGRESS("In Progress"),
	ENDED("Ended");

	private final String displayName;

	private MissionStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
