package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.home.project.dragonrockets.exception.InvalidStatusTransitionException;
import com.home.project.dragonrockets.exception.RocketNotFoundException;
import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;
import com.home.project.dragonrockets.repository.MissionRepository;
import com.home.project.dragonrockets.repository.RocketRepository;
import com.home.project.dragonrockets.service.MissionService;

class MissionServiceTest {

	@Mock
	private MissionRepository missionRepository;

	@Mock
	private RocketRepository rocketRepository;

	@InjectMocks
	private MissionService missionService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void shouldAddMission() {
		Mission mockMission = mock(Mission.class);
		missionService.addMission(mockMission);
		verify(missionRepository, times(1)).addMission(mockMission);
	}

	@Test
	void shouldFindMission() {
		String missionName = "Mars";
		missionService.findMissionByName(missionName);
		verify(missionRepository, times(1)).findByName(missionName);
	}

	@Test
	void shouldAssignRocketToMissionSuccessfully() {
		// Given
		String rocketName = "Falcon 9";
		String missionName = "Mars";
		Rocket mockRocket = new Rocket(rocketName);
		Mission mockMission = new Mission(missionName);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));
		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(mockMission));

		// When
		missionService.assignRocketToMission(rocketName, missionName);

		// Then
		assertEquals(missionName, mockRocket.getAssignedMissionName());
		assertTrue(mockMission.getAssignedRockets().contains(mockRocket));
		verify(rocketRepository, times(1)).findByName(rocketName);
		verify(missionRepository, times(1)).findByName(missionName);
	}

	@Test
	void shouldThrowExceptionWhenAssigningRocketThatDoesNotExist() {
		// Given
		String rocketName = "Nonexistent Rocket";
		String missionName = "Test Mission";
		Mission mockMission = new Mission(missionName);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.empty());
		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(mockMission));

		// When & Then
		assertThrows(RocketNotFoundException.class,
				() -> missionService.assignRocketToMission(rocketName, missionName));
	}

	@Test
	void shouldThrowExceptionWhenAssigningToEndedMission() {
		// Given
		String rocketName = "Falcon 9";
		String missionName = "Ended Mission";
		Rocket mockRocket = new Rocket(rocketName);
		Mission endedMission = new Mission(missionName);
		endedMission.setStatus(MissionStatus.ENDED);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));
		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(endedMission));

		// When & Then
		assertThrows(InvalidStatusTransitionException.class,
				() -> missionService.assignRocketToMission(rocketName, missionName));
	}

	@Test
	void shouldUnassignRocketSuccessfully() {
		// Given
		String rocketName = "Falcon 9";
		String missionName = "Test Mission";

		Rocket mockRocket = mock(Rocket.class);
		Mission mockMission = mock(Mission.class);
		@SuppressWarnings("unchecked")
		List<Rocket> mockRocketsInMission = mock(List.class);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));
		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(mockMission));
		when(mockRocket.getAssignedMissionName()).thenReturn(missionName);
		when(mockMission.getAssignedRockets()).thenReturn(mockRocketsInMission);

		// When
		missionService.unassignRocketFromMission(rocketName);

		// Then
		verify(mockRocket).setAssignedMissionName(null);
		verify(mockRocket).setStatus(RocketStatus.ON_GROUND);
		verify(mockRocketsInMission, times(1)).remove(mockRocket);
	}

	@Test
	void shouldThrowExceptionWhenChangingMissionStatusToEndedWithAssignedRockets() {
		// Given
		String missionName = "Mars Mission";
		Mission mockMission = new Mission(missionName);
		mockMission.getAssignedRockets().add(new Rocket("Rocket 1"));

		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(mockMission));

		// When & Then
		assertThrows(InvalidStatusTransitionException.class,
				() -> missionService.changeMissionStatus(missionName, MissionStatus.ENDED));
	}

	@Test
	void shouldRemoveMissionSuccessfully() {
		// Given
		String missionName = "Empty Mission";
		Mission mockMission = mock(Mission.class);

		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(mockMission));
		when(mockMission.getAssignedRockets()).thenReturn(Collections.emptyList());

		// When
		missionService.removeMission(missionName);

		// Then
		verify(missionRepository, times(1)).remove(missionName);
	}

	@Test
	void shouldGetCorrectMissionSummary() {
		Rocket falcon1 = new Rocket("Falcon 1");
		Rocket falcon2 = new Rocket("Falcon 2");
		Rocket falcon3 = new Rocket("Falcon 3");
		Mission firstMission = new Mission("First Mission");
		Mission secondMission = new Mission("Second Mission");
		firstMission.getAssignedRockets().add(falcon1);
		firstMission.getAssignedRockets().add(falcon2);
		secondMission.getAssignedRockets().add(falcon3);

		when(missionRepository.findAll()).thenReturn(Arrays.asList(firstMission, secondMission));

		List<String> summary = missionService.getMissionSummary();

		assertNotNull(summary);
		assertEquals(5, summary.size()); // 2 Headers + 3 rockets
		assertEquals("First Mission - Scheduled - Dragons: 2", summary.get(0));
		assertTrue(summary.contains("\t- Falcon 1 - On Ground"));
		assertTrue(summary.contains("\t- Falcon 2 - On Ground"));
		assertEquals("Second Mission - Scheduled - Dragons: 1", summary.get(3));
		assertTrue(summary.contains("\t- Falcon 3 - On Ground"));
	}
}
