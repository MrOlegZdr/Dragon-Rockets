package com.home.project.dragonrockets.internal.service;

import java.util.Optional;

import com.home.project.dragonrockets.internal.exception.MissionNotFoundException;
import com.home.project.dragonrockets.internal.exception.RocketAlreadyAssignedException;
import com.home.project.dragonrockets.internal.exception.RocketNotFoundException;
import com.home.project.dragonrockets.internal.model.Mission;
import com.home.project.dragonrockets.internal.model.MissionStatus;
import com.home.project.dragonrockets.internal.model.Rocket;
import com.home.project.dragonrockets.internal.model.RocketStatus;
import com.home.project.dragonrockets.internal.repository.MissionRepository;
import com.home.project.dragonrockets.internal.repository.RocketRepository;

public class RocketService {

	private final RocketRepository rocketRepository;
	private final MissionRepository missionRepository;

	public RocketService(RocketRepository rocketRepository, MissionRepository missionRepository) {
		this.rocketRepository = rocketRepository;
		this.missionRepository = missionRepository;
	}

	public void addRocket(Rocket rocket) {
		rocketRepository.addRocket(rocket);
	}

	public Optional<Rocket> findRocketByName(String rocketName) {
		return rocketRepository.findByName(rocketName);
	}

	public String getRocketInfo(String rocketName) {
		Rocket rocket = rocketRepository.findByName(rocketName)
				.orElseThrow(() -> new RocketNotFoundException("Rocket '" + rocketName + "' not found."));
		String rocketInfo = String.format("%s - %s - Mission: %s",
				rocket.getName(),
				rocket.getStatus().getDisplayName(),
				rocket.getAssignedMissionName() == null ? "NOT ASSIGNED" : rocket.getAssignedMissionName());
		return rocketInfo;
	}

	public void changeRocketStatus(String rocketName, RocketStatus newStatus) {
		Rocket rocket = rocketRepository.findByName(rocketName)
				.orElseThrow(() -> new RocketNotFoundException("Rocket '" + rocketName + "' not found."));

		if (rocket.getStatus() != newStatus) {
			rocket.setStatus(newStatus);
		}

		if (rocket.getAssignedMissionName() != null) {
			Mission mission = missionRepository.findByName(rocket.getAssignedMissionName())
					.orElseThrow(() -> new MissionNotFoundException(
							"Assigned mission '" + rocket.getAssignedMissionName() + "' not found."));

			updateMissionStatusBasedOnRockets(mission);
		}
	}

	public void removeRocket(String rocketName) {
		Rocket rocket = rocketRepository.findByName(rocketName)
				.orElseThrow(() -> new RocketNotFoundException("Rocket '" + rocketName + "' not found."));

		if (rocket.getAssignedMissionName() != null) {
			throw new RocketAlreadyAssignedException("Cannot remove rocket '" + rocketName
					+ "' as it is currently assigned to mission '" + rocket.getAssignedMissionName() + "'.");
		}

		rocketRepository.remove(rocketName);
	}

	private void updateMissionStatusBasedOnRockets(Mission mission) {
		long rocketsInRepair = mission.getAssignedRockets().stream()
				.filter(r -> r.getStatus() == RocketStatus.IN_REPAIR)
				.count();

		if (rocketsInRepair > 0) {
			mission.setStatus(MissionStatus.PENDING);
		}
	}
}
