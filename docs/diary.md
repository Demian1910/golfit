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
Meeting via Discord call with Demian, Tamer, and Constantin to define tasks and responsibilities for the next phase of development. We established a clear breakdown of features and technical requirements:

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
* **QA Concept (10):** Reasonable Software Quality concept in written form (see slides).
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

## Entry #6 10.03.26
Demian: ball mechanic implemented in a separate branch golf-ball. Ball can move with dynamic friction using mouse. Next step is implementing bouncing mechanic for Milestone 4