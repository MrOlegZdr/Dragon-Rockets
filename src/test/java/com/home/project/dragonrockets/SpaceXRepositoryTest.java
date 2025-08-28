package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
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
		assertEquals(RocketStatus.ON_GROUND, assignedRocket.getStatus());
		assertEquals(missionName, assignedRocket.getAssignedMissionName());

		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
		assertTrue(updatedMissionOptional.isPresent());

		Mission updatedMission = updatedMissionOptional.get();
		assertEquals(1, updatedMission.getAssignedRockets().size());
		assertEquals(RocketStatus.ON_GROUND, updatedMission.getAssignedRockets().get(0).getStatus());
		assertEquals(MissionStatus.SCHEDULED, updatedMission.getStatus());
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
	void shouldUnassignRocketFromMissionSuccessfully() {
		// Given: a rocket assigned to a mission
		String rocketName = "Dragon 1";
		String missionName = "Mars";
		rocketRepository.addRocket(new Rocket(rocketName));
		missionRepository.addMission(new Mission(missionName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// When: un-assigning the rocket
		spaceXRepository.unassignRocketFromMission(rocketName);

		// Then: the rocket should be unassigned and its status should be ON_GROUND
		Optional<Rocket> unassignedRocketOptional = rocketRepository.findByName(rocketName);
		assertTrue(unassignedRocketOptional.isPresent());
		Rocket unassignedRocket = unassignedRocketOptional.get();
		assertNull(unassignedRocket.getAssignedMissionName());
		assertEquals(RocketStatus.ON_GROUND, unassignedRocket.getStatus());

		Optional<Mission> missionOptional = missionRepository.findByName(missionName);
		assertTrue(missionOptional.isPresent());
		Mission mission = missionOptional.get();
		assertNotNull(mission);
		assertEquals(0, mission.getAssignedRockets().size());
	}

	@Test
	void shouldThrowExceptionWhenUnassigningRocketNotAssignedToAnyMission() {
		// Given: a rocket not assigned to any mission
		String rocketName = "Falcon Heavy";
		rocketRepository.addRocket(new Rocket(rocketName));

		// When & Then: attempting to un-assign it, an exception should be thrown
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.unassignRocketFromMission(rocketName));
		assertEquals("Rocket 'Falcon Heavy' is not assigned to any mission.", thrown.getMessage());
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
				"Cannot change mission status to 'Ended' because rockets are still assigned. Please unassign all rockets first.",
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
	void shouldAllowMissionToEndAfterUnassigningAllRockets() {
		// Given: a mission with one assigned rocket
		String missionName = "Moon Landing";
		String rocketName = "Starship 1";
		rocketRepository.addRocket(new Rocket(rocketName));
		missionRepository.addMission(new Mission(missionName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// When: un-assigning the last rocket and then changing mission status to Ended
		spaceXRepository.unassignRocketFromMission(rocketName);
		spaceXRepository.changeMissionStatus(missionName, MissionStatus.ENDED);

		// Then: the mission status should be updated successfully
		Optional<Mission> updatedMissionOptional = missionRepository.findByName(missionName);
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

	@Test
	void shouldReturnCorrectMissionSummary() {
		// Given: data from the example in the task
		// Missions
		missionRepository.addMission(new Mission("Mars"));
		missionRepository.addMission(new Mission("Luna1"));
		missionRepository.addMission(new Mission("Double Landing"));
		missionRepository.addMission(new Mission("Transit"));
		missionRepository.addMission(new Mission("Luna2"));
		missionRepository.addMission(new Mission("Vertical Landing"));

		// Rockets
		rocketRepository.addRocket(new Rocket("Dragon 1"));
		rocketRepository.addRocket(new Rocket("Dragon 2"));
		rocketRepository.addRocket(new Rocket("Red Dragon"));
		rocketRepository.addRocket(new Rocket("Dragon XL"));
		rocketRepository.addRocket(new Rocket("Falcon Heavy"));

		// Assignments and status changes
		spaceXRepository.assignRocketToMission("Dragon 1", "Luna1");
		spaceXRepository.assignRocketToMission("Dragon 2", "Luna1");
		spaceXRepository.assignRocketToMission("Red Dragon", "Transit");
		spaceXRepository.assignRocketToMission("Dragon XL", "Transit");
		spaceXRepository.assignRocketToMission("Falcon Heavy", "Transit");

		spaceXRepository.changeRocketStatus("Dragon XL", RocketStatus.IN_SPACE);
		spaceXRepository.changeRocketStatus("Falcon Heavy", RocketStatus.IN_SPACE);
		spaceXRepository.changeMissionStatus("Transit", MissionStatus.IN_PROGRESS);

		missionRepository.findByName("Double Landing").get().setStatus(MissionStatus.ENDED);
		missionRepository.findByName("Vertical Landing").get().setStatus(MissionStatus.ENDED);

		// When: generating the summary
		List<String> summary = spaceXRepository.getMissionSummary();

		// Then: verify the summary matches the expected output
		List<String> expectedSummary = List.of(
				"Transit - IN_PROGRESS - Dragons: 3",
				"\t- Red Dragon - ON_GROUND",
				"\t- Dragon XL - IN_SPACE",
				"\t- Falcon Heavy - IN_SPACE",
				"Luna1 - SCHEDULED - Dragons: 2",
				"\t- Dragon 1 - ON_GROUND",
				"\t- Dragon 2 - ON_GROUND",
				"Vertical Landing - ENDED - Dragons: 0",
				"Mars - SCHEDULED - Dragons: 0",
				"Luna2 - SCHEDULED - Dragons: 0",
				"Double Landing - ENDED - Dragons: 0");

		assertEquals(expectedSummary.size(), summary.size());
		for (int i = 0; i < expectedSummary.size(); i++) {
			assertEquals(expectedSummary.get(i), summary.get(i));
		}
	}

	@Test
	void shouldRemoveRocketSuccessfully() {
		// Given: a rocket not assigned to any mission
		String rocketName = "Falcon 1";
		rocketRepository.addRocket(new Rocket(rocketName));

		// When: attempting to remove the rocket
		spaceXRepository.removeRocket(rocketName);

		// Then: the rocket should no longer be in the repository
		Optional<Rocket> removedRocket = rocketRepository.findByName(rocketName);
		assertTrue(removedRocket.isEmpty());
	}

	@Test
	void shouldThrowExceptionWhenRemovingAssignedRocket() {
		// Given: a rocket assigned to a mission
		String rocketName = "Falcon 9";
		String missionName = "Mars";
		rocketRepository.addRocket(new Rocket(rocketName));
		missionRepository.addMission(new Mission(missionName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// When & Then: attempting to remove the rocket, an exception should be thrown
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.removeRocket(rocketName));
		assertEquals("Cannot remove rocket '" + rocketName + "' as it is currently assigned to mission '" + missionName
				+ "'.", thrown.getMessage());
	}

	@Test
	void shouldRemoveMissionSuccessfully() {
		// Given: a mission with no assigned rockets
		String missionName = "New Mission";
		missionRepository.addMission(new Mission(missionName));

		// When: attempting to remove the mission
		spaceXRepository.removeMission(missionName);

		// Then: the mission should no longer be in the repository
		Optional<Mission> removedMission = missionRepository.findByName(missionName);
		assertTrue(removedMission.isEmpty());
	}

	@Test
	void shouldThrowExceptionWhenRemovingMissionWithAssignedRockets() {
		// Given: a mission with an assigned rocket
		String missionName = "Jupiter";
		String rocketName = "Starship";
		missionRepository.addMission(new Mission(missionName));
		rocketRepository.addRocket(new Rocket(rocketName));
		spaceXRepository.assignRocketToMission(rocketName, missionName);

		// When & Then: attempting to remove the mission, an exception should be thrown
		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> spaceXRepository.removeMission(missionName));
		assertEquals("Cannot remove mission '" + missionName + "' as it has assigned rockets.", thrown.getMessage());
	}
}
