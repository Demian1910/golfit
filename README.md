# GOLFIT

GOLFIT is a multiplayer video game centered around the experience of minigolf. Developed by a team of four students for the Programming project at the University of Basel, it aims to provide a fun golf game, with interactive environment.

## Project Overview
The game is built on a client-server architecture where communication is handled through a custom-made network protocol.
* **Server**: Responsible for game logic, maintaining client lists, and storing the authoritative game state.
* **Client**: Provides a JavaFX-based Graphical User Interface (GUI), handles user input, and implements latency prediction.

## Key Features
* **Multiplayer Gameplay**: Support for up to 4 players per lobby.
* **Lobby & Chat**: Users can create/join lobbies and communicate via a real-time text chat.
* **Reliable Networking**: A custom UDP-based protocol with reliable delivery for system and chat messages.
* **Ball physics**: Includes shot power (0-100%), angle (0-360°), friction-based deceleration, and wall bounces.

## Technology Stack
* **Language**: Java 25.
* **Framework**: JavaFX for the GUI.
* **Build System**: Gradle 9.3.1.
* **Logging**: SLF4J and Logback.

## Getting Started

### Execution
The project is executed through the `Main` class, which accepts specific arguments for the server or client roles.

**To start a Server:**
```bash
java -jar build/libs/golfit-0.0.1-ALPHA.jar server <port>
```
**To start a Client:**
```bash
java -jar build/libs/golfit-0.0.1-ALPHA.jar client <adress>:<port> <name>
```

## Project Documentation
Detailed information about project design and progress can be found in the following documents:
* [**Requirement Analysis**](docs/requirementAnalysis.md): Comprehensive analysis of functional and non-functional requirements.
* [**Network Protocol**](docs/networkProtocol.md): Technical specifications of the custom communication protocol.
* [**Project Diary**](docs/diary.md): Chronological record of milestones and task assignments.

## Contributors
GOLFIT was developed by:
* Anton Bilokon a.bilokon@stud.unibas.ch
* Demian Piodi d.piodi@stud.unibas.com
* Tamer Erata t.erata@stud.unibas.ch
* Constantin Chau c.chau@stud.unibas.ch