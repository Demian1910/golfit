# The GolfIt Protocol

## 1. Overview and Purpose

The GolfIt Protocol enables two-way communication between a client and a remote authoritative server.
It is built on top of UDP/IP and uses a text-based message format.
All messages are UTF-8 encoded strings.

## 2. Architecture and Design

Each message consists of exactly 5 pipe-delimited fields, terminated by a newline character:

```
<type>|<channel>|<senderId>|<sequence>|<payload>\n
```

Example:
```
SYS_CONNECT|CH_SYSTEM|0|0|Anton\n
SYS_PONG|CH_PINGS|1|0|\n
```

- **type** — message type string (e.g. `SYS_CONNECT`)
- **channel** — channel identifier string (e.g. `CH_SYSTEM`)
- **senderId** — integer ID of the sender
- **sequence** — integer sequence number
- **payload** — variable-length UTF-8 string, may be empty

Constants:
```java
public static final char DELIMITER  = '|';
public static final char TERMINATOR = '\n';
public static final int  FIELD_COUNT = 5;
```

## 3. Message Types

### 3.1 System Messages

| Constant | Value | Direction | Channel | Description |
|---|---|---|---|---|
| `SYS_CONNECT` | `"SYS_CONNECT"` | Client → Server | `CH_SYSTEM` | Connection request |
| `SYS_CONNECTED` | `"SYS_CONNECTED"` | Server → Client | `CH_SYSTEM` | Connection accepted, assigns client ID and username |
| `SYS_DISCONNECT` | `"SYS_DISCONNECT"` | Client → Server | `CH_PINGS` | Graceful disconnect notice |
| `SYS_PING` | `"SYS_PING"` | Client → Server | `CH_PINGS` | Ping request |
| `SYS_PONG` | `"SYS_PONG"` | Server → Client | `CH_PINGS` | Ping response, payload is session ID |
| `SYS_ACK` | `"SYS_ACK"` | Both | any reliable channel | Acknowledgement for reliable delivery |

### 3.2 Lobby Messages

| Constant | Value | Direction       | Channel | Description                                         |
|---|---|-----------------|---|-----------------------------------------------------|
| `SYS_LOBBY_CREATE` | `"SYS_LOBBY_CREATE"` | Client → Server | `CH_SYSTEM` | Request to create a lobby                           |
| `SYS_LOBBY_CREATED` | `"SYS_LOBBY_CREATED"` | Server → Client | `CH_SYSTEM` | Lobby creation confirmation, payload is lobby ID    |
| `SYS_LOBBY_JOIN` | `"SYS_LOBBY_JOIN"` | Client → Server | `CH_SYSTEM` | Request to join a lobby, payload is lobby name      |
| `SYS_LOBBY_JOINED` | `"SYS_LOBBY_JOINED"` | Server → Client | `CH_SYSTEM` | Lobby join confirmation, payload is lobby ID        |
| `SYS_LOBBY_LEAVE` | `"SYS_LOBBY_LEAVE"` | Client → Server | `CH_SYSTEM` | Request to leave current lobby, payload is lobby ID |
| `SYS_LOBBY_LEFT` | `"SYS_LOBBY_LEFT"` | Server → Client | `CH_SYSTEM` | Lobby leave confirmation, payload is lobby ID       |
| `SYS_LOBBY_LIST` | `"SYS_LOBBY_LIST"` | Server → Client | `CH_SYSTEM` | List of available lobbies                           |
| `SYS_LOBBY_INFO` | `"SYS_LOBBY_INFO"` | Server → Client | `CH_SYSTEM` | List of players attached to a particular lobby      |
| `SYS_PLAYERS_LIST` | `"SYS_PLAYERS_LIST"` | Server → Client | `CH_SYSTEM` | List of all players connected to the server         |
| `SYS_LOBBY_START` | `"SYS_LOBBY_START"` | Client → Server | `CH_SYSTEM` | Request to game start                               |
| `SYS_GAME_START` | `"SYS_GAME_START"` | Server → Client | `CH_SYSTEM` | Inform game is starting                             |
| `SYS_STROKE_UPDATE` | `"SYS_STROKE_UPDATE"` | Server → Client | `CH_SYSTEM` | Inform on players stroke counts                     |
| `SYS_GAME_END` | `"SYS_GAME_END"` | Server → Client | `CH_SYSTEM` | Game over and result                                |

### 3.3 Chat Messages

| Constant            | Value                 | Direction | Channel | Description                                                                  |
|---------------------|-----------------------|---|---|------------------------------------------------------------------------------|
| `SYS_CHAT_BROADCAST` | `"SYS_CHAT_BROADCAST"` | Both | `CH_CHAT` | Public chat message, server broadcasts to all clients with original sender ID |
| `SYS_CHAT_PRIVATE`  | `"SYS_CHAT_PRIVATE"`  | Both | `CH_CHAT` / `CH_SYSTEM` | Private chat, payload format: `<targetUsername>:<message>`                   |
| `SYS_CHAT_LOBBY`    | `"SYS_CHAT_LOBBY"`    | Both | `CH_CHAT` / `CH_SYSTEM` | Lobby message, payload format: `<lobbyId>:<message>`                         |

### 3.4 Movement Messages

| Constant | Value | Direction | Channel       | Description |
|---|---|---|---------------|---|
| `MOVE_INPUT` | `"MOVE_INPUT"` | Client → Server | `CH_SYSTEM`   | Player movement input |
| `MOVE_UPDATE` | `"MOVE_UPDATE"` | Server → Client | `CH_MOVEMENT` | Authoritative position update broadcast to all clients |

## 4. Channels

| Constant | Value | Reliable | Description |
|---|---|---|---|
| `CH_SYSTEM` | `"CH_SYSTEM"` | Yes | System and lobby messages |
| `CH_CHAT` | `"CH_CHAT"` | Yes | Chat messages |
| `CH_MOVEMENT` | `"CH_MOVEMENT"` | No | Movement data (fire-and-forget) |
| `CH_PINGS` | `"CH_PINGS"` | No | Ping/pong keepalive and disconnect |

**Reliable channels** use sequence numbers and `SYS_ACK` to guarantee delivery. Unacknowledged packets are retried until a max retry count is reached. **Unreliable channels** send packets without acknowledgement.

## 5. Special IDs

| Constant | Value | Meaning |
|---|---|---|
| `ID_INVALID` | `0` | Initial ID before server assigns one |
| `ID_SERVER` | `1` | The server |
| `ID_BROADCAST` | `-1` | All connected clients |

## 6. Payload Formats

| Message | Payload Format                 | Example       |
|---|--------------------------------|---------------|
| `SYS_CONNECT` | `<username>`                   | `Anton`       |
| `SYS_CONNECTED` | `<assignedId>:<finalUsername>` | `2:Anton`     |
| `SYS_PONG` | `<sessionId>`                  | `123456`      |
| `SYS_LOBBY_CREATED` | `<lobbyId>`                    | `42`          |
| `SYS_LOBBY_JOIN` | `<lobbyId>`                    | `42`          |
| `SYS_LOBBY_JOINED` | `<lobbyId>`                    | `42`          |
| `SYS_LOBBY_LEAVE` | `<lobbyId>`                    | `42`          |
| `SYS_LOBBY_LEFT` | `<lobbyId>`                    | `42`          |
| `SYS_LOBBY_LIST` | `<id>:<host>:<size>:<status>&...`| `1:Anton:2:open&2:Berta:1:ongoing` |
| `SYS_LOBBY_INFO` | `<lobbyId>:<usr1>,<usr2>,...`  | `42:Anton,Berta` |
| `SYS_PLAYERS_LIST` | `<username1>:<username2>:...`  | `Anton:Berta` |
| `SYS_LOBBY_START`| `<lobbyId>`                    | `42`          |
| `SYS_GAME_START` | `<lobbyId>`                    | `42`          |
| `SYS_STROKE_UPDATE`| `<usr1>:<strokes>,<usr2>:<strokes>`| `Anton:1,Berta:2` |
| `SYS_GAME_END`   | `winner=<usr>;<usr1>:<strokes>...` | `winner=Anton;Anton:1,Berta:2` |
| `SYS_CHAT_PRIVATE` | `<targetUsername>:<message>`   | `Anton:hello` |

## 7. Connection Lifecycle

```
Client                                    Server
  |                                          |
  |-- SYS_CONNECT (senderId=0, username) --> |
  |                                          |  validates, assigns ID and username
  |<-- SYS_ACK ------------------------------|
  |<-- SYS_CONNECTED (id:username) ----------|
  |                                          |
  |       [connection established]           |
  |                                          |
  |-- SYS_PING --------------------------->  |
  |<-- SYS_PONG (sessionId) ---------------- |
  |                                          |
  |-- SYS_DISCONNECT (CH_PINGS) -----------> |
  |                                          |
```

If the requested username is already taken, the server appends a numeric suffix (e.g. `Anton_001`).

## 8. Reliable Delivery

The `ChannelManager` handles reliable delivery for `CH_SYSTEM` and `CH_CHAT` channels:

1. On send, the packet is stored in a `pendingAck` map
2. A `tick()` runs every 100ms — if a packet has not been acknowledged within `TIMEOUT_MS`, it is resent
3. After `MAX_RETRIES` failed attempts, the packet is dropped
4. On receipt, the receiver sends back `SYS_ACK` with the same sequence number
5. Duplicate packets are discarded

## 9. Error Handling

- Messages with an unrecognized type or channel are discarded
- Messages with fewer than 5 fields are discarded
- The server removes a client after it stops sending pings for longer than `TIMEOUT_PING_MS` (30s)
- Usernames that are already claimed are suffixed with a numeric counter before being assigned