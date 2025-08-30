package com.home.project.dragonrockets;

import java.util.List;

import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;
import com.home.project.dragonrockets.service.MissionService;
import com.home.project.dragonrockets.service.RocketService;

public class SpaceXManager {

	private final RocketService rocketService;
	private final MissionService missionService;

	public SpaceXManager(RocketService rocketService, MissionService missionService) {
		this.rocketService = rocketService;
		this.missionService = missionService;
	}

	public void addRocket(Rocket rocket) {
		rocketService.addRocket(rocket);
	}

	public void addMission(Mission mission) {
		missionService.addMission(mission);
	}

	public void assignRocketToMission(String rocketName, String missionName) {
		missionService.assignRocketToMission(rocketName, missionName);
	}

	public void unassignRocketFromMission(String rocketName) {
		missionService.unassignRocketFromMission(rocketName);
	}

	public void changeRocketStatus(String rocketName, RocketStatus newStatus) {
		rocketService.changeRocketStatus(rocketName, newStatus);
	}

	public void changeMissionStatus(String missionName, MissionStatus newStatus) {
		missionService.changeMissionStatus(missionName, newStatus);
	}

	public List<String> getMissionSummary() {
		return missionService.getMissionSummary();
	}

	public void removeRocket(String rocketName) {
		rocketService.removeRocket(rocketName);
	}

	public void removeMission(String missionName) {
		missionService.removeMission(missionName);
	}
}
