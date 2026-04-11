package org.golfit.client.game;

import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

/**
 * Transparent overlay pane that captures mouse drag input for aiming and shooting.
 * Converts pixel drag distances to grid-unit force vectors and sends them to the server.
 */
public class GameView {
    private Pane root;
    private double startX;
    private double startY;

    /** Creates the game view pane and sets up mouse drag controls for aiming. */
    public GameView() {
        this.root = new Pane();

        this.root.setBackground(Background.EMPTY);
                
        setupMouseControls();
    }

    /** Registers mouse press/release handlers that compute and send force vectors. */
    private void setupMouseControls() {
        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            startX = e.getX();
            startY = e.getY();
        });

        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            try {
                if (org.golfit.Main.currentClient != null) {
                    // 1. Calculate difference
                    double diffX = startX - e.getX();
                    double diffY = startY - e.getY();

                    // 2. Convert from pixels to grid units
                    // We divide by the current width/height of the pane
                    double scaleX = root.getWidth() / 80.0;
                    double scaleY = root.getHeight() / 45.0;

                    double gridFx = diffX / scaleX;
                    double gridFy = diffY / scaleY;

                    // 3. Force multiplier for the feel (Tweak here!)
                    // A value between 1.5 and 2.5 usually feels good
                    double powerMultiplier = 2.0;
                    gridFx *= powerMultiplier;
                    gridFy *= powerMultiplier;

                    // 4. Maximum limit (so the ball doesn't glitch out of the map)
                    double maxForce = 45.0;
                    double currentForce = Math.sqrt(gridFx * gridFx + gridFy * gridFy);

                    if (currentForce > maxForce) {
                        gridFx = (gridFx / currentForce) * maxForce;
                        gridFy = (gridFy / currentForce) * maxForce;
                    }

                    // Only send if it wasn't an accidental click
                    if (currentForce > 0.5) {
                        org.golfit.Main.currentClient.clientApi.sendMovement(gridFx, gridFy);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /** Returns the root pane for insertion into the JavaFX scene graph. */
    public Pane getRoot() {
        return root;
    }
}