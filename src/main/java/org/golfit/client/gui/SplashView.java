package org.golfit.client.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import org.golfit.client.eventBus.EventBus;
import org.golfit.shared.constants.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SplashView extends StackPane {

    private final VBox scoreBoard = new VBox(8);

    // Single source of truth — last payload received from server
    private static String cachedScorePayload = null;

    private static final DateTimeFormatter CSV_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("d MMM yyyy  HH:mm");

    public SplashView() {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#1b5e20")),
                new Stop(0.5, Color.web("#2e7d32")),
                new Stop(1, Color.web("#1b5e20"))
        };
        LinearGradient grassGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        this.setBackground(new Background(new BackgroundFill(grassGradient, null, null)));

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        Text title = new Text("GolfIt");
        title.setFont(Font.font("Apple Chancery", FontWeight.BOLD, 100));
        title.setFill(Color.WHITE);
        DropShadow titleShadow = new DropShadow();
        titleShadow.setRadius(20.0);
        titleShadow.setOffsetX(5.0);
        titleShadow.setOffsetY(5.0);
        titleShadow.setColor(Color.color(0, 0, 0, 0.4));
        title.setEffect(titleShadow);

        Label subtitle = new Label("by Sultans of Swing");
        subtitle.setFont(Font.font("American Typewriter", FontWeight.LIGHT, 24));
        subtitle.setTextFill(Color.web("#a5d6a7"));
        subtitle.setStyle("-fx-font-style: italic;");
        DropShadow subShadow = new DropShadow();
        subShadow.setRadius(5.0);
        subShadow.setColor(Color.color(0, 0, 0, 0.3));
        subtitle.setEffect(subShadow);

        Label scoresTitle = new Label("🏆 High Scores");
        scoresTitle.setFont(Font.font("American Typewriter", FontWeight.BOLD, 20));
        scoresTitle.setTextFill(Color.web("#fff9c4"));

        // The score board itself (rows added dynamically)
        scoreBoard.setAlignment(Pos.CENTER_LEFT);
        scoreBoard.setPadding(new Insets(14, 24, 14, 24));
        scoreBoard.setStyle("""
                -fx-background-color: rgba(0,0,0,0.25);
                -fx-background-radius: 12;
                """);

        // Wrap in a ScrollPane so it handles many entries gracefully
        ScrollPane scrollPane = new ScrollPane(scoreBoard);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(220);
        scrollPane.maxWidthProperty().bind(this.widthProperty().multiply(0.65));
        scrollPane.prefWidthProperty().bind(this.widthProperty().multiply(0.65));
        scrollPane.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                """);
        // Hide the horizontal scrollbar; vertical scrollbar shows on demand
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        content.getChildren().addAll(title, subtitle, scoresTitle, scrollPane);

        StackPane overlay = new StackPane(content);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.15);");
        getChildren().add(overlay);

        // Populate immediately from cache — no race condition possible
        if (cachedScorePayload != null) {
            populateBoard(cachedScorePayload);
        }

        // Update cache and board whenever server sends scores
        EventBus.get().on(Constants.SCORE_UPDATE, payload -> {
            String p = ((String) payload).trim();
            cachedScorePayload = p;
            Platform.runLater(() -> populateBoard(p));
        });
    }

    private void populateBoard(String payload) {
        scoreBoard.getChildren().clear();
        for (String line : payload.split("\n")) {
            if (!line.isBlank())
                appendScore(line.trim());
        }
    }

    /**
     * Parses one score line: {@code timestamp~winner:strokes|player2:strokes|...}
     * <p>
     * Renders the winner on the left (bold, larger) and all other players to the
     * right in a smaller italic font. The timestamp is shown dimly below the row.
     */
    private void appendScore(String payload) {
        // Split timestamp from the rest
        String timestamp = "";
        String scoresPart = payload;
        int tilde = payload.indexOf('~');
        if (tilde >= 0) {
            timestamp = payload.substring(0, tilde).trim();
            scoresPart = payload.substring(tilde + 1).trim();
        }

        String[] entries = scoresPart.split("\\|");
        if (entries.length == 0)
            return;

        // First entry is always the winner
        String[] winnerParts = entries[0].trim().split(":");
        if (winnerParts.length < 2)
            return;
        String winnerName = winnerParts[0].trim();
        String winnerStrokes = winnerParts[1].trim();

        int rank = scoreBoard.getChildren().size() + 1;

        // --- Winner label (left) ---
        Label winnerLabel = new Label(
                String.format("%d.  %s — %s strokes", rank, winnerName, winnerStrokes));
        winnerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        winnerLabel.setTextFill(rank == 1 ? Color.web("#fff176") : Color.web("#e8f5e9"));
        winnerLabel.setMinWidth(230);

        HBox playerRow = new HBox(12);
        playerRow.setAlignment(Pos.CENTER_LEFT);
        playerRow.getChildren().add(winnerLabel);

        // --- Other players (right, smaller italic) ---
        for (int i = 1; i < entries.length; i++) {
            String[] parts = entries[i].trim().split(":");
            if (parts.length < 2)
                continue;
            String name = parts[0].trim();
            String strokes = parts[1].trim();
            Label other = new Label(name + ": " + strokes);
            other.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
            other.setTextFill(Color.web("#80cbc4"));
            other.setStyle("-fx-font-style: italic;");
            playerRow.getChildren().add(other);
        }

        // --- Timestamp (below the player row, dimmed) ---
        Label timeLabel = new Label(formatTimestamp(timestamp));
        timeLabel.setFont(Font.font("Courier New", FontWeight.NORMAL, 11));
        timeLabel.setTextFill(Color.web("#a5d6a7", 0.65));

        VBox entry = new VBox(2, playerRow, timeLabel);
        entry.setAlignment(Pos.CENTER_LEFT);

        // Subtle separator line except before the first entry
        if (rank > 1) {
            Region sep = new Region();
            sep.setPrefHeight(1);
            sep.setStyle("-fx-background-color: rgba(255,255,255,0.10);");
            VBox.setMargin(sep, new Insets(0, 0, 4, 0));
            scoreBoard.getChildren().add(sep);
        }

        scoreBoard.getChildren().add(entry);
    }

    /**
     * Converts {@code "yyyy-MM-dd HH:mm:ss"} into a friendly string like
     * {@code "7 May 2026  15:56"}.
     */
    private String formatTimestamp(String raw) {
        if (raw == null || raw.isBlank())
            return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(raw, CSV_FMT);
            return dt.format(DISPLAY_FMT);
        } catch (DateTimeParseException e) {
            return raw; // fall back to raw string if format doesn't match
        }
    }
}