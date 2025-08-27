package com.home.project.dragonrockets.repository;

import java.util.Optional;

import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;

public class SpaceXRepository {

	private final RocketRepository rocketRepository;
	private final MissionRepository missionRepository;

	public SpaceXRepository(RocketRepository rocketRepository, MissionRepository missionRepository) {
		this.rocketRepository = rocketRepository;
		this.missionRepository = missionRepository;
	}

	public void assignRocketToMission(String rocketName, String missionName) {
		Optional<Rocket> rocketOptional = rocketRepository.findByName(rocketName);
		Optional<Mission> missionOptional = missionRepository.findByName(missionName);

		if (rocketOptional.isEmpty()) {
			throw new IllegalArgumentException("Rocket '" + rocketName + "' not found.");
		}
		if (missionOptional.isEmpty()) {
			throw new IllegalArgumentException("Mission '" + missionName + "' not found.");
		}

		Rocket rocket = rocketOptional.get();
		Mission mission = missionOptional.get();

		if (rocket.getAssignedMissionName() != null) {
			throw new IllegalArgumentException("Rocket '" + rocketName + "' is already assigned to mission '"
					+ rocket.getAssignedMissionName() + "'.");
		}

		if (mission.getStatus() == MissionStatus.ENDED) {
			throw new IllegalArgumentException("Cannot assign rockets to a mission with status 'Ended'.");
		}

		// Assign the rocket to the mission
		mission.getAssignedRockets().add(rocket);

		// Update rocket status and mission link
		rocket.setStatus(RocketStatus.IN_SPACE);
		rocket.setAssignedMissionName(missionName);

		// Update mission status
		if (mission.getStatus() == MissionStatus.SCHEDULED) {
			mission.setStatus(MissionStatus.IN_PROGRESS);
		}
	}

	public void changeRocketStatus(String rocketName, RocketStatus newStatus) {
		Rocket rocket = rocketRepository.findByName(rocketName)
				.orElseThrow(() -> new IllegalArgumentException("Rocket '" + rocketName + "' not found."));

		if (rocket.getStatus() != newStatus) {
			rocket.setStatus(newStatus);
		}

		if (rocket.getAssignedMissionName() != null) {
			Mission mission = missionRepository.findByName(rocket.getAssignedMissionName())
					.orElseThrow(() -> new IllegalStateException(
							"Assigned mission '" + rocket.getAssignedMissionName() + "' not found."));

			updateMissionStatusBasedOnRockets(mission);
		}
	}

	public void changeMissionStatus(String missionName, MissionStatus newStatus) {
		Mission mission = missionRepository.findByName(missionName)
				.orElseThrow(() -> new IllegalArgumentException("Mission '" + missionName + "' not found."));

		if (newStatus != MissionStatus.SCHEDULED && newStatus != MissionStatus.ENDED
				&& mission.getAssignedRockets().isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot set status '" + newStatus + "' on a mission with no assigned rockets.");
		}

		switch (newStatus) {
			case SCHEDULED:
				throw new IllegalArgumentException("Status 'Scheduled' can only be set at mission creation.");
			case PENDING:
				boolean hasRepairRockets = mission.getAssignedRockets().stream()
						.anyMatch(r -> r.getStatus() == RocketStatus.IN_REPAIR);
				if (!hasRepairRockets) {
					throw new IllegalArgumentException(
							"Cannot set status 'Pending' unless at least one assigned rocket is in 'In Repair' status.");
				}
				break;
			case IN_PROGRESS:
				boolean hasRepairRocketsForInProgress = mission.getAssignedRockets().stream()
						.anyMatch(r -> r.getStatus() == RocketStatus.IN_REPAIR);
				if (hasRepairRocketsForInProgress) {
					throw new IllegalArgumentException(
							"Cannot set status 'In Progress' because at least one assigned rocket is in 'In Repair' status.");
				}
				break;
			case ENDED:
				boolean hasAssignedRockets = !mission.getAssignedRockets().isEmpty();
				boolean areAllRocketsOnGround = mission.getAssignedRockets().stream()
						.allMatch(r -> r.getStatus() == RocketStatus.ON_GROUND);
				if (hasAssignedRockets && !areAllRocketsOnGround) {
					throw new IllegalArgumentException(
							"Cannot change mission status to 'Ended' because some rockets are still assigned or not on ground.");
				}
				break;
		}

		mission.setStatus(newStatus);
	}

	private void updateMissionStatusBasedOnRockets(Mission mission) {
		long rocketsInRepair = mission.getAssignedRockets().stream()
				.filter(r -> r.getStatus() == RocketStatus.IN_REPAIR)
				.count();

		if (rocketsInRepair > 0) {
			mission.setStatus(MissionStatus.PENDING);
		} else if (mission.getAssignedRockets().stream().anyMatch(r -> r.getStatus() == RocketStatus.IN_SPACE)) {
			mission.setStatus(MissionStatus.IN_PROGRESS);
		} else if (mission.getAssignedRockets().stream().allMatch(r -> r.getStatus() == RocketStatus.ON_GROUND)
				|| mission.getAssignedRockets().isEmpty()) {
			mission.setStatus(MissionStatus.SCHEDULED);
		}
	}

}
