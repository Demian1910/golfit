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

        this.root.setBackground(Background.EMPTY);
                
        setupMouseControls();
    }

    private void setupMouseControls() {
        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            startX = e.getX(); startY = e.getY();
        });
        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            try {
                if (org.golfit.Main.currentClient != null) {
                    double fx = (startX - e.getX()) * 4;
                    double fy = (startY - e.getY()) * 4;
                    org.golfit.Main.currentClient.clientApi.sendMovement(fx, fy);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * return panel to insert in scene JavaFX.
     */
    public Pane getRoot() {
        return root;
    }
}