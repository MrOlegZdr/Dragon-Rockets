package com.home.project.dragonrockets.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.home.project.dragonrockets.model.Rocket;

public class RocketRepository {

	private final Map<String, Rocket> rockets = new HashMap<>();

	public void addRocket(Rocket rocket) {
		if (rockets.containsKey(rocket.getName())) {
			throw new IllegalArgumentException("Rocket with name '" + rocket.getName() + "' already exists.");
		}
		rockets.put(rocket.getName(), rocket);
	}

	public Optional<Rocket> findByName(String name) {
		return Optional.ofNullable(rockets.get(name));
	}

	public void remove(String name) {
		rockets.remove(name);
	}
}
