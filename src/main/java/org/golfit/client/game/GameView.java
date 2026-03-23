package org.golfit.client.game;

import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

public class GameView {
    private Pane root;
    private double startX;
    private double startY;

    public GameView() {
        this.root = new Pane();

        this.root.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                CornerRadii.EMPTY,
                Insets.EMPTY)));
                
        setupMouseControls();
    }

    private void setupMouseControls() {
        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            startX = e.getX(); startY = e.getY();
        });
        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            try {
                if (org.golfit.Main.currentClient != null) {
                    org.golfit.Main.currentClient.clientApi.sendMovement((startX - e.getX()) * 5, (startY - e.getY()) * 5);
                }
            } catch (Exception ex) {}
        });
    }

    /**
     * return panel to insert in scene JavaFX.
     */
    public Pane getRoot() {
        return root;
    }
}