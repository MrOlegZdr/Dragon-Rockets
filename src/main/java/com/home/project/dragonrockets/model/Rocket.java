package com.home.project.dragonrockets.model;

public class Rocket {

	private final String name;
	private RocketStatus status;

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

	@Override
	public String toString() {
		return "Rocket [name=" + name + ", status=" + status + "]";
	}
}
