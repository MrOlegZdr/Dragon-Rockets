package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.repository.MissionRepository;

class MissionRepositoryTest {

	private MissionRepository missionRepository;

	@BeforeEach
	void setUp() {
		missionRepository = new MissionRepository();
	}

	@Test
	void shouldAddMissionSuccessfully() {
		String missionName = "Mars";
		missionRepository.addMission(new Mission(missionName));

		Optional<Mission> addedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(addedMissionOptional.isPresent());

		Mission addedMission = addedMissionOptional.get();
		assertEquals(missionName, addedMission.getName());
		assertEquals(MissionStatus.SCHEDULED, addedMission.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenAddingDuplicateMission() {
		String missionName = "Mars";
		missionRepository.addMission(new Mission(missionName));

		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> missionRepository.addMission(new Mission(missionName)));

		assertEquals("Mission with name '" + missionName + "' already exists.", thrown.getMessage());
	}
}
