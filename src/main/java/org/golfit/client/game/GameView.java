package org.golfit.client.game;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import org.golfit.client.handlers.MovementHandler;
import org.golfit.shared.constants.MapData;

/**
 * Transparent overlay pane that captures mouse drag input for aiming and shooting.
 * Converts pixel drag distances to grid-unit force vectors and sends them to the server.
 * Also drives the {@link AimIndicator} to give live visual feedback during a stroke.
 */
public class GameView {
    private final Pane root;

    private double startX;
    private double startY;
    private boolean pressing = false;

    private static final double POWER_MULTIPLIER = 2.0;
    private static final double MAX_FORCE        = 45.0;

    private final MovementHandler movementHandler;
    private final int             localPlayerId;
    private       AimIndicator    aimIndicator;

    public GameView(MovementHandler movementHandler, int localPlayerId) {
        this.movementHandler = movementHandler;
        this.localPlayerId   = localPlayerId;
        this.root = new Pane();
        this.root.setBackground(Background.EMPTY);
        setupMouseControls();
    }

    public void setAimIndicator(AimIndicator indicator) {
        this.aimIndicator = indicator;
    }

    private void setupMouseControls() {
        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (!movementHandler.isPlayerBallStill(localPlayerId)) return;
            startX   = e.getX();
            startY   = e.getY();
            pressing = true;
            if (aimIndicator != null) aimIndicator.hide();
        });

        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
            if (!pressing || aimIndicator == null) return;
            if (!movementHandler.isPlayerBallStill(localPlayerId)) {
                aimIndicator.hide();
                return;
            }

            double[] force    = calculateForceVector(e.getX(), e.getY());
            double magnitude  = Math.sqrt(force[0] * force[0] + force[1] * force[1]);
            double ratio      = magnitude / MAX_FORCE;
            double angle      = Math.atan2(force[1], force[0]) + Math.PI;

            BallRender br = movementHandler.players.get(localPlayerId);
            if (br == null) return;

            aimIndicator.update(br.getCircle().getCenterX(), br.getCircle().getCenterY(),
                                br.getCircle().getRadius(), angle, ratio);
        });

        this.root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            if (aimIndicator != null) aimIndicator.hide();
            if (!pressing || !movementHandler.isPlayerBallStill(localPlayerId)) {
                pressing = false;
                return;
            }
            pressing = false;

            try {
                if (org.golfit.Main.currentClient != null) {
                    double[] force = calculateForceVector(e.getX(), e.getY());
                    if (Math.abs(force[0]) > 0.1 || Math.abs(force[1]) > 0.1) {
                        org.golfit.Main.currentClient.clientApi.sendMovement(force[0], force[1]);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private double[] calculateForceVector(double endX, double endY) {
        double scaleX = root.getWidth()  > 0 ? root.getWidth()  / MapData.COLS : 1.0;
        double scaleY = root.getHeight() > 0 ? root.getHeight() / MapData.ROWS : 1.0;

        double fx = ((startX - endX) / scaleX) * POWER_MULTIPLIER;
        double fy = ((startY - endY) / scaleY) * POWER_MULTIPLIER;

        double force = Math.sqrt(fx * fx + fy * fy);
        if (force > MAX_FORCE) {
            fx = (fx / force) * MAX_FORCE;
            fy = (fy / force) * MAX_FORCE;
        }
        return new double[]{fx, fy};
    }

    public Pane getRoot() {
        return root;
    }
}