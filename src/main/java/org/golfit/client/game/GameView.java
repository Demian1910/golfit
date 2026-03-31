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
            startX = e.getX();
            startY = e.getY();
        });

        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            try {
                if (org.golfit.Main.currentClient != null) {
                    // 1. Differenz berechnen
                    double diffX = startX - e.getX();
                    double diffY = startY - e.getY();

                    // 2. Umrechnen von Pixel in Grid-Einheiten
                    // Wir teilen durch die aktuelle Breite/Höhe der Pane
                    double scaleX = root.getWidth() / 80.0;
                    double scaleY = root.getHeight() / 45.0;

                    double gridFx = diffX / scaleX;
                    double gridFy = diffY / scaleY;

                    // 3. Kraft-Multiplikator für das Gefühl (Tweak hier!)
                    // Ein Wert von 1.5 bis 2.5 fühlt sich meist gut an
                    double powerMultiplier = 2.0;
                    gridFx *= powerMultiplier;
                    gridFy *= powerMultiplier;

                    // 4. Maximales Limit (Damit der Ball nicht aus der Map glitcht)
                    double maxForce = 45.0;
                    double currentForce = Math.sqrt(gridFx * gridFx + gridFy * gridFy);

                    if (currentForce > maxForce) {
                        gridFx = (gridFx / currentForce) * maxForce;
                        gridFy = (gridFy / currentForce) * maxForce;
                    }

                    // Nur senden, wenn es kein versehentlicher Klick war
                    if (currentForce > 0.5) {
                        org.golfit.Main.currentClient.clientApi.sendMovement(gridFx, gridFy);
                    }
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