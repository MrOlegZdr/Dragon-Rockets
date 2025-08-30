# SpaceX Dragon Rockets Repository
A simple Java library for managing the status of SpaceX Dragon rockets and their missions.

## Introduction
This project is a straightforward, in-memory repository implementation developed as a simple library.  
It allows users to track the status of SpaceX rockets and missions, assign rockets to missions, and generate a summary report.  
The solution is built with a focus on clean code, object-oriented design, and adherence to SOLID principles, using a Test-Driven Development (TDD) approach.

## Architecture and design
**1. Separation of Concerns and Service Layer.** The library is designed using the **Repository** pattern to separate data access logic from business logic:  
* **RocketRepository:** manages a collection of **Rocket** objects.
* **MissionRepository:** manages a collection of **Mission** objects.
* **RocketService:** encapsulates all business logic related to **Rocket** objects.
* **MissionService:** encapsulates all business logic related to **Mission** objects.
* **SpaceXManager:** this is a **facade** class that provides the main public interface for the library. It delegates all requests to the appropriate service.  
  
**2. In-Memory Store:**  
* All data is stored in memory using standard Java collections (**HashMap**).  
  
**3. TDD Approach:**  
* The entire code was developed following Test-Driven Development.  
Each feature began with a failing test, followed by the minimal code required to make it pass.  
  
**4. Custom Exception Handling.** The library uses custom exceptions to provide clearer error messages and better control over error handling.  
* **RocketNotFoundException**
* **MissionNotFoundException**
* **RocketAlreadyAssignedException**
* **InvalidStatusTransitionException**
* **MissionHasAssignedRocketsException**  
  
## Key Features
**1. Object management:**  
* Rocket Management: Adding new rockets with name uniqueness check. Removing rockets from the repository based on specific conditions.  
* Mission Management: Adding new missions with name uniqueness check. Removing missions from the repository based on specific conditions.  
  
**2. Assignment Logic:**  
* Assigning one or more rockets to a mission.  
* A single rocket can be assigned to only one mission.  
* Un-assign rockets from a mission.  
  
**3. Status management:**  
* Manually change the status of a rocket ("On ground", "In space", "In repair").  
* Manually change the status of a mission ("Scheduled", "Pending", "In progress", "Ended") with strict checks.
* The enum status values includes a user-friendly display name.  
  
**4. Reporting:**  
* Get a summary of all missions, sorted by the number of assigned rockets (descending) and then by mission name (descending alphabetical order).  

## Assumptions and Specifications
**1. Unique Names:**  
* Rocket and mission names are treated as unique identifiers. Attempting to add an object with a name that already exists will throw an **IllegalArgumentException**.  
  
**2. Object Aggregation:**  
* The rocket and the mission are linked in a two-way manner.  
The rocket "knows" what mission it belongs to, and the mission contains a list of its rockets.  
  
**3. Description of Rockets statuses:**  
* **"On ground"** – initial status, where the rocket is not assigned to any mission.  
* **"In space"** – the rocket was assigned to the mission.  
* **"In repair"** – the rocket is due to repair, it implies **"Pending"** status of the mission.  
  
**4. Description of Missions statuses:**  
* **"Scheduled"** – initial status, where no rockets are assigned.  
* **"Pending"** – at least one rocket is assigned and one or more assigned rockets are in repair.  
* **"In Progress"** – at least one rocket is assigned and none of them is in repair.  
* **"Ended"** – the final stage of the mission, at this point rockets should not be assigned anymore to a mission.  
  
**5. Status Change Rules (Manual Control):**  
* A rocket's status does not automatically change from **"On Ground"** to **"In Space"** upon assignment to a mission.  
* A mission's status does not automatically change to **"In Progress"** when rockets are assigned.  
* A mission's status does not automatically change back to **"Scheduled"** when all assigned rockets are back **"On Ground"**.  
* A mission's status is set to **"Pending"** automatically if at least one assigned rocket has entered the **"In Repair"** status.  
  
**6. Summary Report Sorting:**  
* The summary report is generated based on the number of rockets assigned to the mission.  
* Missions with the same number of assigned rockets are sorted in descending alphabetical order (Z-A).

## Using the library

* The library can be integrated into any Java project by including the compiled **.jar** file.  
To use the library, you need to instantiate the repositories and services, and then create a **SpaceXManager** instance, which serves as the public facade.  
  

* Initialize repositories:  

```
RocketRepository rocketRepository = new RocketRepository();
MissionRepository missionRepository = new MissionRepository();
```
* Initialize services with their dependencies:  

```
RocketService rocketService = new RocketService(rocketRepository, missionRepository);
MissionService missionService = new MissionService(missionRepository, rocketRepository);
```
* Initialize the facade:  

```
SpaceXManager spaceXManager = new SpaceXManager(rocketService, missionService);
```
* Adding new rockets and missions:

```
spaceXManager.addRocket(new Rocket("Falcon 9"));
spaceXManager.addRocket(new Rocket("Starship"));
spaceXManager.addMission(new Mission("Mars Landing"));
spaceXManager.addMission(new Mission("ISS Resupply"));
```
* Assigning a rocket to a mission:

```
spaceXManager.assignRocketToMission("Falcon 9", "Mars Landing");
spaceXManager.assignRocketToMission("Starship", "ISS Resupply");
```
* Manually changing statuses:

```
spaceXManager.changeRocketStatus("Falcon 9", RocketStatus.IN_SPACE);
spaceXManager.changeMissionStatus("Mars Landing", MissionStatus.IN_PROGRESS);
```
* Generate and print summary report:

```
List<String> summary = spaceXManager.getMissionSummary();
summary.forEach(System.out::println);
```

## Used technology
* Java 17
* Maven
* JUnit 5
* Mockito (for testing)

## Learn More
* list of changes and updates: [Changelog](CHANGELOG.md)