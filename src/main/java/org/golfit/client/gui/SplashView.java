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

import java.util.ArrayList;
import java.util.List;

public class SplashView extends StackPane {

    private final VBox scoreBoard = new VBox(6);

    // Single source of truth — last payload received from server
    private static String cachedScorePayload = null;

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

        // Populate immediately from cache — no race condition possible
        if (cachedScorePayload != null) {
            populateBoard(cachedScorePayload);
        }

        // Update cache and board whenever server sends scores
        EventBus.get().on(Constants.SCORE_UPDATE, payload -> {
            String p = ((String) payload).trim();
            cachedScorePayload = p;  // always update cache, even if this view isn't visible yet
            Platform.runLater(() -> {
                scoreBoard.getChildren().clear();
                populateBoard(p);
            });
        });
    }

    private void populateBoard(String payload) {
        scoreBoard.getChildren().clear();
        for (String line : payload.split("\n")) {
            if (!line.isBlank()) appendScore(line.trim());
        }
    }

    private void appendScore(String payload) {
        String[] parts = payload.trim().split(":");
        if (parts.length < 2) return;
        String username = parts[0].trim();
        String strokes  = parts[1].trim();
        int rank = scoreBoard.getChildren().size() + 1;
        String text = String.format("  %d.  %-16s %s strokes", rank, username, strokes);
        Label row = new Label(text);
        row.setFont(Font.font("Courier New", FontWeight.NORMAL, 16));
        row.setTextFill(rank == 1 ? Color.web("#fff176") : Color.web("#e8f5e9"));
        scoreBoard.getChildren().add(row);
    }
}