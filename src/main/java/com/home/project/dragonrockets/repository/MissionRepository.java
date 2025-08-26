package com.home.project.dragonrockets.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.home.project.dragonrockets.model.Mission;

public class MissionRepository {

	private final Map<String, Mission> missions = new HashMap<>();

	public void addMission(Mission mission) {
		if (missions.containsKey(mission.getName())) {
			throw new IllegalArgumentException("Mission with name '" + mission.getName() + "' already exists.");
		}
		missions.put(mission.getName(), mission);
	}

	public Optional<Mission> findByName(String name) {
		return Optional.ofNullable(missions.get(name));
	}
}
