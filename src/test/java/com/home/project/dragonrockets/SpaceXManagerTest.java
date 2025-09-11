package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.home.project.dragonrockets.internal.exception.InvalidStatusTransitionException;
import com.home.project.dragonrockets.internal.exception.MissionHasAssignedRocketsException;
import com.home.project.dragonrockets.internal.exception.MissionNotFoundException;
import com.home.project.dragonrockets.internal.exception.RocketAlreadyAssignedException;
import com.home.project.dragonrockets.internal.exception.RocketNotFoundException;
import com.home.project.dragonrockets.internal.model.Mission;
import com.home.project.dragonrockets.internal.model.MissionStatus;
import com.home.project.dragonrockets.internal.model.Rocket;
import com.home.project.dragonrockets.internal.model.RocketStatus;

class SpaceXManagerTest {

	private SpaceXManager spaceXManager;

	@BeforeEach
	void setUp() {
		spaceXManager = new SpaceXManager();
	}

	@Test
	void shouldAssignRocketToMissionSuccessfully() {
		// Given: a new rocket and a new mission
		String rocketName = "Falcon 9";
		String missionName = "Mars";
		Rocket rocket = new Rocket(rocketName);
		Mission mission = new Mission(missionName);

		spaceXManager.addRocket(rocket);
		spaceXManager.addMission(mission);

		// When: assigning the rocket to the mission
		spaceXManager.assignRocketToMission(rocketName, missionName);

		Rocket assignedRocket = mission.getAssignedRockets().get(0);
		assertEquals(RocketStatus.ON_GROUND, assignedRocket.getStatus());
		assertEquals(missionName, assignedRocket.getAssignedMissionName());
		assertEquals(missionName, rocket.getAssignedMissionName());

		assertEquals(1, mission.getAssignedRockets().size());
		assertEquals(RocketStatus.ON_GROUND, mission.getAssignedRockets().get(0).getStatus());
		assertEquals(MissionStatus.SCHEDULED, mission.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenAssigningRocketAlreadyAssigned() {
		// Given: a rocket already assigned to a mission
		String rocketName = "Falcon 9";
		String mission1Name = "Mission Alpha";
		String mission2Name = "Mission Beta";
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.addMission(new Mission(mission1Name));
		spaceXManager.addMission(new Mission(mission2Name));
		spaceXManager.assignRocketToMission(rocketName, mission1Name);

		// When & Then: attempting to re-assign the rocket, an exception should be
		// thrown
		RocketAlreadyAssignedException thrown = assertThrows(
				RocketAlreadyAssignedException.class,
				() -> spaceXManager.assignRocketToMission(rocketName, mission2Name));
		assertEquals("Rocket 'Falcon 9' is already assigned to mission 'Mission Alpha'.", thrown.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenAssigningToEndedMission() {
		// Given: a mission with 'ENDED' status
		String rocketName = "Falcon Heavy";
		String missionName = "Old Mission";
		spaceXManager.addRocket(new Rocket(rocketName));
		Mission endedMission = new Mission(missionName);
		endedMission.setStatus(MissionStatus.ENDED);
		spaceXManager.addMission(endedMission);

		// When & Then: attempting to assign a rocket to the ended mission, an exception
		// should be thrown
		InvalidStatusTransitionException thrown = assertThrows(
				InvalidStatusTransitionException.class,
				() -> spaceXManager.assignRocketToMission(rocketName, missionName));
		assertEquals("Cannot assign rockets to a mission with status 'Ended'.", thrown.getMessage());
	}

	@Test
	void shouldUnassignRocketFromMissionSuccessfully() {
		// Given: a rocket assigned to a mission
		String rocketName = "Dragon 1";
		String missionName = "Mars";
		Rocket rocket = new Rocket(rocketName);
		Mission mission = new Mission(missionName);

		spaceXManager.addRocket(rocket);
		spaceXManager.addMission(mission);
		spaceXManager.assignRocketToMission(rocketName, missionName);

		// Checking rocket assigning
		assertEquals(missionName, rocket.getAssignedMissionName());
		assertEquals(1, mission.getAssignedRockets().size());
		assertEquals("Mars", rocket.getAssignedMissionName());

		// When: un-assigning the rocket
		spaceXManager.unassignRocketFromMission(rocketName);

		// Then: the rocket should be unassigned
		assertNotNull(mission);
		assertEquals(0, mission.getAssignedRockets().size());
		assertEquals(null, rocket.getAssignedMissionName());

	}

	@Test
	void shouldThrowExceptionWhenUnassigningRocketNotAssignedToAnyMission() {
		// Given: a rocket not assigned to any mission
		String rocketName = "Falcon Heavy";
		spaceXManager.addRocket(new Rocket(rocketName));

		// When & Then: attempting to un-assign it, an exception should be thrown
		RocketAlreadyAssignedException thrown = assertThrows(
				RocketAlreadyAssignedException.class,
				() -> spaceXManager.unassignRocketFromMission(rocketName));
		assertEquals("Rocket 'Falcon Heavy' is not assigned to any mission.", thrown.getMessage());
	}

	@Test
	void shouldChangeRocketStatus() {
		// Given: a rocket with 'ON_GROUND' status
		String rocketName = "Starship";
		Rocket rocket = new Rocket(rocketName);
		spaceXManager.addRocket(rocket);

		assertEquals(RocketStatus.ON_GROUND, rocket.getStatus());

		// When: changing its status to 'IN_REPAIR'
		spaceXManager.changeRocketStatus(rocketName, RocketStatus.IN_REPAIR);

		// Then: the status should be updated
		assertEquals(RocketStatus.IN_REPAIR, rocket.getStatus());
	}

	@Test
	void shouldChangeMissionStatusToPendingWhenRocketIsInRepair() {
		// Given: a mission with two rockets, both 'IN_SPACE'
		String missionName = "Luna 2";
		String rocket1Name = "Dragon 1";
		String rocket2Name = "Dragon 2";
		Rocket rocket1 = new Rocket(rocket1Name);
		Rocket rocket2 = new Rocket(rocket2Name);
		Mission mission = new Mission(missionName);

		spaceXManager.addMission(mission);
		spaceXManager.addRocket(rocket1);
		spaceXManager.addRocket(rocket2);
		spaceXManager.assignRocketToMission(rocket1Name, missionName);
		spaceXManager.assignRocketToMission(rocket2Name, missionName);

		// When: one of the rockets goes to 'IN_REPAIR' status
		spaceXManager.changeRocketStatus(rocket1Name, RocketStatus.IN_REPAIR);

		// Then: the mission status should automatically change to 'PENDING'
		assertEquals(MissionStatus.PENDING, mission.getStatus());
	}

	@Test
	void shouldNotChangeMissionStatusFromPendingIfOtherRocketsAreInRepair() {
		// Given: a mission with one rocket 'IN_REPAIR' and one 'IN_SPACE'
		String missionName = "Luna 3";
		String rocket1Name = "Dragon 3";
		String rocket2Name = "Dragon 4";
		Rocket rocket1 = new Rocket(rocket1Name);
		Rocket rocket2 = new Rocket(rocket2Name);
		Mission mission = new Mission(missionName);

		spaceXManager.addMission(mission);
		spaceXManager.addRocket(rocket1);
		spaceXManager.addRocket(rocket2);
		spaceXManager.assignRocketToMission(rocket1Name, missionName);
		spaceXManager.assignRocketToMission(rocket2Name, missionName);
		spaceXManager.changeRocketStatus(rocket1Name, RocketStatus.IN_REPAIR); // Mission status is now Pending
		assertEquals(MissionStatus.PENDING, mission.getStatus());

		// When: the other rocket changes status to 'IN_SPACE'
		spaceXManager.changeRocketStatus(rocket2Name, RocketStatus.IN_SPACE);

		// Then: the mission status should remain 'PENDING' because the first rocket is
		// still in repair
		assertEquals(MissionStatus.PENDING, mission.getStatus());
	}

	@Test
	void shouldChangeMissionStatusToInProgress() {
		// Given: a mission with assigned rocket
		String missionName = "Mars";
		String rocketName = "Falcon 9";
		Rocket rocket = new Rocket(rocketName);
		Mission mission = new Mission(missionName);

		spaceXManager.addMission(mission);
		spaceXManager.addRocket(rocket);

		// Check initial status SCHEDULED
		assertEquals(MissionStatus.SCHEDULED, mission.getStatus());

		spaceXManager.assignRocketToMission(rocketName, missionName);

		// When: changing its status to IN_PROGRESS
		spaceXManager.changeMissionStatus(missionName, MissionStatus.IN_PROGRESS);

		// Then: the status should be updated
		assertEquals(MissionStatus.IN_PROGRESS, mission.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenChangingToEndedStatusWithAssignedRockets() {
		// Given: a mission with assigned rocket
		String missionName = "Mars";
		String rocketName = "Starship";
		spaceXManager.addMission(new Mission(missionName));
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.assignRocketToMission(rocketName, missionName);

		// When & Then: attempting to change status to Ended
		InvalidStatusTransitionException thrown = assertThrows(
				InvalidStatusTransitionException.class,
				() -> spaceXManager.changeMissionStatus(missionName, MissionStatus.ENDED));
		assertEquals(
				"Cannot change mission status to 'Ended' because rockets are still assigned. Please unassign all rockets first.",
				thrown.getMessage());
	}

	@Test
	void shouldChangeMissionStatusToEndedWhenNoRocketsAreAssigned() {
		// Given: a mission with no rockets assigned
		String missionName = "Interstellar";
		Mission mission = new Mission(missionName);
		spaceXManager.addMission(mission);

		assertEquals(MissionStatus.SCHEDULED, mission.getStatus());

		// When: changing its status to ENDED
		spaceXManager.changeMissionStatus(missionName, MissionStatus.ENDED);

		// Then: the status should be updated
		assertEquals(MissionStatus.ENDED, mission.getStatus());
	}

	@Test
	void shouldAllowMissionToEndAfterUnassigningAllRockets() {
		// Given: a mission with one assigned rocket
		String missionName = "Moon Landing";
		String rocketName = "Starship 1";
		Rocket rocket = new Rocket(rocketName);
		Mission mission = new Mission(missionName);

		spaceXManager.addRocket(rocket);
		spaceXManager.addMission(mission);
		spaceXManager.assignRocketToMission(rocketName, missionName);
		assertEquals(1, mission.getAssignedRockets().size());

		// When: un-assigning the last rocket and then changing mission status to Ended
		spaceXManager.unassignRocketFromMission(rocketName);
		spaceXManager.changeMissionStatus(missionName, MissionStatus.ENDED);

		// Then: the mission status should be updated successfully
		assertNotNull(mission);
		assertEquals(MissionStatus.ENDED, mission.getStatus());
		assertEquals(0, mission.getAssignedRockets().size());
	}

	@Test
	void shouldThrowExceptionWhenChangingToScheduledStatus() {
		String missionName = "Test";
		String rocketName = "Starship";
		spaceXManager.addMission(new Mission(missionName));
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.assignRocketToMission(rocketName, missionName);

		InvalidStatusTransitionException thrown = assertThrows(
				InvalidStatusTransitionException.class,
				() -> spaceXManager.changeMissionStatus(missionName, MissionStatus.SCHEDULED));
		assertEquals("Status 'Scheduled' can only be set at mission creation.", thrown.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenChangingToPendingWithoutRocketsInRepair() {
		String missionName = "Jupiter";
		String rocketName = "Starship 1";
		spaceXManager.addMission(new Mission(missionName));
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.assignRocketToMission(rocketName, missionName);

		InvalidStatusTransitionException thrown = assertThrows(
				InvalidStatusTransitionException.class,
				() -> spaceXManager.changeMissionStatus(missionName, MissionStatus.PENDING));
		assertEquals("Cannot set status 'Pending' unless at least one assigned rocket is in 'In Repair' status.",
				thrown.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenChangingToInProgressWithRocketInRepair() {
		String missionName = "Saturn";
		String rocketName = "Falcon 9";
		spaceXManager.addMission(new Mission(missionName));
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.assignRocketToMission(rocketName, missionName);
		spaceXManager.changeRocketStatus(rocketName, RocketStatus.IN_REPAIR);

		InvalidStatusTransitionException thrown = assertThrows(
				InvalidStatusTransitionException.class,
				() -> spaceXManager.changeMissionStatus(missionName, MissionStatus.IN_PROGRESS));
		assertEquals("Cannot set status 'In Progress' because at least one assigned rocket is in 'In Repair' status.",
				thrown.getMessage());
	}

	@Test
	void shouldReturnCorrectMissionSummary() {
		// Given: data from the example in the task
		// Missions
		spaceXManager.addMission(new Mission("Mars"));
		spaceXManager.addMission(new Mission("Luna1"));
		spaceXManager.addMission(new Mission("Double Landing"));
		spaceXManager.addMission(new Mission("Transit"));
		spaceXManager.addMission(new Mission("Luna2"));
		spaceXManager.addMission(new Mission("Vertical Landing"));

		// Rockets
		spaceXManager.addRocket(new Rocket("Dragon 1"));
		spaceXManager.addRocket(new Rocket("Dragon 2"));
		spaceXManager.addRocket(new Rocket("Red Dragon"));
		spaceXManager.addRocket(new Rocket("Dragon XL"));
		spaceXManager.addRocket(new Rocket("Falcon Heavy"));

		// Assignments and status changes
		spaceXManager.assignRocketToMission("Dragon 1", "Luna1");
		spaceXManager.assignRocketToMission("Dragon 2", "Luna1");
		spaceXManager.assignRocketToMission("Red Dragon", "Transit");
		spaceXManager.assignRocketToMission("Dragon XL", "Transit");
		spaceXManager.assignRocketToMission("Falcon Heavy", "Transit");

		spaceXManager.changeRocketStatus("Dragon XL", RocketStatus.IN_SPACE);
		spaceXManager.changeRocketStatus("Falcon Heavy", RocketStatus.IN_SPACE);
		spaceXManager.changeMissionStatus("Transit", MissionStatus.IN_PROGRESS);

		spaceXManager.changeMissionStatus("Double Landing", MissionStatus.ENDED);
		spaceXManager.changeMissionStatus("Vertical Landing", MissionStatus.ENDED);

		// When: generating the summary
		List<String> summary = spaceXManager.getMissionSummary();

		// Then: verify the summary matches the expected output
		List<String> expectedSummary = List.of(
				"Transit - In Progress - Dragons: 3",
				"\t- Red Dragon - On Ground",
				"\t- Dragon XL - In Space",
				"\t- Falcon Heavy - In Space",
				"Luna1 - Scheduled - Dragons: 2",
				"\t- Dragon 1 - On Ground",
				"\t- Dragon 2 - On Ground",
				"Vertical Landing - Ended - Dragons: 0",
				"Mars - Scheduled - Dragons: 0",
				"Luna2 - Scheduled - Dragons: 0",
				"Double Landing - Ended - Dragons: 0");

		assertEquals(expectedSummary.size(), summary.size());
		for (int i = 0; i < expectedSummary.size(); i++) {
			assertEquals(expectedSummary.get(i), summary.get(i));
		}
	}

	@Test
	void shouldReturnCorrectRocketInfo() {
		String rocketName = "Falcon 1";
		String missionName = "Mars";
		Rocket rocket = new Rocket(rocketName);
		Mission mission = new Mission(missionName);
		spaceXManager.addRocket(rocket);
		spaceXManager.addMission(mission);
		spaceXManager.assignRocketToMission(rocketName, missionName);
		spaceXManager.changeRocketStatus(rocketName, RocketStatus.IN_SPACE);
		spaceXManager.changeMissionStatus(missionName, MissionStatus.IN_PROGRESS);
		assertEquals("Falcon 1 - In Space - Mission: Mars", spaceXManager.getRocketInfo(rocketName));
	}

	@Test
	void shouldRemoveRocketSuccessfully() {
		// Given: a rocket not assigned to any mission
		String rocketName = "Falcon 1";
		Rocket rocket = new Rocket(rocketName);
		spaceXManager.addRocket(rocket);
		assertEquals("Falcon 1 - On Ground - Mission: NOT ASSIGNED", spaceXManager.getRocketInfo(rocketName));

		// When: attempting to remove the rocket
		spaceXManager.removeRocket(rocketName);

		// Then: the rocket should no longer be in the repository
		assertThrows(RocketNotFoundException.class, () -> spaceXManager.getRocketInfo(rocketName));
	}

	@Test
	void shouldThrowExceptionWhenRemovingAssignedRocket() {
		// Given: a rocket assigned to a mission
		String rocketName = "Falcon 9";
		String missionName = "Mars";
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.addMission(new Mission(missionName));
		spaceXManager.assignRocketToMission(rocketName, missionName);

		// When & Then: attempting to remove the rocket, an exception should be thrown
		RocketAlreadyAssignedException thrown = assertThrows(
				RocketAlreadyAssignedException.class,
				() -> spaceXManager.removeRocket(rocketName));
		assertEquals("Cannot remove rocket '" + rocketName + "' as it is currently assigned to mission '" + missionName
				+ "'.", thrown.getMessage());
	}

	@Test
	void shouldRemoveMissionSuccessfully() {
		// Given: a mission with no assigned rockets
		String missionName = "New Mission";
		spaceXManager.addMission(new Mission(missionName));
		assertEquals(Arrays.asList("New Mission - Scheduled - Dragons: 0"), spaceXManager.getMissionSummary());

		// When: attempting to remove the mission
		spaceXManager.removeMission(missionName);

		// Then: the mission should no longer be in the repository
		assertThrows(MissionNotFoundException.class, () -> spaceXManager.removeMission(missionName));
	}

	@Test
	void shouldThrowExceptionWhenRemovingMissionWithAssignedRockets() {
		// Given: a mission with an assigned rocket
		String missionName = "Jupiter";
		String rocketName = "Starship";
		spaceXManager.addMission(new Mission(missionName));
		spaceXManager.addRocket(new Rocket(rocketName));
		spaceXManager.assignRocketToMission(rocketName, missionName);

		// When & Then: attempting to remove the mission, an exception should be thrown
		MissionHasAssignedRocketsException thrown = assertThrows(
				MissionHasAssignedRocketsException.class,
				() -> spaceXManager.removeMission(missionName));
		assertEquals("Cannot remove mission '" + missionName + "' as it has assigned rockets.", thrown.getMessage());
	}
}
