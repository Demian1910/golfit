# GOLFIT

**Website:** [https://demian1910.github.io/golfit-website/](https://demian1910.github.io/golfit-website/)

GolfIt is a multiplayer video game centered around the experience of minigolf. Developed by a team of four students for the Programming project at the University of Basel, it aims to provide a fun golf game, with interactive environment.

## Project Overview
The game is built on a client-server architecture where communication is handled through a custom-made network protocol.
* **Server**: Responsible for game logic, maintaining client lists, and storing the authoritative game state.
* **Client**: Provides a JavaFX-based Graphical User Interface (GUI), handles user input, and implements latency prediction.

## Key Features
* **Multiplayer Gameplay**: Support for up to 4 players per lobby.
* **Lobby & Chat**: Users can create/join lobbies and communicate via a real-time text chat.
* **Reliable Networking**: A custom UDP-based protocol with reliable delivery for system and chat messages.
* **Game physics**: Friction-based deceleration, wall bounces and interactive elements such as trees, wall and bridge..
* **Dynamic Aim Indicator**: Visual cone-shaped power and direction indicator for intuitive stroke control.
* **Deterministic Player Colors**: Automatic and unique golf ball color assignment based on lobby join order.
* **Competitive Logic**: Game state tracking with stroke counts and finish-time tie-breakers.

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
java -jar build/libs/golfit-0.0.1-ALPHA.jar client <address>:<port> <name>
```

## Project Documentation
Detailed information about project design and progress can be found in the following documents:
* [**Project Overview**](GOLFIT_PROJECT.md): Complete description of the game, architecture, milestones and work done.
* [**Website**](https://demian1910.github.io/golfit-website/): Official GOLFIT presentation website.
* [**Network Protocol**](docs/networkProtocol.md): Technical specifications of the custom communication protocol.
* [**Outreach**](outreach): Folder containing game's presentation website, trailer, and pictures.
* [**Project Diary**](docs/diary.md): Chronological record of milestones and task assignments.


## Contributors
GOLFIT was developed by:
* Anton Bilokon a.bilokon@stud.unibas.ch
* Demian Piodi d.piodi@stud.unibas.ch
* Tamer Erata t.erata@stud.unibas.ch
* Constantin Chau c.chau@stud.unibas.ch