package org.golfit.client.cli;

import org.golfit.client.ClientApi;

public class ClientCli {
    private final ClientApi clientApi;



    public ClientCli(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public void processCommand(String command) throws Exception {
        if(command.charAt(0) == '/') {
            String[] args = command.split(" ");
            String cleanCommand = args[0].substring(1);
            try{
                switch (Commands.valueOf(cleanCommand)) {
                    case help -> {
                        System.out.println("Available commands:");
                        System.out.println("/help - Displays this message");
                        System.out.println("/whoami - Displays your username");
                        System.out.println("/changeUsername <username> - Changes your username");
                        System.out.println("/createLobby - Creates a lobby");
                        System.out.println("/joinLobby <lobbyName> - Joins a lobby");
                        System.out.println("/leaveLobby - Leaves the current lobby");
                        System.out.println("/chat <message> - Sends a chat message");
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
//                        clientApi.resetChannels();
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
                        System.out.println("Joining lobby...");
                        clientApi.joinLobby();
                    }
                    case leaveLobby -> {
                        System.out.println("Leaving lobby...");
                        clientApi.leaveLobby();
                    }
                    case chat -> {
                        String msg = command.substring(command.indexOf(' ') + 1);
                        clientApi.sendChat(msg);
                    }
                    case privateChat -> {
                        if (args.length < 3) {
                            System.out.println("Usage: /privateChat <username> <message>");
                            break;
                        }
                        String data = command.substring("/privateChat ".length());
                        clientApi.sendPrivateChat(data);
                    }
                    case movement -> {
                        clientApi.sendMovement(args[1]);
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
    privateChat,
    movement,
    q
}
