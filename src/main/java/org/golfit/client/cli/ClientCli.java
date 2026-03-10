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
            switch (Commands.valueOf(cleanCommand)) {
                case help -> {
                    System.out.println("Available commands:");
                    System.out.println("/help - Displays this message");
                    System.out.println("/whoami - Displays your username");
                    System.out.println("/connect <username> - Connects to the server");
                    System.out.println("/rec - Reconnects to the server");
                    System.out.println("/createLobby <lobbyName> - Creates a lobby");
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
                case connect -> {
                    System.out.println("Connecting to the server...");
                    String connectUsername = args.length > 1 ? args[1] : clientApi.getUsername();
                    clientApi.connect(connectUsername);
                }
                case rec -> {
                    System.out.println("Reconnecting to the server...");
                    String reconnectUsername = args.length > 1 ? args[1] : clientApi.getUsername();
                    clientApi.resetChannels();
                    clientApi.connect(reconnectUsername);
                }
                case createLobby -> {
                    System.out.println("Creating lobby...");
                }
                case joinLobby -> {
                    System.out.println("Joining lobby...");
                }
                case leaveLobby -> {
                    System.out.println("Leaving lobby...");
                }
                case chat -> {
                    clientApi.sendChat(args[1]);
                }
                case privateChat -> {
                    clientApi.sendPrivateChat(args[1], Integer.parseInt(args[2]));
                }
                case movement -> {
                    System.out.println("Movement command not implemented yet");
                }
                case q -> {
                    System.out.println("Shutting down...");
                    clientApi.shutdown();
                }
                default -> System.out.println("Unknown command");
            }
        }
    }
}

enum Commands {
    help,
    whoami,
    connect,
    rec,
    createLobby,
    joinLobby,
    leaveLobby,
    chat,
    privateChat,
    movement,
    q
}
