package org.golfit.client.game;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

import javafx.animation.AnimationTimer;
import org.golfit.client.clientLogic.ClientApi;
import org.golfit.server.ClientHandler.ClientHandler;
import org.golfit.shared.protocol.Protocol;

public class GamepadInput {
    private static final float DEADZONE = 0.15f;

    private final ControllerManager controllers = new ControllerManager();
    private final Protocol.SendFunction send;
    private final ClientApi clientApi;

    public GamepadInput(Protocol.SendFunction send, ClientApi clientApi) {
        this.send = send;
        controllers.initSDLGamepad();
        this.clientApi = clientApi;
    }

    public void start() {
        new AnimationTimer() {
            @Override public void handle(long now) {
                ControllerState state = controllers.getState(0);  // player 1
                if (!state.isConnected) return;

                float fx = applyDeadzone(state.leftStickX);
                float fy = applyDeadzone(state.leftStickY);

                if (fx != 0f || fy != 0f) {
                    try {
                        clientApi.sendMovement(fx, fy);
                    } catch (Exception e) { /* log */ }
                }
            }
        }.start();
    }

    private static float applyDeadzone(float v) {
        return Math.abs(v) < DEADZONE ? 0f : v;
    }

    public void shutdown() { controllers.quitSDLGamepad(); }
}