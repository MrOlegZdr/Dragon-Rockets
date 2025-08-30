package com.home.project.dragonrockets.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.home.project.dragonrockets.exception.InvalidStatusTransitionException;
import com.home.project.dragonrockets.exception.MissionHasAssignedRocketsException;
import com.home.project.dragonrockets.exception.MissionNotFoundException;
import com.home.project.dragonrockets.exception.RocketAlreadyAssignedException;
import com.home.project.dragonrockets.exception.RocketNotFoundException;
import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;
import com.home.project.dragonrockets.repository.MissionRepository;
import com.home.project.dragonrockets.repository.RocketRepository;

public class MissionService {

	private final MissionRepository missionRepository;
	private final RocketRepository rocketRepository;

	public MissionService(MissionRepository missionRepository, RocketRepository rocketRepository) {
		this.missionRepository = missionRepository;
		this.rocketRepository = rocketRepository;
	}

	public void addMission(Mission mission) {
		missionRepository.addMission(mission);
	}

	public Optional<Mission> findMissionByName(String missionName) {
		return missionRepository.findByName(missionName);
	}

	public void assignRocketToMission(String rocketName, String missionName) {
		Optional<Rocket> rocketOptional = rocketRepository.findByName(rocketName);
		Optional<Mission> missionOptional = missionRepository.findByName(missionName);

		if (rocketOptional.isEmpty()) {
			throw new RocketNotFoundException("Rocket '" + rocketName + "' not found.");
		}
		if (missionOptional.isEmpty()) {
			throw new MissionNotFoundException("Mission '" + missionName + "' not found.");
		}

		Rocket rocket = rocketOptional.get();
		Mission mission = missionOptional.get();

		if (rocket.getAssignedMissionName() != null) {
			throw new RocketAlreadyAssignedException("Rocket '" + rocketName + "' is already assigned to mission '"
					+ rocket.getAssignedMissionName() + "'.");
		}

		if (mission.getStatus() == MissionStatus.ENDED) {
			throw new InvalidStatusTransitionException("Cannot assign rockets to a mission with status 'Ended'.");
		}

		// Assign the rocket to the mission
		mission.getAssignedRockets().add(rocket);

		// Update rocket's mission link
		rocket.setAssignedMissionName(missionName);
	}

	public void unassignRocketFromMission(String rocketName) {
		Rocket rocket = rocketRepository.findByName(rocketName)
				.orElseThrow(() -> new RocketNotFoundException("Rocket '" + rocketName + "' not found."));

		if (rocket.getAssignedMissionName() == null) {
			throw new RocketAlreadyAssignedException("Rocket '" + rocketName + "' is not assigned to any mission.");
		}

		Mission mission = missionRepository.findByName(rocket.getAssignedMissionName())
				.orElseThrow(() -> new IllegalStateException(
						"Assigned mission '" + rocket.getAssignedMissionName() + "' not found."));

		// Remove the rocket from the mission's list
		mission.getAssignedRockets().remove(rocket);

		// Reset rocket's state
		rocket.setAssignedMissionName(null);
		rocket.setStatus(RocketStatus.ON_GROUND);
	}

	public void changeMissionStatus(String missionName, MissionStatus newStatus) {
		Mission mission = missionRepository.findByName(missionName)
				.orElseThrow(() -> new MissionNotFoundException("Mission '" + missionName + "' not found."));

		if (newStatus != MissionStatus.SCHEDULED && newStatus != MissionStatus.ENDED
				&& mission.getAssignedRockets().isEmpty()) {
			throw new MissionHasAssignedRocketsException(
					"Cannot set status '" + newStatus.getDisplayName() + "' on a mission with no assigned rockets.");
		}

		switch (newStatus) {
			case SCHEDULED:
				throw new InvalidStatusTransitionException("Status 'Scheduled' can only be set at mission creation.");
			case PENDING:
				boolean hasRepairRockets = mission.getAssignedRockets().stream()
						.anyMatch(r -> r.getStatus() == RocketStatus.IN_REPAIR);
				if (!hasRepairRockets) {
					throw new InvalidStatusTransitionException(
							"Cannot set status 'Pending' unless at least one assigned rocket is in 'In Repair' status.");
				}
				break;
			case IN_PROGRESS:
				boolean hasRepairRocketsForInProgress = mission.getAssignedRockets().stream()
						.anyMatch(r -> r.getStatus() == RocketStatus.IN_REPAIR);
				if (hasRepairRocketsForInProgress) {
					throw new InvalidStatusTransitionException(
							"Cannot set status 'In Progress' because at least one assigned rocket is in 'In Repair' status.");
				}
				break;
			case ENDED:
				if (!mission.getAssignedRockets().isEmpty()) {
					throw new InvalidStatusTransitionException(
							"Cannot change mission status to 'Ended' because rockets are still assigned. Please unassign all rockets first.");
				}
				break;
		}

		mission.setStatus(newStatus);
	}

	public List<String> getMissionSummary() {
		return missionRepository.findAll().stream()
				.sorted(Comparator.comparingInt((Mission m) -> m.getAssignedRockets().size()).reversed()
						.thenComparing(Mission::getName, Comparator.reverseOrder()))
				.flatMap(mission -> {
					String header = String.format("%s - %s - Dragons: %d",
							mission.getName(),
							mission.getStatus().getDisplayName(),
							mission.getAssignedRockets().size());

					List<String> lines = new ArrayList<>();
					lines.add(header);

					mission.getAssignedRockets().stream()
							.map(rocket -> String.format("\t- %s - %s", rocket.getName(),
									rocket.getStatus().getDisplayName()))
							.forEach(lines::add);

					return lines.stream();
				})
				.collect(Collectors.toList());
	}

	public void removeMission(String missionName) {
		Mission mission = missionRepository.findByName(missionName)
				.orElseThrow(() -> new MissionNotFoundException("Mission '" + missionName + "' not found."));

		if (!mission.getAssignedRockets().isEmpty()) {
			throw new MissionHasAssignedRocketsException(
					"Cannot remove mission '" + missionName + "' as it has assigned rockets.");
		}

		missionRepository.remove(missionName);
	}
}
