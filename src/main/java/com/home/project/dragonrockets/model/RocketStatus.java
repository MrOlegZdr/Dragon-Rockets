package com.home.project.dragonrockets.model;

public enum RocketStatus {

	ON_GROUND("On Ground"),
	IN_SPACE("In Space"),
	IN_REPAIR("In Repair");

	private final String displayName;

	private RocketStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
