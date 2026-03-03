# The GolfIt Protocol

## 1. Overview and Purpose
The GolfIt Protocol enables two-way communication between a "dumb" client to a remote "authoritative" host. This document
defines set of rules and principles that govern the protocol. The protocol is exclusively designed for use with the
GolfIt Network. This is a buildup over UDP/IP. 

## 2. Architecture and Design
Protocol request consists of 2 logical parts: header and payload. Header contains information about the request: type, 
channel, senderId, sequence. Where payload contains the actual data stored as byte array. Total size of the request is 
determined by the sum of header and payload sizes. Header is 10 bytes long, payload size is variable, but no more than 
1024 bytes. Client and server using Packet builder respective Packer parser to build and parse requests. Text based 
communication is excluded from the protocol.

## 3. Header
Header is a fixed-size structure that contains information about the request. Header class is defined in 
`org.golfit.shared.packetParser` package. It has following fields:
```java
public static class Header {
    public byte type; // 1 byte
    public byte channel; // 1 byte
    public int senderId; // 4 bytes
    public int sequence; // 4 bytes
}
```
Format diagram:
```
0       1       2       3       4       5       6       7       8       9      10
+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+
| type  |  ch   |             senderId (32-bit) |    sequence (32-bit)          |
+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+
|           payload (0–1024 bytes)              |
+-------+-------+-------+-------+-------+-------+
``` 

### 3.1 Message Type
Message type is a single byte field that defines the type of the message. GolfIt protocol supports following message types:
System messages:

| Constant                 | Value |Direction|Channel|Description|
|--------------------------|-------|---------|-------|-----------|
| **PKG_SYS_CONNECT**      | 0x01  |Client to Server|CH_SYSTEM|Connection request|
| **PKG_SYS_DISCONNECT**   | 0x02  |Server to Client|CH_SYSTEM|Connection close|
| **PKG_SYS_PING**         | 0x03  |Client to Server|CH_SYSTEM|Ping request|
| **PKG_SYS_PONG**         | 0x04  |Server to Client|CH_SYSTEM|Ping response|
| **PKG_SYS_CREATE_LOBBY** | 0x05  |Client to Server|CH_SYSTEM|Create lobby request|
| **PKG_SYS_JOIN_LOBBY**   | 0x06  |Client to Server|CH_SYSTEM|Join lobby request|
| **PKG_SYS_LEAVE_LOBBY**  | 0x07  |Client to Server|CH_SYSTEM|Leave lobby request|
| **PKG_SYS_LOBBY_LIST**   | 0x08  |Server to Client|CH_SYSTEM|Lobby list|
| **PKG_SYS_CHAT**         | 0x09  |Client to Server|CH_CHAT|Chat message|
| **PKG_SYS_CHAT_PRIVATE** | 0x0A  |Client to Server|CH_CHAT|Private chat message|
| **PKG_MOVE_INPUT**       | 0x0B  |Client to Server|CH_MOVEMENT|Move input|
| **PKG_MOVE_UPDATE**      | 0x0C  |Server to Client|CH_MOVEMENT|Move update|
| **PKG_SYS_ACK**          | 0x0D  |Server to Client|CH_MOVEMENT|Move input acknowledgement|

Special ids:

 |Constant|Value|Meaning|
 |--------|-----|-------|
 |**ID_INVALID**|0|Invalid id|
 |**ID_SERVER**|1|Server id|
 |**ID_BROADCAST**|0xFFFFFFFF|Broadcast id|

### 3.2 Channel
Channel is a single byte field that defines the channel of the message. Is serves as filter for the messages that are received by the client.


### 3.3 Sender Id
Sender id is a 32-bit integer field that defines the id of the sender of the message. The sender sends ID_INVALID as its initial ID in PKG_SYS_CONNECT message,
before server assigns it id. 

### 3.4 Sequence
Sequence is a 32-bit integer field that defines the sequence number of the message. This number is incremented for each message sent by the client. Its used 
package ordering and acknowledgement.

### 3.5 Payload
Payload is a variable-length byte array that contains the actual data of the message. Payload can be empty for specific message formats.(e.g PKG_SYS_CONNECT)

## 4 Conection Lifecycle
 
```
Client                              Server
  |                                   |
  |-- PKG_SYS_CONNECT (senderId=0) -->|
  |                                   |  validates version, assigns client ID
  |<-- PKG_SYS_ACK (assigned_id) -----|
  |<-- PKG_SYS_LOBBY_LIST ------------|
  |                                   |
  |       [connection established]    |
```

## 5 Error Handling
- Package receiver that gets package with invalid type value must discard the package.
- Package receiver that gets package with invalid channel value must discard the package.
- Server may close connection if server continiously sends invalid or malformed packages.

## References
1. [Websocket Protocol] (https://datatracker.ietf.org/doc/html/rfc6455)
2. [Godot game engine] (https://github.com/godotengine/godot