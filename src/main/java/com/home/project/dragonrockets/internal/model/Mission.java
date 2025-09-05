package com.home.project.dragonrockets.internal.model;

import java.util.ArrayList;
import java.util.List;

public class Mission {

	private final String name;
	private final List<Rocket> assignedRockets;
	private MissionStatus status;

	public Mission(String name) {
		this.name = name;
		this.assignedRockets = new ArrayList<>();
		status = MissionStatus.SCHEDULED;
	}

	public String getName() {
		return name;
	}

	public MissionStatus getStatus() {
		return status;
	}

	public void setStatus(MissionStatus status) {
		this.status = status;
	}

	public List<Rocket> getAssignedRockets() {
		return assignedRockets;
	}

	@Override
	public String toString() {
		return "Mission [name=" + name + ", status=" + status + ", assignedRockets=" + assignedRockets + "]";
	}

}
