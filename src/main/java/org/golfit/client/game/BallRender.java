package org.golfit.client.game;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.golfit.client.game.physics.Ball;
import org.golfit.shared.constants.Constants;
import java.awt.*;

public class BallRender {
    private Circle circle;
    private static final double BALL_RADIUS = Constants.BALL_RADIUS;
    public Color ballColor = Color.BLUE;

    public BallRender(Pane root, Ball ball) {
        this.circle = new Circle(BALL_RADIUS, ballColor);

        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());

        // Add circle to the pane
        root.getChildren().add(this.circle);
    }

    public void updatePosition(Ball ball) {
        this.circle.setCenterX(ball.getPx());
        this.circle.setCenterY(ball.getPy());
    }

    public Color getColor() {
        return ballColor;
    }

    public void changeColor(Color newColor) {
        ballColor = newColor;
        if (this.circle != null) {
            this.circle.setFill(newColor);
        }
    }
}