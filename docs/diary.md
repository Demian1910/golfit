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


## Entry #10 22.03.26
Anton implemented lobby logics and chat messaging. Network protocol was updated with few entries, such as, separate communication commands
for different types of messages i.e. broadcast, whisper and lobby messages handled differently. Such mechanics allows
to separate and scale logic connected to different communication types. Lobbies are fully implemented, each lobby has 
status fields(open, ongoing, running), set of clients and different useful getter and setter functions. Current implementation 
allows it to integrate lobby logic with GUI components. 

## Entry #11 27.03.26
Constantin worked on the implementation of bounce logic, by collecting obstacle data and terrain data in arrays.
and storing them in an array. And also implemented a function to collect shapes close to the ball.

## Entry #12 31.03.26
Tamer did some bugfixes but most importantly Tamer Implemented a Chat GUI. However it still needs polishing 
and making it align with the milestone tasks will probably be needed. 

Constantin worked on the implementation of bounce logic,
including adding more realistic friction to the ball and adding collision to the ball. 
The array containing mapdata is not perfect and will need readjustments in the future.
Also fixed the course rendering since the blue sidebar had to be deleted.

## Entry #13 02.04.26
Tamer Worked on the chat GUI sidebar today. Started by adding a username label at the top so players can see who 
theyre logged in as, then added Create Lobby and Leave Lobby buttons to the lobby section. The Leave Lobby button 
dims out when your not in a lobby and lights up when you join one which is handled through EventBus events that get 
emitted when the server confirms a join or leave. After that I implemented slash command support in the chat input 
field, typing something like /help, /whoami, /changeUsername, /whisper, or /q now gets intercepted and handled as a 
command instead of being sent as a regular message. While testing all of this I ran into several bugs that had been 
sitting in the codebase for a while. the client dispatch was missing a case for SYS_LOBBY_JOINED so the join button 
worked but the leave button never enabled, there was a mismatch between SYS_LOBBY_LEAVE and SYS_LOBBY_LEFT that caused 
leave responses to be silently dropped which took me a bit to figure out. the disconnect packet was being sent on an 
unreliable channel so the server often never recieved it, and disconnecting while in a lobby left ghost state on the 
server because nobody cleaned up lobby membership. Fixed all of those along the way. Also reworked lobby creation so it 
no longer auto joins the creator, added a max limit of 10 lobbies with sequential IDs made empty lobbies get deleted 
when the last player leaves. and added host reassignment so the lobby doesnt break when the original creator leaves. 
Oh also the leave lobby mismatch thing I mentoned earlier was probably the most annoying one because everything looked 
fine on the client side but the server was just dropping the packet silently so I had to dig into the dispatch to find 
it. Removed the pencil edit button for username changes and the disconnect button since they felt redundant, username 
changes go through /changeUsername now and disconnecting is handled by /q or just closing the window. i can say summing 
up i would say that lobby functions and chat gui logic should be at least 95% finished the 5% being if they are ever 
going to be moved in a way that for example we make a start screen then an active lobbies screen etc. which in that 
case the change will just be like i said moving them to their own individual places which would just change where the 
components live in the UI not what actually they do.

## Entry #14 06.04.26

Constantin: fixed the implementation of the course. Previously there was a conflict between the sidebar and course, where the sidebar would "eat" the space of the course. Now the course is a fixed 1100 x 600, resizing will need to be adjusted.

Demian: 
- Implemented SplashView as a not in-game state displayed
- Used EventBus to automatically switch UI screens when a match starts or ends.
- Added server-authoritative lobby start logic, ensuring all clients transition together when any player starts a match.
- Implemented server-side stroke counting and end-game detection.
- Added persistent result tracking in 'results.csv' with winner detection and stroke summaries.
- Completed the translation of all remaining German comments and Javadocs to English for not breaking coding conventions.

## Entry #15 11.04.26
Tamer added comprehensive JavaDoc to anything he could find which might have not been worth it but he had already invested 
too much time in it to give up when he noticed that it would be a painful process, So he pulled through to get those 
bittersweet 10 points for the javadoc task hopefully...

## Entry #16 13.04.26
Presented M4 Product tasks in exercise session. Group discussion about project presentation and task division.
- Anton: About a Game, Demo! (3min)
- Demian: Rules to Code (1.5min)
- Constantin: QA and Technology! (2.5min)
- Tamer: Progress Report (2min

Discussion about next milestones features: interactive environment, aiming and power shamanism, screen resizing, game layout
textures, ball bug in water, hole logic position shifted.

## Entry #17 17.04.26
Demian added dynamic aim indicator with cone segments that aims in the direction of the aiming.
The sections of the cone start with one green and increase in number and change color to red, based on the power of the shot. They also spin based on the direction of the shot. This aiming indicator is only visible from the client aiming on its client, and now shots can only be charged when the considered ball is not moving.

## Entry #18 18 20.4.26
Constantin implemented the logic for the bouncy balls, so that it applies the appropriate friction.

## Entry #19 22.04.26
Tamer wrote unit tests for the project's core components with the help of his teammates. Created 7 test classes with 75 
tests total covering the protocol layer which Anton helped with (MessageBuilder, MessageParser, Channel, ChannelManager,
PendingPacket) and the game physics engine which Demian and Constantin helped with sorting out (ServerBall — stroke mechanics, friction,
speed clamping, position reset, collision), and the EventBus pub/sub system. All tests pass. JaCoCo coverage reports are generated 
alongside the test results.

Anton helped Constantin with the implementation of the interactive environment on communication side.

## Entry #20 23.04.26
Demian: winner correctly determined, In case of more players finishing with the same stroke count, the one that arrived to the hole first wins.

Anton changed and updated high scores, such that they are displayed at the end of every game, and the Server is responsible of changing the persistently stored version. 
Cheat code added. Golf ball animation implemented.

## Entry #21 24.4.26 
Constantin implemented the one-time collision on the trees, so that it affects the physics server-side and the rendering client-side appropriately.

Anton: grass background improved.

## Entry #22 30.4.26
Constantin implemented the mechanic for the switch, so that the barrier and bridge appear/disappear. Also reallocated trees 