package org.golfit.client.gui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.golfit.client.clientLogic.ClientApi;

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

    public Sidebar(ClientApi clientApi) {
        this.clientApi = clientApi;

        setPrefWidth(250);
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #2c3e50;");

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
        VBox.setVgrow(chatMessages, Priority.ALWAYS);

        // Input area
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setOnAction(e -> sendMessage());

        Button sendButton = new Button("Send");
        sendButton.setMaxWidth(Double.MAX_VALUE);
        sendButton.setOnAction(e -> sendMessage());

        getChildren().addAll(title, chatMode, targetLabel, targetField, chatMessages, messageInput, sendButton);
    }

    private void onModeChanged() {
        boolean isPrivate = "Private".equals(chatMode.getValue());
        targetLabel.setVisible(isPrivate);
        targetLabel.setManaged(isPrivate);
        targetField.setVisible(isPrivate);
        targetField.setManaged(isPrivate);
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

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
}
