package org.golfit.client.cli;

import org.golfit.client.clientLogic.ClientApi;

import java.util.Arrays;

/** Processes user input from the CLI and dispatches slash commands to the client API. */
public class ClientCli {
    private final ClientApi clientApi;



    /**
     * Creates a new CLI processor.
     *
     * @param clientApi the client API for executing commands
     */
    public ClientCli(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /** Parses and executes a slash command entered by the user. */
    public void processCommand(String command) throws Exception {
        if(command.isEmpty()) return;
        if(command.charAt(0) == '/') {
            try{
                String[] args = command.split(" ");
                String cleanCommand = args[0].substring(1);
                switch (Commands.valueOf(cleanCommand)) {
                    case help -> {
                        System.out.println("Available commands:");
                        System.out.println("/help - Displays this message");
                        System.out.println("/whoami - Displays your username");
                        System.out.println("/changeUsername <username> - Changes your username");
                        System.out.println("/lobbyList - Lists available lobbies");
                        System.out.println("/createLobby - Creates a lobby");
                        System.out.println("/joinLobby <lobbyName> - Joins a lobby");
                        System.out.println("/leaveLobby - Leaves the current lobby");
                        System.out.println("/chat <message> - Sends a chat message");
                        System.out.println("/lobby <message> - Sends lobby message");
                        System.out.println("/privateChat <username> <message> - Sends a private chat message");
                        System.out.println("/movement <direction> - Moves the player in the specified direction");
                        System.out.println("/q - Quits the client");
                    }
                    case whoami -> {
                        System.out.println("Your username is: " + clientApi.getUsername());
                    }
                    case changeUsername -> {
                        System.out.println("Changing username...");
                        String reconnectUsername = args.length > 1 ? args[1] : clientApi.getUsername();
                        clientApi.connect(reconnectUsername);
                    }
                    case lobbyList -> {
                        System.out.println("Listing lobbies...");
                        clientApi.lobbyList();
                    }
                    case createLobby -> {
                        System.out.println("Creating lobby...");
                        clientApi.createLobby();
                    }
                    case joinLobby -> {
                        if(args.length > 1) {
                            System.out.println("Joining lobby...");
                            String lobbyName = args[1];
                            clientApi.joinLobby(lobbyName);
                        }
                        else {
                            System.out.println("Usage: /joinLobby <lobbyName>");
                        }
                    }
                    case leaveLobby -> {
                        System.out.println("Leaving lobby...");
                        clientApi.leaveLobby(String.valueOf(clientApi.getLobbyId()));
                    }
                    case chat -> {
                        String msg = command.substring(command.indexOf(' ') + 1);
                        clientApi.sendChatBroadcast(msg);
                    }
                    case whisper -> {
                        if (args.length < 3) {
                            System.out.println("Usage: /privateChat <username> <message>");
                            break;
                        }
                        String targetUsername = args[1];
                        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        String data = targetUsername + ":" + message;

                        clientApi.sendPrivateChat(data);
                    }
                    case lobby -> {
                        String msg = command.substring(command.indexOf(' ') + 1);
                        clientApi.sendLobbyChat(msg);
                    }
                    case movement -> {
                        if (args.length >= 2) {
                            String[] parts = args[1].split(",");
                            if (parts.length == 2) {
                                double fx = Double.parseDouble(parts[0]);
                                double fy = Double.parseDouble(parts[1]);
                                clientApi.sendMovement(fx, fy);
                            } else {
                                System.out.println("Usage: /movement <forceX>,<forceY>");
                            }
                        } else {
                            System.out.println("Usage: /movement <forceX>,<forceY>");
                        }
                    }
                    case q -> {
                        System.out.println("Shutting down...");
                        clientApi.shutdown();
                    }
                    default -> System.out.println("Unknown command");
                }
            }
            catch(IllegalArgumentException e) {
                System.out.println("Invalid command format: " + command);
            }
        }
    }
}

/** Available CLI slash commands. */
enum Commands {
    help,
    whoami,
    changeUsername,
    rec,
    lobbyList,
    createLobby,
    joinLobby,
    leaveLobby,
    chat,
    whisper,
    lobby,
    movement,
    q
}
