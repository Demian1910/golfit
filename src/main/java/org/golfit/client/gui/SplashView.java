package org.golfit.client.gui;

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

/**
 * Premium splash screen for the GolfIt application.
 */
public class SplashView extends StackPane {

    public SplashView() {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#1b5e20")), // Dark forest green
                new Stop(0.5, Color.web("#2e7d32")), // Bright grass green
                new Stop(1, Color.web("#1b5e20"))
        };
        LinearGradient grassGradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        this.setBackground(new Background(new BackgroundFill(grassGradient, null, null)));

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        Text title = new Text("GolfIt");
        title.setFont(Font.font("Apple Chancery", FontWeight.BOLD, 100));
        title.setFill(Color.WHITE);

        // DropShadow for a "pop" effect
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

        content.getChildren().addAll(title, subtitle);

        // emi-transparent overlay for a pro look
        StackPane overlay = new StackPane(content);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.15);");

        getChildren().add(overlay);
    }
}
