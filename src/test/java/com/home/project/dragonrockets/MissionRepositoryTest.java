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

	@Test
	void shouldFindMissionByName() {
		String missionName = "Mars";
		missionRepository.addMission(new Mission(missionName));

		Optional<Mission> foundMission = missionRepository.findByName(missionName);
		assertTrue(foundMission.isPresent());
		assertEquals(missionName, foundMission.get().getName());
	}

	@Test
	void shouldNotFindNonexistentMission() {
		String missionName = "Mars";
		String nonExistentName = "Jupiter";
		missionRepository.addMission(new Mission(missionName));

		Optional<Mission> foundMission = missionRepository.findByName(nonExistentName);
		assertTrue(foundMission.isEmpty());
	}

	@Test
	void shouldRemoveMissionSuccessfully() {
		// Given: a mission exists in the repository
		String missionName = "Mars";
		missionRepository.addMission(new Mission(missionName));
		assertTrue(missionRepository.findByName(missionName).isPresent());

		// When: removing the mission
		missionRepository.remove(missionName);

		// Then: the mission should no longer be found
		assertTrue(missionRepository.findByName(missionName).isEmpty());
	}

	@Test
	void shouldNotThrowExceptionWhenRemovingNonexistentMission() {
		// Given: a nonexistent mission name
		String nonExistentName = "Nonexistent Mission";
		String existentName = "Mars";
		missionRepository.addMission(new Mission(existentName));
		assertTrue(missionRepository.findByName(existentName).isPresent());
		assertFalse(missionRepository.findByName(nonExistentName).isPresent());

		// When: attempting to remove it
		// Then: no exception should be thrown
		assertDoesNotThrow(() -> missionRepository.remove(nonExistentName));
	}
}
