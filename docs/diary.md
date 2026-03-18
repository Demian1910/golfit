# Diary - GOLFIT
| Contributors | Email                   |
|--------------|-------------------------|
| Demian Piodi | d.piodi@stud.unibas.com |
|Anton Bilokon | a.bilokon@stud.unibas.ch |
|Tamer Erata | t.erata@stud.unibas.ch |
|Constantin Chau | c.chau@stud.unibas.ch |

## Entry #1 02.03.26
Each team member has committed to the project for the first time. Anton and Demian had a productive phone call. They 
coordinated the project idea discussed details of network architecture and the project structure. They agreed on the 
project name: **GOLFIT**. They also discussed the project timeline and milestones.
## Entry #2 02.03.26 
Anton created simple project structure and added the first versions for the Client and Server. He also added the 
first prototype of the network protocol. Now server and client can communicate with each other using connectionless UDP.
## Entry #3 02.03.26
Demian added a comprehensive game concept, with clear game rules and mechanics outline. Added document includes our 
vision of the game, its objectives win/loss conditions. Game rules section clearly underlines how the game process will
be. On top Client and Server responsibilities are clearly defined.

## Entry #4 3.3.26
We had a group discussion and concluded that the game should be real-time based unlike the traditional turn-based golf game.
We divided responsibilities so everyone can work has clear tasks. Anton will focus on networking, Tamer on physics while
Constantin and Demian focus on the graphics of the game. 
We briefly discussed how we should handle the presentation for milestone I. Tony: Networking, Constantin: Timeline 
Tamer: Goals, rules and mechanics of Golfit. Demian will present the mock-up.

## Entry #5 06.03.26
Meeting via Discord call with Demian, Tamer, and Constantin to define tasks and responsibilities for the next phase of development. 
We established a clear breakdown of features and technical requirements:

### Individual Assignments

**ANTON:**
* **3nc0d1️⃣ng ER2ÖR (15):** Protocol and content is properly encoded, decoded and validated.
* **Ping (10):** A ping message is regularly sent from server to client and processed meaningfully (connection losses are detected and handled).
* **Protocol Code (10):** The human-readable network protocol is defined and implemented.

**TAMER:**
* **Call Me Bob (5):** Players can change their nicknames.
* **whoami (5):** The client suggests a nickname based on the system username.
* **bob_001 (5):** The server assures that player nicknames are unique and changes duplicates in a consistent manner by assigning appropriate names.
* **Login (10):** Arbitrarily many clients can log on to the server.
* **Logout (5):** The server handles a client logout in a meaningful way.
* **Chat (15):** Client to client chat is working.

**DEMIAN:**
* **Physics Prototype:** Create a ball object that can be thrown with a certain velocity and direction, that slows down over time, and that bounces on a wall. Its position has to be known in every moment. (Note: integration with a future UI bar for direction/velocity).

**CONSTANTIN:**
* **Requirements & GUI:** Create a text file describing the requirements of the graphic interface by next week for team review. Subsequently, start the development of the game screen with JavaFX for the next milestone.

### Shared Goals (All)
* **Protocol Validator (15):** Network protocol is being used correctly.
* **Documentation:** Source code is sufficiently documented with JavaDoc.
* **Dear Diary (10):** Project diary is up to date and filled with meaningful entries.

---

### Internal Suggestion: Game Display Requirements (Demian to Constantin)
To ensure consistency with the game logic and mockup, the following requirements for the graphic interface were suggested:

* **Screen Division:** Maintain the mockup layout: game area, upper part (YOUR STROKES, TIME LEFT), Leaderboard, and Chat.
* **Adaptability:** The display should be partially adaptable, supporting full screen and resizing.
* **Modular Map Environment:** Create map objects (walls, ball, grass, lake, hole, door, bridge) that can be used to construct varied environments.
* **Level Design:** Maps should be intuitive but offer multiple strategic paths to the destination.
* **Ball Mechanics:** Implement bounce logic where the ball reflects at the same angle it hits a wall.
* **Physics Constraints:** Avoid placing walls too close to each other to prevent issues with the bouncing logic.
* **Interactive Elements:** Design interactive environments (doors and bridges) that are graphically distinct but logically consistent.

## Entry #6 9.03.26
Tamer checked all the tasks for milestone 2 and informed the group accordingly and also told anton that the current Protocol is not
human readable anton then spent the day changing the protocol to make it human readable.

## Entry #7 10.03.26
Tamer either implemented or refined all his tasks which include: Call me Bob, whoami, bob_001, Login, Logout, Chat
with the help of Anton also kind of cleaned up the handlers for the client and server which went from multiple separated
classes like chathandler, loginhandler, *whatever else was there i forgot*handler... etc. to just a combined SystemHandler inside both the Client
and the Server folders inside the handlers folder so .../client/handlers/SystemHandler and .../server/handlers/SystemHandler were made.
Demian: ball mechanic implemented in a separate branch golf-ball. Ball can move with dynamic friction using mouse. Next step is implementing bouncing mechanic for Milestone 4

## Entry 8: 12.03.26

Constantin implemented game window and the graphics for the golf course, including walls, sand, ice, balls, trees, golf hole, switch and ramps.
added Javadoc to the graphics. 

## Entry #9 16.03.26
The team has met after the Mileston 2 in presence hour to discuss about the development tasks division for **Milestone 4**

### Individual Assignments (Milestone 4)

**DEMIAN:**
* **Game Logic (40):** Implementation of the main game mechanics and ensuring a playable state.
* **Game State (10):** Development of the server-side logic to handle and synchronize the game state.
* **Rules to Code (5):** Mapping the formal game rules into the actual code structure.
* **Manual (10):** Drafting a written guide for players on how to play GOLFIT.

**CONSTANTIN:**
* **Progress Report (15):** Documenting the project timeline, individual responsibilities, and problem-solving strategies.
* **QA Concept & Advanced QA (20):** Developing a detailed software quality plan and implementing measurement protocols.
* **Game logic**: work with Demian to implement the the logival walls object and bouncing logic 

**ANTON:**
* **Protocol & Networking:** Finalizing the human-readable **Protocol Code (10)** and providing the **Protocol Document (5)** with command examples.
* **Social Features:** Implementing **Lobby/Lounging (5)** for multiple rooms, **Broadcast (5)** for global messages, and **Whisper (5)** for private 1-to-1 chats.
* **Data Management:** Implementing the **Game, Lounging, and Player Lists (15)**.
* **Tooling:** Creating the **Build Script (5)** for JAR/JavaDoc generation and the **Command Line (5)** parser for host/port/username.

**TAMER:**
* **GUI (25):** Finalizing the user interface, specifically focusing on the Chat and Game screens.
* **Librarian (10):** Integrating an external library (outside of JavaFX/JUnit) to enhance project functionality.

---