package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;
import com.home.project.dragonrockets.repository.MissionRepository;
import com.home.project.dragonrockets.repository.RocketRepository;
import com.home.project.dragonrockets.repository.SpaceXRepository;

class SpaceXRepositoryTest {

	private SpaceXRepository spaceXRepository;
	private RocketRepository rocketRepository;
	private MissionRepository missionRepository;

	@BeforeEach
	void setUp() {
		rocketRepository = new RocketRepository();
		missionRepository = new MissionRepository();
		spaceXRepository = new SpaceXRepository(rocketRepository, missionRepository);
	}

	@Test
	void shouldAssignRocketToMissionSuccessfully() {
		// Given: a new rocket and a new mission
		String rocketName = "Falcon 9";
		String missionName = "Mars";
		rocketRepository.addRocket(new Rocket(rocketName));
		missionRepository.addMission(new Mission(missionName));

		// When: assigning the rocket to the mission
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// Then: verify the assignment and status changes
		Optional<Rocket> assignedRocketOptional = rocketRepository.findByName(rocketName);
		assertTrue(assignedRocketOptional.isPresent());

		Rocket assignedRocket = assignedRocketOptional.get();
		assertEquals(RocketStatus.IN_SPACE, assignedRocket.getStatus());
		assertEquals(missionName, assignedRocket.getAssignedMissionName());

		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());

		Mission updatedMission = updatedMissionOptional.get();
		assertEquals(1, updatedMission.getAssignedRockets().size());
		assertEquals(RocketStatus.IN_SPACE, updatedMission.getAssignedRockets().get(0).getStatus());
		assertEquals(MissionStatus.IN_PROGRESS, updatedMission.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenAssigningRocketAlreadyAssigned() {
		// Given: a rocket already assigned to a mission
		String rocketName = "Falcon 9";
		String mission1Name = "Mission Alpha";
		String mission2Name = "Mission Beta";
		rocketRepository.addRocket(new Rocket(rocketName));
		missionRepository.addMission(new Mission(mission1Name));
		missionRepository.addMission(new Mission(mission2Name));
		spaceXRepository.assignRocketToMission(rocketName, mission1Name);

		// When & Then: attempting to re-assign the rocket, an exception should be
		// thrown
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.assignRocketToMission(rocketName, mission2Name));
		assertEquals("Rocket 'Falcon 9' is already assigned to mission 'Mission Alpha'.", thrown.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenAssigningToEndedMission() {
		// Given: a mission with 'ENDED' status
		String rocketName = "Falcon Heavy";
		String missionName = "Old Mission";
		rocketRepository.addRocket(new Rocket(rocketName));
		Mission endedMission = new Mission(missionName);
		endedMission.setStatus(MissionStatus.ENDED);
		missionRepository.addMission(endedMission);

		// When & Then: attempting to assign a rocket to the ended mission, an exception
		// should be thrown
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.assignRocketToMission(rocketName, missionName));
		assertEquals("Cannot assign rockets to a mission with status 'Ended'.", thrown.getMessage());
	}

	@Test
	void shouldChangeRocketStatus() {
		// Given: a rocket with 'ON_GROUND' status
		String rocketName = "Starship";
		rocketRepository.addRocket(new Rocket(rocketName));

		// When: changing its status to 'IN_REPAIR'
		spaceXRepository.changeRocketStatus(rocketName, RocketStatus.IN_REPAIR);

		// Then: the status should be updated
		Optional<Rocket> updatedRocketOptional = rocketRepository.findByName(rocketName);
		assertTrue(updatedRocketOptional.isPresent());

		Rocket updatedRocket = updatedRocketOptional.get();
		assertNotNull(updatedRocket);
		assertEquals(RocketStatus.IN_REPAIR, updatedRocket.getStatus());
	}

	@Test
	void shouldChangeMissionStatusToPendingWhenRocketIsInRepair() {
		// Given: a mission with two rockets, both 'IN_SPACE'
		String missionName = "Luna 2";
		String rocket1Name = "Dragon 1";
		String rocket2Name = "Dragon 2";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocket1Name));
		rocketRepository.addRocket(new Rocket(rocket2Name));
		spaceXRepository.assignRocketToMission(rocket1Name, missionName);
		spaceXRepository.assignRocketToMission(rocket2Name, missionName);

		// When: one of the rockets goes to 'IN_REPAIR' status
		spaceXRepository.changeRocketStatus(rocket1Name, RocketStatus.IN_REPAIR);

		// Then: the mission status should automatically change to 'PENDING'
		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());

		Mission updatedMission = updatedMissionOptional.get();
		assertNotNull(updatedMission);
		assertEquals(MissionStatus.PENDING, updatedMission.getStatus());
	}

	@Test
	void shouldNotChangeMissionStatusFromPendingIfOtherRocketsAreInRepair() {
		// Given: a mission with one rocket 'IN_REPAIR' and one 'IN_SPACE'
		String missionName = "Luna 3";
		String rocket1Name = "Dragon 3";
		String rocket2Name = "Dragon 4";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocket1Name));
		rocketRepository.addRocket(new Rocket(rocket2Name));
		spaceXRepository.assignRocketToMission(rocket1Name, missionName);
		spaceXRepository.assignRocketToMission(rocket2Name, missionName);
		spaceXRepository.changeRocketStatus(rocket1Name, RocketStatus.IN_REPAIR); // Mission status is now Pending

		// When: the other rocket changes status to 'IN_SPACE'
		spaceXRepository.changeRocketStatus(rocket2Name, RocketStatus.IN_SPACE);

		// Then: the mission status should remain 'PENDING' because the first rocket is
		// still in repair
		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());

		Mission updatedMission = updatedMissionOptional.get();
		assertNotNull(updatedMission);
		assertEquals(MissionStatus.PENDING, updatedMission.getStatus());
	}

	@Test
	void shouldChangeMissionStatusToInProgress() {
		// Given: a mission with assigned rocket
		String missionName = "Mars";
		String rocketName = "Falcon 9";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocketName));

		// Check initial status SCHEDULED
		Optional<Mission> missionOptional = missionRepository.findByName(missionName);
		assertTrue(missionOptional.isPresent());
		Mission mission = missionOptional.get();
		assertEquals(MissionStatus.SCHEDULED, mission.getStatus());

		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// When: changing its status to IN_PROGRESS
		spaceXRepository.changeMissionStatus(missionName, MissionStatus.IN_PROGRESS);

		// Then: the status should be updated
		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());
		Mission updatedMission = updatedMissionOptional.get();
		assertNotNull(updatedMission);
		assertEquals(MissionStatus.IN_PROGRESS, updatedMission.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenChangingToEndedStatusWithAssignedRockets() {
		// Given: a mission with assigned rocket
		String missionName = "Mars";
		String rocketName = "Starship";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocketName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// When & Then: attempting to change status to Ended
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.changeMissionStatus(missionName, MissionStatus.ENDED));
		assertEquals(
				"Cannot change mission status to 'Ended' because some rockets are still assigned or not on ground.",
				thrown.getMessage());
	}

	@Test
	void shouldChangeMissionStatusToEndedWhenNoRocketsAreAssigned() {
		// Given: a mission with no rockets assigned
		String missionName = "Interstellar";
		missionRepository.addMission(new Mission(missionName));

		Optional<Mission> missionOptional = missionRepository.findByName(missionName);
		assertTrue(missionOptional.isPresent());
		Mission mission = missionOptional.get();
		assertEquals(MissionStatus.SCHEDULED, mission.getStatus());

		// When: changing its status to ENDED
		spaceXRepository.changeMissionStatus(missionName, MissionStatus.ENDED);

		// Then: the status should be updated
		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());
		Mission updatedMission = updatedMissionOptional.get();
		assertNotNull(updatedMission);
		assertEquals(MissionStatus.ENDED, updatedMission.getStatus());
	}

	@Test
	void shouldChangeMissionStatusToEndedWhenAllRocketsAreOnGround() {
		// Given: a mission with assigned rockets that are all 'ON_GROUND'
		String missionName = "Jupiter";
		String rocket1Name = "Dragon 1";
		String rocket2Name = "Dragon 2";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocket1Name));
		rocketRepository.addRocket(new Rocket(rocket2Name));
		spaceXRepository.assignRocketToMission(rocket1Name, missionName);
		spaceXRepository.assignRocketToMission(rocket2Name, missionName);

		// Simulating the end of the mission, returning rockets to "ON_GROUND"
		spaceXRepository.changeRocketStatus(rocket1Name, RocketStatus.ON_GROUND);
		spaceXRepository.changeRocketStatus(rocket2Name, RocketStatus.ON_GROUND);

		// When: changing its status to ENDED
		spaceXRepository.changeMissionStatus(missionName, MissionStatus.ENDED);

		// Then: the status should be updated
		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());
		Mission updatedMission = updatedMissionOptional.get();
		assertNotNull(updatedMission);
		assertEquals(MissionStatus.ENDED, updatedMission.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenChangingToScheduledStatus() {
		String missionName = "Test";
		String rocketName = "Starship";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocketName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.changeMissionStatus(missionName, MissionStatus.SCHEDULED));
		assertEquals("Status 'Scheduled' can only be set at mission creation.", thrown.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenChangingToPendingWithoutRocketsInRepair() {
		String missionName = "Jupiter";
		String rocketName = "Starship 1";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocketName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.changeMissionStatus(missionName, MissionStatus.PENDING));
		assertEquals("Cannot set status 'Pending' unless at least one assigned rocket is in 'In Repair' status.",
				thrown.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenChangingToInProgressWithRocketInRepair() {
		String missionName = "Saturn";
		String rocketName = "Falcon 9";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocketName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);
		spaceXRepository.changeRocketStatus(rocketName, RocketStatus.IN_REPAIR);

		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.changeMissionStatus(missionName, MissionStatus.IN_PROGRESS));
		assertEquals("Cannot set status 'In Progress' because at least one assigned rocket is in 'In Repair' status.",
				thrown.getMessage());
	}

}
