# Requirement Analysis: GOLFIT

## 1. Introduction

### 1.1 Purpose

This specification describes requirements for the GOLFIT video game. This document is designed for developers and evaluators of the project. 

Minigolf as the way to have fun while maintaining # Requirement Analysis: GOLFIT

## 1. Introduction

### 1.1 Purpose

This specification describes requirements for the GOLFIT video game. This document is designed for developers and evaluators of the project. 

Minigolf as the way to have fun while maintaining competitive elements became popular in the whole world. Our goal is to create a simple yet fun video game about minigolf, where players can experience this game, without need to use real golf court or equipment. 

## 1.2 Scope and Objectives

**Project Scope** This project is developed by 4 people as part of the Programming project lecture at University of Basel. GOLFIT is a video game about minigolf, that implements server/client architecture, where the communication is made through custom made network protocol. Where users (clients) are able to establish connection with server and use the interface that server provides, such as: Lobby creation, chat and game itself. 

**Essential Functions**

**Server** 
- Listen for clients connections
- Maintaining clients lists
- Handle disconnected users
- Handle malformed/faulty requests
- Handle package loss
- Responding to client's requests
- Store the superior game state
- Contains game logic

**Client** 
- Establish connection with server
- Send communication requests to server
- Provide GUI for game, chat and lobby
- Predict user movement to hide latency


**What the software will not do**
- Use handshake procedure before establishing communication betweeen server-client
- Provide authentification interface
- Store persistently client's IP addresses, usernames
- Implement rank system
- Use text based network protocol

**Project Goals** The goal is to create a user-friendly, stable build and provide modern multiplayer game interfaces for user experience.

### 1.3 Definitions

- **GOLFIT** Open-source multiplayer video game about minigolf.
- **Server** Part of the client/server arhitecture, it is a software that provides different interfaces for client, while communicating with latter via defined network protocol.
- **Client** Part of the client/server architecture, it is a software that user interacts with, it provides GUI for the user and allows him to use server features to achieve some result.
- **GUI** Graphical user interface, implementation layer that allows user to interct with the software via graphical elements.
- **Network Protocol** Clearly defined set of rules and principles, that is used by server and client in order to standardize their communication, avoid faulty and malformed requests.
- **Multiplayer game** is a video game in which more than one person can play in the same game environment at the same time. 
- **Game logic** Established rules and concepts that are defining game behaviour, its flow and win/lose conditions.
- **Game state** Reflection of the game at a arbitrary chosen time slot.

## 2. User profiles and Software Constraints

### 2.1 User profiles

**Primary User Group: University students, employees, people interested in minigolf**

- **Technical skills:** Users familiar with jar files, OS console interaction, basic computer literacy.
- **Needs:** Time spending with friends, engaging into minigolf experience. 
- **Language** Primarily English, with potential multi-language support.

**User assumptions** 
- Users have basic understanding how multiplayer game works.
- Users are familiar with execution of jar file and simple command line arguments.
- They can identify their IP address 
- No programming knowledge is required.

### 2.2 Software Constraints

**Technical Constraits**
- Must be compatible with Gradle 9.3.1 version.
- Must work as LAN application.
- Must be compatible with java 25.0.2 version.
- Must define and use its network protocol.
- Must not use technologies listed in Black List of the Programming project lecture.
- Can implement technologies listed in Allow List of the Programming project lecture.

### 2.3 Assumptions and dependencies

**Assumptions** 
- Java version will remain stable during development.
- The JavaFX framework will continue to be supported
- Usercount will not exceed 50 people

**Dependencies**
- Java Development Kit (JDK) 25
- Availability of JavaFX

**External prerequisites** 
- Communication tools for team coordination (GitLab, Discord etc.)

## 3. Specific Requirements

### 3.1 Server

#### 3.1.1 Connection Management

- **/F10/** Server must listen for incoming connections.
- **/F11/** Server must maintain a list of connected clients.
- **/F12/** Server must handle disconnected clients.
- **/F13/** Server must handle malformed/faulty requests.
- **/F14/** Server must handle package loss.
- **/F15/** Server must respond to client's requests.

#### 3.1.2 Request Handling
- **/F16/** Server must validate and parse all incoming request according to the network protocol.
- **/F17/** Server must discrad all malformed requests.
- **/F18/** Server must respond to vadid client requests withing a time frame. (<200 ms under normal load)

#### 3.1.3 Game State Management
- **/F19/** Server must store the superior game state.
- **/F20/** Server must broadcast the superior game state to all clients in a game section.
- **/F21/** Server must resolve all the state conflicts.

#### 3.1.4 Lobby Management
- **/F22/** Server must allow users to create new lobbies.
- **/F23/** Server must allow users to join existing lobbies.
- **/F24/** Server must allow lobby host to start the game, when at least 2 players are connected.
- **/F25/** Server must delete lobby when all players are disconnected.
- **/F26/** Server must enforce maximum of 4 players per lobby.

### 3.2 Client

#### 3.2.1 Connection Management
- **/F27/** Client must establish connection with server using host address and port provided by the user.
- **/F29/** Client must handle connection failure and notify the user.
- **/F30/** Client must attempt to reconnect to the server if connection is lost.

#### 3.2.2 Game interaction
- **/F31/** Client must allow the user to aim a shot and its power.
- **/F32/** Client must send angle and shot power to the server.
- **/F33/** Client must display the current game state as received from the server.
- **/F34/** Client must implement a prediction system to hide latency.
- **/F35/** Client must display number of strokes taken by each player.
- **/F36/** Client must display remaining time and leaderboard of the current game.

#### 3.2.3 Lobby Interaction
- **/F37/** Client must display list of available lobbies.
- **/F38/** Client must allow the user to create a new lobby.
- **/F39/** Client must allow the user to join and leave an existing lobby.
- **/F40/** Client must display the list of players in the lobby.
- **/F41/** Client must allow lobby host to start the game.

### 3.3 Network Protocol

#### 3.3.1 General 

- **/F42/** Network protocol must use binary format.
- **/F43/** Network protocol must use defined header format: type(1 byte),channel (1 byte), senderId (4 bytes), sequence(4 bytes) total 10 bytes.
- **/F44/** Network protocol must define payload format as byte array.
- **/F45/** Network protocol must define different message types, such as: connection, disconnection, lobby operations, 
game state updates, user inputs, chat operations.

#### 3.3.2 Reliability
**/F46/** Protocol must defines mechanism for package loss (sequencing).

### 3.4 GUI
- **/F47/** GUI must be implemented using JavaFX framework.
- **/F48/** GUI must be able to display game state.
- **/F49/** GUI must be able to display lobby list.
- **/F50/** GUI must be able to display chat messages.

### 3.5 Game Logic

#### 3.5.1 Shot mechanics
- **/F51/** Shot must be defined with 2 parameters: power(0 - 100%) and angle(0-360).
- **/F52/** Shot must be move into direction of the angle with specified power.
- **/F53/** Shot must be deccelerate with time dues to the friction.
- **/F54/** Shot must be bounce off the walls according to basic reflection physics.
- **/F55/** Shot must be considered "in the hole" when it arrives to the specified area with relatively small velocity.

#### 3.5.2 Scoring and Win Condition
- **/F56/** Each shot taken by the player must increment their stroke count by one.
- **/F57/** Player with the fewest total strokes must be declared as the winner. 
- **/F58/** Afer the game ends final scoreboard must be displayed to all users.

### 3.6 Chat
- **/F59/** System must provide text based chat for all players in the same lobby.
- **/F60/** Chat must be accessible during a game.

## 4. Non-functional Requirements

### 4.1 Performance 
- **/NF101/** Server must be able to handle up to 50 users simultaniously.
- **/NF102/** Server should broadcast game state to all clients in a game within 200ms under normal network conditions.
- **/NF103/** Client GUI should maintain a minimum of 30 FPS during gameplay (ref hardware: macbook air m1, with 3.2 GHz Silicon, 8 GB 2667 MHz DDR4).

### 4.2 Reliability
- **/NF104/** Server must be continue operating game, even if 1 client is disconnected.
- **/NF105/** Server and Client must produce log files, that can be used for debugging purposes.


### 4.3 Usability 
- **/NF106/** GUI must use clear labels, icons and intuitive controls.

## 5. Acceptance Criteria
- **/AC101/** Client can connect to the server using host address and port.
- **/AC102/** Multiple clients can connect to the server.
- **/AC103/** Players can create and join lobbies using GUI.
- **/AC104/** Game session starts when at least 2 players are connected, and the lobby host pressed the start button.
- **/AC105/** Players can aim and shoot ball using angle and power.
- **/AC106/** Ball physics work correctly as expected.
- **/AC107/** Scoreboard is displayed correctly.
- **/AC108/** Winner is defined as one with the fewest total strokes.
- **/AC109/** Chat messages and server announcements are delivered to all players in the same lobby.
- **/AC110/** Server is not crashing when client disconnects.


## Appendix

### Appendix 1A. Use Cases

#### Use Case 1: Connect to the server
**Name** Connection to the server.
**Actors** Anton(CS student at University of Basel).
**Process:** 
Anton wants to play minigolf with his friends. But first he needs to connect to the server. He opens the terminal and 
runs:  `java -jar golfit.jar. client 192.168.1.100:12345` upon execution, client window opens and he is connected to the server.

**Postcondition Success:** 
Client window is opened and Anton is connected to the server.

 elements became popular in the whole world. Our goal is to create a simple yet fun video game about minigolf, where players can experience this game, without need to use real golf court or equipment. 

## 1.2 Scope and Objectives

**Project Scope** This project is developed by 4 people as part of the Programming project lecture at University of Basel. GOLFIT is a video game about minigolf, that implements server/client architecture, where the communication is made through custom made network protocol. Where users (clients) are able to establish connection with server and use the interface that server provides, such as: Lobby creation, chat and game itself. 

**Essential Functions**

**Server** 
- Listen for clients connections
- Maintaining clients lists
- Handle disconnected users
- Handle malformed/faulty requests
- Handle package loss
- Responding to client's requests
- Store the superior game state
- Contain game logic

**Client** 
- Establish connection with server
- Send communication requests to server
- Provide GUI for game, chat and lobby
- Predict user movement to hide latencty


**What the software will not do**
- Use handshake procedure before establishing communication betweeen server-client
- Provide authentification interface
- Store persistently client's IP addresses, usernames
- Implement rank system
- Use text based network protocol

**Project Goals** The goal is to create a user-friendly, stable build and provide modern multiplayer game interfaces for user experience.

### 1.3 Definitions

- **GOLFIT** Open-source multiplayer video game about minigolf.
- **Server** Part of the client/server architecture, it is a software that provides different interfaces for client, while communicating with latter via defined network protocol.
- **Client** Part of the client/server architecture, it is a software that user interacts with, it provides GUI for the user and allows him to use server features to achieve some result.
- **GUI** Graphical user interface, implementation layer that allows user to interct with the software via graphical elements.
- **Network Protocol** Clearly defined set of rules and principles, that is used by server and client in order to standardize their communication, avoid faulty and malformed requests.
- **Multiplayer game** is a video game in which more than one person can play in the same game environment at the same time. 
- **Game logic** Established rules and concepts that are defining game behaviour, its flow and win/lose conditions.
- **Game state** Reflection of the game at a arbitrary chosen time slot.

## 2. User profiles and Software Constraints

### 2.1 User profiles

**Primary User Group: University students, employees, people interested in minigolf**

- **Technical skills:** Users familiar with jar files, OS console interaction, basic computer literacy.
- **Needs:** Time spending with friends, engaging into minigolf experience. 
- **Language** Primarily English, with potential multi-language support.

**User assumptions** 
- Users have basic understanding how multiplayer game works.
- Users are familiar with execution of jar file and simple command line arguments.
- They can identify their IP address 
- No programming knowledge is required.

### 2.2 Software Constraints

**Technical Constraints**
- Must be compatible with Gradle 9.3.1 version.
- Must work as LAN application.
- Must be compatible with java 25.0.2 version.
- Must define and use its network protocol.
- Must not use technologies listed in Black List of the Programming project lecture.
- Can implement technologies listed in Allow List of the Programming project lecture.

### 2.3 Assumptions and dependencies

**Assumptions** 
- Java version will remain stable during development.
- The JavaFX framework will continue to be supported
- Usercount will not exceed 50 people

**Dependencies**
- Java Development Kit (JDK) 25
- Availability of JavaFX

**External prerequisites** 
- Communication tools for team coordination (GitLab, Discord etc.)

## 3. Specific Requirements

### 3.1 Server

#### 3.1.1 Connection Management

- **/F10/** Server must listen for incoming connections.
- **/F11/** Server must maintain a list of connected clients.
- **/F12/** Server must handle disconnected clients.
- **/F13/** Server must handle malformed/faulty requests.
- **/F14/** Server must handle package loss.
- **/F15/** Server must respond to client's requests.

#### 3.1.2 Request Handling
- **/F16/** Server must validate and parse all incoming request according to the network protocol.
- **/F17/** Server must discrad all malformed requests.
- **/F18/** Server must respond to valid client requests within a time frame. (<200 ms under normal load)

#### 3.1.3 Game State Management
- **/F19/** Server must store the superior game state.
- **/F20/** Server must broadcast the superior game state to all clients in a game section.
- **/F21/** Server must resolve all the state conflicts.

#### 3.1.4 Lobby Management
- **/F22/** Server must allow users to create new lobbies.
- **/F23/** Server must allow users to join existing lobbies.
- **/F24/** Server must allow lobby host to start the game, when at least 2 players are connected.
- **/F25/** Server must delete lobby when all players are disconnected.
- **/F26/** Server must enforce maximum of 4 players per lobby.

### 3.2 Client

#### 3.2.1 Connection Management
- **/F27/** Client must establish connection with server using host address and port provided by the user.
- **/F29/** Client must handle connection failure and notify the user.
- **/F30/** Client must attempt to reconnect to the server if connection is lost.

#### 3.2.2 Game interaction
- **/F31/** Client must allow the user to aim a shot and its power.
- **/F32/** Client must send angle and shot power to the server.
- **/F33/** Client must display the current game state as received from the server.
- **/F34/** Client must implement a prediction system to hide latency.
- **/F35/** Client must display number of strokes taken by each player.
- **/F36/** Client must display remaining time and leaderboard of the current game.

#### 3.2.3 Lobby Interaction
- **/F37/** Client must display list of available lobbies.
- **/F38/** Client must allow the user to create a new lobby.
- **/F39/** Client must allow the user to join and leave an existing lobby.
- **/F40/** Client must display the list of players in the lobby.
- **/F41/** Client must allow lobby host to start the game.

### 3.3 Network Protocol

#### 3.3.1 General 

- **/F42/** Network protocol must use binary format.
- **/F43/** Network protocol must use defined header format: type(1 byte),channel (1 byte), senderId (4 bytes), sequence(4 bytes) total 10 bytes.
- **/F44/** Network protocol must define payload format as byte array.
- **/F45/** Network protocol must define different message types, such as: connection, disconnection, lobby operations, 
game state updates, user inputs, chat operations.

#### 3.3.2 Reliability
**/F46/** Protocol must defines mechanism for package loss (sequencing).

### 3.4 GUI
- **/F47/** GUI must be implemented using JavaFX framework.
- **/F48/** GUI must be able to display game state.
- **/F49/** GUI must be able to display lobby list.
- **/F50/** GUI must be able to display chat messages.

### 3.5 Game Logic

#### 3.5.1 Shot mechanics
- **/F51/** Shot must be defined with 2 parameters: power(0 - 100%) and angle(0-360).
- **/F52/** Shot must be move into direction of the angle with specified power.
- **/F53/** Shot must be decelerate with time due to the friction.
- **/F54/** Shot must be bounce off the walls according to basic reflection physics.
- **/F55/** Shot must be considered "in the hole" when it arrives to the specified area with relatively small velocity.

#### 3.5.2 Scoring and Win Condition
- **/F56/** Each shot taken by the player must increment their stroke count by one.
- **/F57/** Player with the fewest total strokes must be declared as the winner. 
- **/F58/** After the game ends final scoreboard must be displayed to all users.

### 3.6 Chat
- **/F59/** System must provide text based chat for all players in the same lobby.
- **/F60/** Chat must be accessible during a game.

## 4. Non-functional Requirements

### 4.1 Performance 
- **/NF101/** Server must be able to handle up to 50 users simultaneously.
- **/NF102/** Server should broadcast game state to all clients in a game within 200ms under normal network conditions.
- **/NF103/** Client GUI should maintain a minimum of 30 FPS during gameplay (ref hardware: macbook air m1, with 3.2 GHz Silicon, 8 GB 2667 MHz DDR4).

### 4.2 Reliability
- **/NF104/** Server must be continue operating game, even if 1 client is disconnected.
- **/NF105/** Server and Client must produce log files, that can be used for debugging purposes.


### 4.3 Usability 
- **/NF106/** GUI must use clear labels, icons and intuitive controls.

## 5. Acceptance Criteria
- **/AC101/** Client can connect to the server using host address and port.
- **/AC102/** Multiple clients can connect to the server.
- **/AC103/** Players can create and join lobbies using GUI.
- **/AC104/** Game session starts when at least 2 players are connected, and the lobby host pressed the start button.
- **/AC105/** Players can aim and shoot ball using angle and power.
- **/AC106/** Ball physics work correctly as expected.
- **/AC107/** Scoreboard is displayed correctly.
- **/AC108/** Winner is defined as one with the fewest total strokes.
- **/AC109/** Chat messages and server announcements are delivered to all players in the same lobby.
- **/AC110/** Server is not crashing when client disconnects.


## Appendix

### Appendix 1A. Use Cases

#### Use Case 1: Connect to the server
**Name** Connection to the server.
**Actors** Anton(CS student at University of Basel).
**Process:** 
Anton wants to play minigolf with his friends. But first he needs to connect to the server. He opens the terminal and 
runs:  `java -jar golfit.jar. client 192.168.1.100:12345` upon execution, client window opens and he is connected to the server.

**Postcondition Success:** 
Client window is opened and Anton is connected to the server.

