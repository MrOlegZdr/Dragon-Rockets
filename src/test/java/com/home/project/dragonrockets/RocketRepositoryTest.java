package com.home.project.dragonrockets;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.home.project.dragonrockets.model.Rocket;
import com.home.project.dragonrockets.model.RocketStatus;
import com.home.project.dragonrockets.repository.RocketRepository;

class RocketRepositoryTest {

	private RocketRepository rocketRepository;

	@BeforeEach
	void setUp() {
		rocketRepository = new RocketRepository();
	}

	@Test
	void shouldAddRocketSuccessfully() {
		String rocketName = "Falcon 9";
		rocketRepository.addRocket(new Rocket(rocketName));

		Optional<Rocket> addedRocketOptional = rocketRepository.findByName(rocketName);
		assertTrue(addedRocketOptional.isPresent());

		Rocket addedRocket = addedRocketOptional.get();
		assertEquals(rocketName, addedRocket.getName());
		assertEquals(RocketStatus.ON_GROUND, addedRocket.getStatus());
	}

	@Test
	void shouldThrowExceptionWhenAddingDuplicateRocket() {
		String rocketName = "Falcon 9";
		rocketRepository.addRocket(new Rocket(rocketName));

		IllegalArgumentException thrown = assertThrows(
				IllegalArgumentException.class,
				() -> rocketRepository.addRocket(new Rocket(rocketName)));

		assertEquals("Rocket with name 'Falcon 9' already exists.", thrown.getMessage());
	}

}
