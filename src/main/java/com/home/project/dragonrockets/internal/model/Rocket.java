package com.home.project.dragonrockets.internal.model;

public class Rocket {

	private final String name;
	private RocketStatus status;
	private String assignedMissionName;

	public Rocket(String name) {
		this.name = name;
		status = RocketStatus.ON_GROUND;
	}

	public String getName() {
		return name;
	}

	public RocketStatus getStatus() {
		return status;
	}

	public void setStatus(RocketStatus status) {
		this.status = status;
	}

	public String getAssignedMissionName() {
		return assignedMissionName;
	}

	public void setAssignedMissionName(String assignedMissionName) {
		this.assignedMissionName = assignedMissionName;
	}

	@Override
	public String toString() {
		return "Rocket [name=" + name + ", status=" + status + ", assignedMissionName=" + assignedMissionName + "]";
	}

}
