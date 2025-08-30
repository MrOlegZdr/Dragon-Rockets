package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.home.project.dragonrockets.exception.RocketAlreadyAssignedException;
import com.home.project.dragonrockets.exception.RocketNotFoundException;
import com.home.project.dragonrockets.model.Mission;
import com.home.project.dragonrockets.model.MissionStatus;
import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;
import com.home.project.dragonrockets.repository.MissionRepository;
import com.home.project.dragonrockets.repository.RocketRepository;
import com.home.project.dragonrockets.service.RocketService;

class RocketServiceTest {

	@Mock
	private RocketRepository rocketRepository;

	@Mock
	private MissionRepository missionRepository;

	@InjectMocks
	private RocketService rocketService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void shouldAddRocket() {
		Rocket mockRocket = mock(Rocket.class);
		rocketService.addRocket(mockRocket);
		verify(rocketRepository, times(1)).addRocket(mockRocket);
	}

	@Test
	void shouldFindRocket() {
		String rocketName = "Falcon 9";
		rocketService.findRocketByName(rocketName);
		verify(rocketRepository, times(1)).findByName(rocketName);
	}

	@Test
	void shouldChangeRocketStatusSuccessfully() {
		// Given
		String rocketName = "Falcon 9";
		Rocket mockRocket = new Rocket(rocketName);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));

		// When
		rocketService.changeRocketStatus(rocketName, RocketStatus.IN_SPACE);

		// Then
		assertEquals(RocketStatus.IN_SPACE, mockRocket.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenChangingStatusOfNonexistentRocket() {
		// Given
		String rocketName = "Nonexistent Rocket";
		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(RocketNotFoundException.class,
				() -> rocketService.changeRocketStatus(rocketName, RocketStatus.IN_SPACE));
	}

	@Test
	void shouldChangeMissionStatusToPendingWhenRocketIsInRepair() {
		// Given
		String rocketName = "Dragon 1";
		String missionName = "ISS Mission";
		Rocket mockRocket = new Rocket(rocketName);
		mockRocket.setAssignedMissionName(missionName);
		Mission mockMission = new Mission(missionName);
		mockMission.getAssignedRockets().add(mockRocket);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));
		when(missionRepository.findByName(missionName)).thenReturn(Optional.of(mockMission));

		// When
		rocketService.changeRocketStatus(rocketName, RocketStatus.IN_REPAIR);

		// Then
		verify(missionRepository, times(1)).findByName(missionName);
		assertEquals(MissionStatus.PENDING, mockMission.getStatus());
	}

	@Test
	void shouldRemoveRocketSuccessfully() {
		// Given
		String rocketName = "Starship";
		Rocket mockRocket = mock(Rocket.class);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));
		when(mockRocket.getAssignedMissionName()).thenReturn(null);

		// When
		rocketService.removeRocket(rocketName);

		// Then
		verify(rocketRepository, times(1)).remove(rocketName);
	}

	@Test
	void shouldThrowExceptionWhenRemovingAssignedRocket() {
		String rocketName = "Starship";
		Rocket mockRocket = mock(Rocket.class);

		when(rocketRepository.findByName(rocketName)).thenReturn(Optional.of(mockRocket));
		when(mockRocket.getAssignedMissionName()).thenReturn("Assigned Mission");

		// When & Then
		verify(rocketRepository, never()).remove(rocketName);
		assertThrows(RocketAlreadyAssignedException.class, () -> rocketService.removeRocket(rocketName));
	}
}
