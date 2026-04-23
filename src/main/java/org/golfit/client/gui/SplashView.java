package org.golfit.client.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SplashView extends StackPane {

    private final VBox scoreBoard = new VBox(6);

    public SplashView() {
        Stop[] stops = new Stop[]{
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

        // --- Scoreboard ---
        Label scoresTitle = new Label("🏆 High Scores");
        scoresTitle.setFont(Font.font("American Typewriter", FontWeight.BOLD, 20));
        scoresTitle.setTextFill(Color.web("#fff9c4"));

        scoreBoard.setAlignment(Pos.CENTER);
        scoreBoard.setStyle("""
                -fx-background-color: rgba(0,0,0,0.25);
                -fx-background-radius: 12;
                -fx-padding: 14 24 14 24;
                """);

        content.getChildren().addAll(title, subtitle, scoresTitle, scoreBoard);

        StackPane overlay = new StackPane(content);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.15);");
        getChildren().add(overlay);

        // Listen for score updates from the server
        EventBus.get().on(Constants.SCORE_UPDATE, payload -> {
            Platform.runLater(() -> populateScores((String) payload));
        });
    }

    /**
     * Parses CSV lines and renders one row per entry.
     * Expected line format: timestamp,id,username,user:score
     * e.g. 2026-04-23 10:33:50,1618818195,tosha,tosha:13
     */
    private void populateScores(String csv) {
        scoreBoard.getChildren().clear();

        // winner → best (lowest) score across all games
        Map<String, Integer> bestScores = new LinkedHashMap<>();

        for (String line : csv.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",", 4);
            if (cols.length < 4) continue;

            String winner = cols[2].trim();

            // Find the winner's score in the players field (pipe-separated)
            for (String entry : cols[3].split("\\|")) {
                String[] parts = entry.split(":");
                if (parts.length < 2) continue;
                if (!parts[0].trim().equals(winner)) continue;
                try {
                    int score = Integer.parseInt(parts[1].trim());
                    if (score > 0) {
                        bestScores.merge(winner, score, Math::min);
                    }
                } catch (NumberFormatException ignored) {}
                break;
            }
        }

        // Sort ascending (fewest strokes = best)
        List<Map.Entry<String, Integer>> sorted = bestScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            String text = String.format("  %d.  %-16s %s strokes", rank, entry.getKey(), entry.getValue());
            Label row = new Label(text);
            row.setFont(Font.font("Courier New", FontWeight.NORMAL, 16));
            row.setTextFill(rank == 1 ? Color.web("#fff176") : Color.web("#e8f5e9"));
            scoreBoard.getChildren().add(row);
            rank++;
        }
    }
}