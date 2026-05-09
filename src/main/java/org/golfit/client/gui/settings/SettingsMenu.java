package org.golfit.client.gui.settings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.golfit.client.clientLogic.ClientApi;
import org.golfit.client.eventBus.EventBus;
import org.golfit.shared.constants.Constants;

/** Settings popup window: change username, sound toggle, ball color picker. */
public class SettingsMenu {

    /** Must stay in sync with {@code MovementHandler.PLAYER_COLORS} and {@code LobbyMenu.PLAYER_EMOJIS}. */
    public static final Color[]  PRESET_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.GOLD, Color.PURPLE, Color.ORANGE
    };
    public static final String[] PRESET_EMOJIS = {"🔴", "🔵", "🟢", "🟡", "🟣", "🟠"};

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

        // ── Sound ────────────────────────────────────────────────────
        root.getChildren().add(header("Sound"));

        ToggleButton soundToggle = new ToggleButton(
                ClientPreferences.isSoundEnabled() ? "Sound: ON" : "Sound: OFF");
        soundToggle.setSelected(ClientPreferences.isSoundEnabled());
        soundToggle.setMaxWidth(Double.MAX_VALUE);

        Label volumeLabel = new Label("Volume");
        volumeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Slider volumeSlider = new Slider(0, 100, ClientPreferences.getVolume() * 100);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setMaxWidth(Double.MAX_VALUE);
        volumeSlider.setDisable(!ClientPreferences.isSoundEnabled());
        volumeSlider.setLabelFormatter(new StringConverter<>() {
            @Override public String toString(Double v) {
                return v.intValue() == 0 ? "0" : v.intValue() == 100 ? "100" : "";
            }
            @Override public Double fromString(String s) { return Double.parseDouble(s); }
        });
        volumeSlider.valueProperty().addListener((obs, oldV, newV) ->
                ClientPreferences.setVolume(newV.floatValue() / 100f));

        soundToggle.setOnAction(e -> {
            boolean enabled = soundToggle.isSelected();
            ClientPreferences.setSoundEnabled(enabled);
            soundToggle.setText(enabled ? "Sound: ON" : "Sound: OFF");
            volumeSlider.setDisable(!enabled);
            EventBus.get().emit(Constants.SOUND_TOGGLE, enabled);
        });

        root.getChildren().addAll(soundToggle, volumeLabel, volumeSlider, divider());

        // ── Ball Color ───────────────────────────────────────────────
        root.getChildren().add(header("Ball Color"));

        ToggleGroup colorGroup = new ToggleGroup();
        HBox swatchRow = new HBox(6);
        swatchRow.setAlignment(Pos.CENTER_LEFT);

        Color current = ClientPreferences.getPreferredBallColor();
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            final Color c = PRESET_COLORS[i];
            ToggleButton swatch = new ToggleButton(PRESET_EMOJIS[i]);
            swatch.setToggleGroup(colorGroup);
            swatch.setPrefSize(38, 32);
            swatch.setSelected(c.equals(current));
            swatch.setOnAction(e -> {
                ClientPreferences.setPreferredBallColor(c);
                EventBus.get().emit(Constants.BALL_COLOR_CHANGED, c);
                try { clientApi.sendColorPreference(c); } catch (Exception ignored) {}
            });
            swatchRow.getChildren().add(swatch);
        }

        Button resetColor = new Button("Default");
        resetColor.setOnAction(e -> {
            colorGroup.selectToggle(null);
            ClientPreferences.setPreferredBallColor(null);
            EventBus.get().emit(Constants.BALL_COLOR_CHANGED, null);
            try { clientApi.sendColorPreference(null); } catch (Exception ignored) {}
        });

        root.getChildren().addAll(swatchRow, resetColor, divider());

        // ── Close ────────────────────────────────────────────────────
        Button closeButton = new Button("Close");
        closeButton.setMaxWidth(Double.MAX_VALUE);
        closeButton.setOnAction(e -> popup.close());
        root.getChildren().add(closeButton);

        popup.setScene(new Scene(root, 280, 460));
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
