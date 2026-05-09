package org.golfit.client.gui.settings;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.eventBus.EventBus;
import org.golfit.shared.constants.Constants;

/** Settings popup window: change username, sound toggle, ball color picker. */
public class SettingsMenu {

    private final ClientApi clientApi;

    public SettingsMenu(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    /**
     * Opens the settings modal window.
     *
     * @param owner the parent window used to centre and block the modal; may be {@code null}
     */
    public void open(Window owner) {
        Stage popup = new Stage();
        popup.setTitle("Settings");
        popup.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) popup.initOwner(owner);
        popup.setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2c3e50;");

        // ── Change Username ──────────────────────────────────────────
        root.getChildren().add(header("Change Username"));

        TextField usernameField = new TextField(clientApi.getUsername());
        usernameField.setPromptText("New username");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        Label usernameStatus = statusLabel("");

        Button applyUsername = new Button("Apply");
        applyUsername.setMaxWidth(Double.MAX_VALUE);
        applyUsername.setOnAction(e -> {
            String newName = usernameField.getText().trim();
            if (newName.isEmpty()) {
                usernameStatus.setText("Username cannot be empty.");
                return;
            }
            try {
                clientApi.connect(newName);
                EventBus.get().emit(Constants.USERNAME_CHANGED, newName);
                usernameStatus.setText("Username changed to: " + newName);
            } catch (Exception ex) {
                usernameStatus.setText("Error: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(usernameField, applyUsername, usernameStatus, divider());

        // ── Sound Toggle ─────────────────────────────────────────────
        root.getChildren().add(header("Sound"));

        ToggleButton soundToggle = new ToggleButton(
                ClientPreferences.isSoundEnabled() ? "Sound: ON" : "Sound: OFF");
        soundToggle.setSelected(ClientPreferences.isSoundEnabled());
        soundToggle.setMaxWidth(Double.MAX_VALUE);
        soundToggle.setOnAction(e -> {
            boolean enabled = soundToggle.isSelected();
            ClientPreferences.setSoundEnabled(enabled);
            soundToggle.setText(enabled ? "Sound: ON" : "Sound: OFF");
            EventBus.get().emit(Constants.SOUND_TOGGLE, enabled);
        });

        root.getChildren().addAll(soundToggle, divider());

        // ── Ball Color ───────────────────────────────────────────────
        root.getChildren().add(header("Ball Color"));

        Color initialColor = ClientPreferences.getPreferredBallColor() != null
                ? ClientPreferences.getPreferredBallColor()
                : Color.WHITE;

        ColorPicker colorPicker = new ColorPicker(initialColor);
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        Label colorStatus = statusLabel("");

        Button applyColor = new Button("Apply Color");
        applyColor.setMaxWidth(Double.MAX_VALUE);
        applyColor.setOnAction(e -> {
            Color chosen = colorPicker.getValue();
            ClientPreferences.setPreferredBallColor(chosen);
            EventBus.get().emit(Constants.BALL_COLOR_CHANGED, chosen);
            colorStatus.setText("Color applied.");
        });

        root.getChildren().addAll(colorPicker, applyColor, colorStatus, divider());

        // ── Close ────────────────────────────────────────────────────
        Button closeButton = new Button("Close");
        closeButton.setMaxWidth(Double.MAX_VALUE);
        closeButton.setOnAction(e -> popup.close());
        root.getChildren().add(closeButton);

        popup.setScene(new Scene(root, 280, 420));
        popup.show();
    }

    private Label header(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        return label;
    }

    private Label statusLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #aabbcc; -fx-font-size: 11px;");
        return label;
    }

    private Separator divider() {
        return new Separator();
    }
}
