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

        String[] lines = csv.split("\n");
        int rank = 1;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",");
            // cols[3] = "username:score"
            String userScore = cols.length >= 4 ? cols[3] : line;
            String[] parts   = userScore.split(":");
            String username  = parts.length >= 1 ? parts[0] : "?";
            String score     = parts.length >= 2 ? parts[1] : "?";

            Label row = new Label(String.format("  %d.  %-16s %s strokes", rank++, username, score));
            row.setFont(Font.font("Courier New", FontWeight.NORMAL, 16));
            row.setTextFill(Color.web(rank == 2 ? "#fff176" : "#e8f5e9")); // gold for #1
            scoreBoard.getChildren().add(row);
        }
    }
}