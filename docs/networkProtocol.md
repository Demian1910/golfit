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
| `SYS_PING` | `"SYS_PING"` | Client → Server | `CH_PINGS` | Ping request |
| `SYS_PONG` | `"SYS_PONG"` | Server → Client | `CH_PINGS` | Ping response |
| `SYS_ACK` | `"SYS_ACK"` | Both | any reliable channel | Acknowledgement for reliable delivery |

### 3.2 Lobby Messages

| Constant | Value | Direction | Channel | Description |
|---|---|---|---|---|
| `SYS_CREATE_LOBBY` | `"SYS_CREATE_LOBBY"` | Client → Server | `CH_SYSTEM` | Request to create a lobby |
| `SYS_LOBBY_CREATED` | `"SYS_LOBBY_CREATED"` | Server → Client | `CH_SYSTEM` | Lobby creation confirmation |
| `SYS_JOIN_LOBBY` | `"SYS_JOIN_LOBBY"` | Client → Server | `CH_SYSTEM` | Request to join a lobby |
| `SYS_LOBBY_JOINED` | `"SYS_LOBBY_JOINED"` | Server → Client | `CH_SYSTEM` | Lobby join confirmation |
| `SYS_LEAVE_LOBBY` | `"SYS_LEAVE_LOBBY"` | Client → Server | `CH_SYSTEM` | Request to leave current lobby |
| `SYS_LOBBY_LIST` | `"SYS_LOBBY_LIST"` | Server → Client | `CH_SYSTEM` | List of available lobbies |

### 3.3 Chat Messages

| Constant | Value | Direction | Channel | Description |
|---|---|---|---|---|
| `SYS_CHAT` | `"SYS_CHAT"` | Client → Server | `CH_CHAT` | Public chat message |
| `SYS_CHAT_PRIVATE` | `"SYS_CHAT_PRIVATE"` | Client → Server | `CH_CHAT` | Private chat message |

### 3.4 Movement Messages

| Constant | Value | Direction | Channel | Description |
|---|---|---|---|---|
| `MOVE_INPUT` | `"MOVE_INPUT"` | Client → Server | `CH_MOVEMENT` | Player movement input |
| `MOVE_UPDATE` | `"MOVE_UPDATE"` | Server → Client | `CH_MOVEMENT` | Authoritative position update |

## 4. Channels

Channels act as logical filters for messages. Each channel has a reliability setting.

| Constant | Value | Reliable | Description |
|---|---|---|---|
| `CH_SYSTEM` | `"CH_SYSTEM"` | Yes | System and lobby messages |
| `CH_CHAT` | `"CH_CHAT"` | Yes | Chat messages |
| `CH_MOVEMENT` | `"CH_MOVEMENT"` | No | Movement data (fire-and-forget) |
| `CH_PINGS` | `"CH_PINGS"` | No | Ping/pong keepalive |

**Reliable channels** use sequence numbers and `SYS_ACK` to guarantee delivery. Unacknowledged packets are retried until a max retry count is reached. **Unreliable channels** send packets without acknowledgement.

## 5. Special IDs

| Constant | Value | Meaning |
|---|---|---|
| `ID_INVALID` | `0` | Initial ID before server assigns one |
| `ID_SERVER` | `1` | The server |
| `ID_BROADCAST` | `-1` | All connected clients |

## 6. Connection Lifecycle

```
Client                                  Server
  |                                        |
  |-- SYS_CONNECT (senderId=0, username) ->|
  |                                        |  validates, assigns ID and username
  |<-- SYS_ACK ----------------------------|
  |<-- SYS_CONNECTED (id:username) --------|
  |                                        |
  |       [connection established]         |
  |                                        |
  |-- SYS_PING ------------------------- ->|
  |<-- SYS_PONG ---------------------------|
  |                                        |
  |-- SYS_DISCONNECT -------------------- >|
  |                                        |
```

`SYS_CONNECT` payload format: `<username>`

`SYS_CONNECTED` payload format: `<assignedId>:<finalUsername>`

If the requested username is already taken, the server appends a numeric suffix (e.g. `Anton_001`).

## 7. Reliable Delivery

The `ChannelManager` handles reliable delivery for `CH_SYSTEM` and `CH_CHAT` channels:

1. On send, the packet is stored in a `pendingAck` map
2. A `tick()` runs every 100ms — if a packet has not been acknowledged within `TIMEOUT_MS`, it is resent
3. After `MAX_RETRIES` failed attempts, the packet is dropped
4. On receipt, the receiver sends back `SYS_ACK` with the same sequence number
5. Duplicate packets are discarded

## 8. Error Handling

- Messages with an unrecognized type or channel are discarded
- Messages with fewer than 5 fields are discarded
- The server removes a client after it stops sending pings for longer than `TIMEOUT_PING_MS` (30s)