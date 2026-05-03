package org.golfit.client.game;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import org.golfit.client.handlers.MovementHandler;
import org.golfit.shared.constants.MapData;

/**
 * Transparent overlay pane that captures mouse drag input for aiming and shooting.
 * Converts pixel drag distances to grid-unit force vectors and sends them to the server.
 * Also drives the {@link AimIndicator} to give live visual feedback during a stroke.
 * <p>
 * Aim/fire logic is exposed via {@link #beginAim()}, {@link #updateAim(double, double)},
 * and {@link #fireShot(double, double)} so alternative input sources (e.g. gamepad) can
 * drive the same pipeline as mouse input.
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
        this.root.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            startX = e.getX();
            startY = e.getY();
            beginAim();
        });

        this.root.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            double[] f = calculateForceVector(e.getX(), e.getY());
            updateAim(f[0], f[1]);
        });

        this.root.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            double[] f = calculateForceVector(e.getX(), e.getY());
            fireShot(f[0], f[1]);
        });
    }

    /** Begin a stroke. No-op if the player's ball is still in motion. */
    void beginAim() {
        if (!movementHandler.isPlayerBallStill(localPlayerId)) return;
        pressing = true;
        if (aimIndicator != null) aimIndicator.hide();
    }

    /** Update the aim indicator to reflect the current force vector. */
    void updateAim(double fx, double fy) {
        if (!pressing || aimIndicator == null) return;
        if (!movementHandler.isPlayerBallStill(localPlayerId)) {
            aimIndicator.hide();
            return;
        }
        double magnitude = Math.sqrt(fx * fx + fy * fy);
        double ratio     = Math.min(magnitude / MAX_FORCE, 1.0);
        double angle     = Math.atan2(fy, fx) + Math.PI;

        BallRender br = movementHandler.players.get(localPlayerId);
        if (br == null) return;
        aimIndicator.update(br.getCircle().getCenterX(), br.getCircle().getCenterY(),
                br.getCircle().getRadius(), angle, ratio);
    }

    /** Fire the stroke with the given force vector and end the aiming state. */
    void fireShot(double fx, double fy) {
        if (aimIndicator != null) aimIndicator.hide();
        if (!pressing || !movementHandler.isPlayerBallStill(localPlayerId)) {
            pressing = false;
            return;
        }
        pressing = false;
        try {
            if (org.golfit.Main.currentClient != null
                    && (Math.abs(fx) > 0.1 || Math.abs(fy) > 0.1)) {
                org.golfit.Main.currentClient.clientApi.sendMovement(fx, fy);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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