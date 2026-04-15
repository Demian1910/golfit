package org.golfit.client.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.stage.Modality;
import javafx.stage.Stage;
import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.eventBus.EventBus;
import org.golfit.client.gui.lobby.LobbyMenu;
import org.golfit.shared.constants.Constants;

/**
 * Chat sidebar with three modes: broadcast, private, and lobby chat.
 */
public class Sidebar extends VBox {

    private final ClientApi clientApi;
    private final ListView<String> chatMessages;
    private final TextField messageInput;
    private final ComboBox<String> chatMode;
    private final TextField targetField;
    private final Label targetLabel;
    private final Label usernameLabel;
    private final LobbyMenu lobbyMenu;

    public Sidebar(ClientApi clientApi) {
        this.clientApi = clientApi;

        lobbyMenu = new LobbyMenu(clientApi);
        lobbyMenu.setPrefHeight(200);
        lobbyMenu.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(lobbyMenu, Priority.ALWAYS);

        EventBus.get().on(Constants.BROADCAST_MESSAGE, data -> addMessage ((String[]) data, Constants.BROADCAST_MESSAGE));
        EventBus.get().on(Constants.PRIVATE_MESSAGE, data -> addMessage((String[]) data, Constants.PRIVATE_MESSAGE));
        EventBus.get().on(Constants.LOBBY_MESSAGE, data -> addMessage((String[]) data, Constants.LOBBY_MESSAGE));
        EventBus.get().on(Constants.PLAYERS_LIST, data -> {
            Platform.runLater(() -> {
                openPlayerWindow((String[]) data);
            });
        });

        Button playerButton = new Button("Players");
        playerButton.setPrefHeight(25);
        playerButton.setMaxHeight(Double.MAX_VALUE);
        playerButton.setOnAction(event -> {
            try{
                clientApi.requestPlayersList();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });

        setPrefWidth(250);
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #2c3e50;");

        // Whoami — username display
        usernameLabel = new Label(clientApi.getUsername());
        usernameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        // Title
        Label title = new Label("Chat");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Chat mode selector
        chatMode = new ComboBox<>();
        chatMode.getItems().addAll("Broadcast", "Private", "Lobby");
        chatMode.setValue("Broadcast");
        chatMode.setMaxWidth(Double.MAX_VALUE);
        chatMode.setOnAction(e -> onModeChanged());

        // Target field (only visible for private chat)
        targetLabel = new Label("To:");
        targetLabel.setStyle("-fx-text-fill: white;");
        targetField = new TextField();
        targetField.setPromptText("Username");
        targetLabel.setVisible(false);
        targetLabel.setManaged(false);
        targetField.setVisible(false);
        targetField.setManaged(false);

        // Message display
        chatMessages = new ListView<>();
        chatMessages.setPrefHeight(200);
        chatMessages.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(chatMessages, Priority.ALWAYS);

        // Input area
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setOnAction(e -> sendMessage());

        Button sendButton = new Button("Send");
        sendButton.setMaxWidth(Double.MAX_VALUE);
        sendButton.setOnAction(e -> sendMessage());

        getChildren().addAll(playerButton,usernameLabel, title, chatMode, targetLabel, targetField, chatMessages, messageInput, sendButton, lobbyMenu);
    }

    /** Toggles the target username field visibility based on the selected chat mode. */
    private void onModeChanged() {
        boolean isPrivate = "Private".equals(chatMode.getValue());
        targetLabel.setVisible(isPrivate);
        targetLabel.setManaged(isPrivate);
        targetField.setVisible(isPrivate);
        targetField.setManaged(isPrivate);
    }

    /** Sends the current message input via the selected chat mode (broadcast, private, or lobby). */
    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        if (text.startsWith("/")) {
            handleCommand(text);
            messageInput.clear();
            return;
        }

        try {
            switch (chatMode.getValue()) {
                case "Broadcast" -> {
                    clientApi.sendChatBroadcast(text);
                }
                case "Private" -> {
                    String target = targetField.getText().trim();
                    if (target.isEmpty()) {
                        addMessage("[System] Enter a target username first.");
                        return;
                    }
                    clientApi.sendPrivateChat(target + ":" + text);
                }
                case "Lobby" -> {
                    if (clientApi.getLobbyId() <= 0) {
                        addMessage("[System] You are not in a lobby.");
                        return;
                    }
                    clientApi.sendLobbyChat(text);
                }
            }
            messageInput.clear();
        } catch (Exception ex) {
            addMessage("[Error] " + ex.getMessage());
        }
    }

    /** Processes a slash command entered in the chat input field. */
    private void handleCommand(String input) {
        String[] args = input.split(" ");
        String cmd = args[0].substring(1).toLowerCase();

        try {
            switch (cmd) {
                case "help" -> {
                    addMessage("[System] Available commands:");
                    addMessage("  /help — Show this list");
                    addMessage("  /whoami — Show your username");
                    addMessage("  /changeUsername <name> — Change your username");
                    addMessage("  /chat <msg> — Send broadcast message");
                    addMessage("  /whisper <user> <msg> — Send private message");
                    addMessage("  /lobby <msg> — Send lobby message");
                    addMessage("  /lobbyList — Refresh lobby list");
                    addMessage("  /createLobby — Create a new lobby");
                    addMessage("  /joinLobby <id> — Join a lobby");
                    addMessage("  /leaveLobby — Leave current lobby");
                    addMessage("  /q — Disconnect");
                }
                case "whoami" -> {
                    addMessage("[System] You are: " + clientApi.getUsername());
                }
                case "changeusername" -> {
                    if (args.length < 2) {
                        addMessage("[System] Usage: /changeUsername <name>");
                        return;
                    }
                    clientApi.connect(args[1]);
                    usernameLabel.setText(args[1]);
                    addMessage("[System] Username changed to: " + args[1]);
                }
                case "chat" -> {
                    if (args.length < 2) {
                        addMessage("[System] Usage: /chat <message>");
                        return;
                    }
                    String msg = input.substring(input.indexOf(' ') + 1);
                    clientApi.sendChatBroadcast(msg);
                }
                case "whisper" -> {
                    if (args.length < 3) {
                        addMessage("[System] Usage: /whisper <username> <message>");
                        return;
                    }
                    String target = args[1];
                    String msg = input.substring(input.indexOf(' ', input.indexOf(' ') + 1) + 1);
                    clientApi.sendPrivateChat(target + ":" + msg);
                }
                case "lobby" -> {
                    if (clientApi.getLobbyId() <= 0) {
                        addMessage("[System] You are not in a lobby.");
                        return;
                    }
                    if (args.length < 2) {
                        addMessage("[System] Usage: /lobby <message>");
                        return;
                    }
                    String msg = input.substring(input.indexOf(' ') + 1);
                    clientApi.sendLobbyChat(msg);
                }
                case "lobbylist" -> {
                    clientApi.lobbyList();
                    addMessage("[System] Refreshing lobby list...");
                }
                case "createlobby" -> {
                    if (clientApi.getLobbyId() > 0) {
                        addMessage("[System] You are already in a lobby. Leave first.");
                        return;
                    }
                    clientApi.createLobby();
                    addMessage("[System] Creating lobby...");
                }
                case "joinlobby" -> {
                    if (args.length < 2) {
                        addMessage("[System] Usage: /joinLobby <lobbyId>");
                        return;
                    }
                    if (clientApi.getLobbyId() > 0) {
                        addMessage("[System] You are already in a lobby. Leave first.");
                        return;
                    }
                    clientApi.joinLobby(args[1]);
                    addMessage("[System] Joining lobby...");
                }
                case "leavelobby" -> {
                    if (clientApi.getLobbyId() <= 0) {
                        addMessage("[System] You are not in a lobby.");
                        return;
                    }
                    clientApi.leaveLobby(String.valueOf(clientApi.getLobbyId()));
                    addMessage("[System] Leaving lobby...");
                }
                case "q" -> {
                    addMessage("[System] Disconnecting...");
                    clientApi.shutdown();
                    Platform.exit();
                }
                default -> addMessage("[System] Unknown command: /" + cmd + ". Type /help for a list of commands.");
            }
        } catch (Exception ex) {
            addMessage("[Error] " + ex.getMessage());
        }
    }

    /** Opens a modal popup window listing all connected players. */
    private void openPlayerWindow(String[] players) {
        Stage popup = new Stage();
        popup.setTitle("Connected players");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox content = new VBox();
        content.setPadding(new Insets(12));

        for(String player : players) {
            Label playerLabel = new Label(player);
            content.getChildren().add(playerLabel);
        }

        popup.setScene(new Scene(content, 200, 300));
        popup.show();
    }

    /**
     * Adds a message to the chat display. Thread-safe — can be called from any thread.
     */
    public void addMessage(String message) {
        if (javafx.application.Platform.isFxApplicationThread()) {
            chatMessages.getItems().add(message);
            chatMessages.scrollTo(chatMessages.getItems().size() - 1);
        } else {
            javafx.application.Platform.runLater(() -> {
                chatMessages.getItems().add(message);
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });
        }
    }



    /**
     * Adds a typed chat message to the display with the appropriate prefix.
     *
     * @param messageArray array of {@code [username, message]}
     * @param type         the message type constant (broadcast, private, or lobby)
     */
    public void addMessage(String[] messageArray, String type) {
        final String username = messageArray[0];
        final String message = messageArray[1];
        switch (type) {
            case Constants.BROADCAST_MESSAGE -> Platform.runLater(() -> {
                chatMessages.getItems().add("[Broadcast] " + username + ": " + message);
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });
            case Constants.PRIVATE_MESSAGE   -> Platform.runLater(() -> {
                chatMessages.getItems().add("[Whisper] " + username + ": " + message);
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });

            case Constants.LOBBY_MESSAGE     -> Platform.runLater(() -> {
                chatMessages.getItems().add("[Lobby] " + username + ": " + message);
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });

            default -> Platform.runLater(() -> {
                chatMessages.getItems().add(message);
                chatMessages.scrollTo(chatMessages.getItems().size() - 1);
            });
        }
        }
    }
