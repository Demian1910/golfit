package org.golfit.client.game;

import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

public class GameView {
    private Pane root;

    public GameView() {
        this.root = new Pane();

        this.root.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
    }

    /**
     * return panel to insert in scene JavaFX.
     */
    public Pane getRoot() {
        return root;
    }
}