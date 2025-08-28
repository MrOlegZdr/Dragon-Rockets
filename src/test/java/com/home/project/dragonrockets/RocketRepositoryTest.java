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

	@Test
	void shouldFindRocketByName() {
		String rocketName = "Falcon 9";
		rocketRepository.addRocket(new Rocket(rocketName));

		Optional<Rocket> foundRocket = rocketRepository.findByName(rocketName);
		assertTrue(foundRocket.isPresent());
		assertEquals(rocketName, foundRocket.get().getName());
	}

	@Test
	void shouldNotFindNonexistentRocket() {
		String rocketName = "Falcon 9";
		rocketRepository.addRocket(new Rocket(rocketName));
		String searchName = "Starship";

		Optional<Rocket> foundRocket = rocketRepository.findByName(searchName);
		assertTrue(foundRocket.isEmpty());
	}

	@Test
	void shouldRemoveRocketSuccessfully() {
		// Given: a rocket exists in the repository
		String rocketName = "Falcon 9";
		rocketRepository.addRocket(new Rocket(rocketName));
		assertTrue(rocketRepository.findByName(rocketName).isPresent());

		// When: removing the rocket
		rocketRepository.remove(rocketName);

		// Then: the rocket should no longer be found
		assertTrue(rocketRepository.findByName(rocketName).isEmpty());
	}

	@Test
	void shouldNotThrowExceptionWhenRemovingNonexistentRocket() {
		// Given: a non-existent rocket name
		String nonExistentName = "Nonexistent Rocket";
		String existentName = "Falcon 9";
		rocketRepository.addRocket(new Rocket(existentName));
		assertTrue(rocketRepository.findByName(existentName).isPresent());
		assertFalse(rocketRepository.findByName(nonExistentName).isPresent());

		// When: attempting to remove it
		// Then: no exception should be thrown
		assertDoesNotThrow(() -> rocketRepository.remove(nonExistentName));
	}
}
