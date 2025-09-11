# Changelog

All notable changes to this project will be documented in this file.

## [2.0.0] - 2025-09-11

### Added
* Introduced new Facade Constructor **SpaceXManager()**. Now facade uses a simplified constructor with no external dependencies.
* New method **getRocketInfo** to display information about rocket by its name.
* Added tests for the new facade's methods and the services to ensure comprehensive coverage and stability.

### Changed
* Refactored **SpaceXManager** facade to handle the initialization of **RocketService** and **MissionService** internally. This removes the responsibility of dependency injection from the user, simplifying the library's public API.
* Updated all existing tests to reflect the new **SpaceXManager** initialization.

### Removed
* The original **SpaceXRepository(RocketService rocketService, MissionService missionService)** constructor.

## [1.1.1] - 2025-09-05

### Changed
* The entire internal structure has been transferred to the **internal.*** package for future facade changes.

## [1.1.0] - 2025-08-30

### Added
* Introduced **SpaceXManager** class, implementing the Facade pattern to simplify working with the library.
* Implemented **RocketService** to handle all rocket-related business logic.
* Implemented **MissionService** to handle all mission-related business logic.
* Implemented custom exceptions.
* Added tests for **RocketService** and **MissionService** to ensure reliability and maintainability.

### Changed
* Renamed **SpaceXRepository** to **SpaceXManager** to better reflect its role as a facade.
* Refactored core logic from the original **SpaceXRepository** into two separate service classes.
* Refactored enum classes. Added corresponding string value to each enum constant.
* Updated dependencies to reflect the new service-oriented architecture.

### Removed
* Deprecated the original **SpaceXRepository** class. Its functionality has been migrated to the new service classes.
* DragonrocketsApplication.class. The class is not needed for the library's functionality and its absence will not affect the build or performance.

## [1.0.0] - 2025-08-29
### Added
* Initial project structure and **SpaceXRepository** class with basic functionality.
* Initial unit tests for **SpaceXRepository**.