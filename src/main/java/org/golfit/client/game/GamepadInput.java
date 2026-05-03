package org.golfit.client.game;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import javafx.animation.AnimationTimer;

public class GamepadInput {
    private static final float  DEADZONE       = 0.15f;
    private static final double MAX_FORCE      = 45.0;   // match GameView
    private static final double SEND_THRESHOLD = 0.1;    // match GameView

    private final ControllerManager controllers = new ControllerManager();
    private final GameView gameView;

    private AnimationTimer timer;
    private boolean wasAiming = false;

    public GamepadInput(GameView gameView) {
        this.gameView = gameView;
        controllers.initSDLGamepad();
    }

    public void start() {
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                ControllerState state = controllers.getState(0);
                if (!state.isConnected) {
                    if (wasAiming) { gameView.fireShot(0, 0); wasAiming = false; }
                    return;
                }

                if (!state.isConnected) {
                    if (wasAiming) { gameView.fireShot(0, 0); wasAiming = false; }
                    return;
                }

                float sx = applyDeadzone(state.leftStickX);
                float sy = applyDeadzone(-state.leftStickY); // invert: stick up = aim up

                boolean aimingNow = state.a && (sx != 0f || sy != 0f);

                // pull-back semantics: aim opposite of stick direction, like mouse drag
                double fx = -sx * MAX_FORCE;
                double fy = -sy * MAX_FORCE;

                if (aimingNow && !wasAiming) {
                    gameView.beginAim();
                    wasAiming = true;
                }
                if (aimingNow) {
                    gameView.updateAim(fx, fy);
                }
                if (!aimingNow && wasAiming) {
                    gameView.fireShot(fx, fy);  // last known vector when A was released
                    wasAiming = false;
                }
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }

    public void shutdown() {
        stop();
        controllers.quitSDLGamepad();
    }

    private static float applyDeadzone(float v) {
        return Math.abs(v) < DEADZONE ? 0f : v;
    }
}