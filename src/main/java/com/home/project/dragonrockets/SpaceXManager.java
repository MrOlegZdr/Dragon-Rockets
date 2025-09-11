package com.home.project.dragonrockets;

import java.util.List;

import com.home.project.dragonrockets.internal.model.Mission;
import com.home.project.dragonrockets.internal.model.MissionStatus;
import com.home.project.dragonrockets.internal.model.Rocket;
import com.home.project.dragonrockets.internal.model.RocketStatus;
import com.home.project.dragonrockets.internal.repository.MissionRepository;
import com.home.project.dragonrockets.internal.repository.RocketRepository;
import com.home.project.dragonrockets.internal.service.MissionService;
import com.home.project.dragonrockets.internal.service.RocketService;

public class SpaceXManager {

	private final RocketService rocketService;
	private final MissionService missionService;
	private final RocketRepository rocketRepository = new RocketRepository();
	private final MissionRepository missionRepository = new MissionRepository();

	public SpaceXManager() {
		this.rocketService = new RocketService(rocketRepository, missionRepository);
		this.missionService = new MissionService(missionRepository, rocketRepository);
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

	public String getRocketInfo(String rocketName) {
		return rocketService.getRocketInfo(rocketName);
	}

	public void removeRocket(String rocketName) {
		rocketService.removeRocket(rocketName);
	}

	public void removeMission(String missionName) {
		missionService.removeMission(missionName);
	}
}
