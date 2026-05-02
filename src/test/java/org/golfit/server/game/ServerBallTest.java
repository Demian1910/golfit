package org.golfit.server.game;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.constants.MapData;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerBallTest {

    @Test
    void constructor_setsPosition() {
        ServerBall ball = new ServerBall(10, 20);
        assertEquals(10, ball.getPx(), 0.01);
        assertEquals(20, ball.getPy(), 0.01);
    }

    @Test
    void initialState_notMoving() {
        ServerBall ball = new ServerBall(10, 20);
        assertFalse(ball.isMoving());
    }

    @Test
    void initialState_zeroStrokes() {
        ServerBall ball = new ServerBall(10, 20);
        assertEquals(0, ball.getStrokeCount());
    }

    @Test
    void initialState_notFinished() {
        ServerBall ball = new ServerBall(10, 20);
        assertFalse(ball.isFinished());
    }

    @Test
    void stroke_setsVelocity() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(5, 3);
        assertTrue(ball.isMoving());
    }

    @Test
    void stroke_incrementsStrokeCount() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(5, 3);
        assertEquals(1, ball.getStrokeCount());
        ball.stroke(2, 1);
        assertEquals(2, ball.getStrokeCount());
    }

    @Test
    void stroke_clampsMaxSpeed() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(1000, 1000);
        ball.update(0.04, Collections.emptyList());
        double dx = ball.getPx() - 10;
        double dy = ball.getPy() - 20;
        double distPerTick = Math.sqrt(dx * dx + dy * dy);
        assertTrue(distPerTick < 10, "Ball should not teleport across the map");
    }

    @Test
    void stroke_ignoredWhenFinished() {
        ServerBall ball = new ServerBall(10, 20);
        ball.setFinished(true);
        ball.stroke(50, 50);
        assertEquals(0, ball.getStrokeCount());
        assertFalse(ball.isMoving());
    }

    @Test
    void update_ballMovesInVelocityDirection() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(20, 0);
        double startX = ball.getPx();
        ball.update(0.04, Collections.emptyList());
        assertTrue(ball.getPx() > startX);
    }

    @Test
    void update_frictionSlowsBall() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(10, 0);
        ball.update(0.04, Collections.emptyList());
        double speed1 = ball.getPx();
        ball.update(0.04, Collections.emptyList());
        double moved2 = ball.getPx() - speed1;
        ball.update(0.04, Collections.emptyList());
        double moved3 = ball.getPx() - speed1 - moved2;
        assertTrue(moved3 <= moved2, "Ball should decelerate over time");
    }

    @Test
    void update_ballStopsBelowThreshold() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(2, 0);
        for (int i = 0; i < 200; i++) {
            ball.update(0.04, Collections.emptyList());
        }
        assertFalse(ball.isMoving());
    }

    @Test
    void update_stationaryBall_doesNotMove() {
        ServerBall ball = new ServerBall(10, 20);
        ball.update(0.04, Collections.emptyList());
        assertEquals(10, ball.getPx(), 0.001);
        assertEquals(20, ball.getPy(), 0.001);
    }

    @Test
    void resetToLastPosition_restoresPreStrokePosition() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(30, 0);
        ball.update(0.04, Collections.emptyList());
        ball.resetToLastPosition();
        assertEquals(10, ball.getPx(), 0.01);
        assertEquals(20, ball.getPy(), 0.01);
        assertFalse(ball.isMoving());
    }

    @Test
    void setFinished_changesState() {
        ServerBall ball = new ServerBall(10, 20);
        ball.setFinished(true);
        assertTrue(ball.isFinished());
    }

    @Test
    void changeFriction_affectsDeceleration() {
        ServerBall ball1 = new ServerBall(10, 20);
        ServerBall ball2 = new ServerBall(10, 20);

        ball1.stroke(20, 0);
        ball2.stroke(20, 0);

        ball1.changeFriction(Constants.FRICTION_SAND);
        ball2.changeFriction(Constants.FRICTION_ICE);

        for (int i = 0; i < 10; i++) {
            ball1.update(0.04, Collections.emptyList());
            ball2.update(0.04, Collections.emptyList());
        }
        assertTrue(ball2.getPx() > ball1.getPx(),
                "Ice (less friction) should let ball travel further than sand");
    }

    @Test
    void reset_restoresDefaultState() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(30, 30);
        ball.update(0.04, Collections.emptyList());
        ball.setFinished(true);
        ball.reset();
        assertEquals(9.8, ball.getPx(), 0.01);
        assertEquals(42.4, ball.getPy(), 0.01);
        assertFalse(ball.isMoving());
        assertFalse(ball.isFinished());
        assertEquals(0, ball.getStrokeCount());
    }

    @Test
    void multipleStrokes_lastPositionUpdates() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(20, 0);
        for (int i = 0; i < 200; i++) ball.update(0.04, Collections.emptyList());
        double stoppedX = ball.getPx();

        ball.stroke(20, 0);
        ball.update(0.04, Collections.emptyList());
        ball.resetToLastPosition();
        assertEquals(stoppedX, ball.getPx(), 0.5);
    }

    @Test
    void teleportToHole_movesBallToHoleCenterAndStops() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(15, 15);
        ball.teleportToHole();
        assertEquals(Constants.HOLE_CENTER_X, ball.getPx(), 0.001);
        assertEquals(Constants.HOLE_CENTER_Y, ball.getPy(), 0.001);
        assertFalse(ball.isMoving());
    }

    @Test
    void teleport_movesBallAndKillsVelocity() {
        ServerBall ball = new ServerBall(10, 20);
        ball.stroke(20, 20);
        ball.teleport(50.0, 30.0);
        assertEquals(50.0, ball.getPx(), 0.001);
        assertEquals(30.0, ball.getPy(), 0.001);
        assertFalse(ball.isMoving());
    }

    @Test
    void setVxSetVy_makeBallMove() {
        ServerBall ball = new ServerBall(10, 20);
        assertFalse(ball.isMoving());
        ball.setVx(3);
        assertTrue(ball.isMoving());
        ball.setVx(0);
        ball.setVy(2);
        assertTrue(ball.isMoving());
    }

    @Test
    void getPendingRemovals_isEmptyByDefault() {
        ServerBall ball = new ServerBall(10, 20);
        assertTrue(ball.getPendingRemovals().isEmpty());
    }

    @Test
    void checkCollision_rectOverlap_returnsTrue() {
        ServerBall ball = new ServerBall(10, 10);
        MapData.ServerShape rect = new MapData.ServerShape(
                1, 1, -1, MapData.ServerShape.RECT, 9.5, 9.5, 1.0, 1.0);
        assertTrue(ball.checkCollision(rect));
    }

    @Test
    void checkCollision_rectFarAway_returnsFalse() {
        ServerBall ball = new ServerBall(10, 10);
        MapData.ServerShape rect = new MapData.ServerShape(
                1, 1, -1, MapData.ServerShape.RECT, 50.0, 50.0, 1.0, 1.0);
        assertFalse(ball.checkCollision(rect));
    }

    @Test
    void checkCollision_ellipseOverlap_returnsTrue() {
        ServerBall ball = new ServerBall(10, 10);
        MapData.ServerShape ellipse = new MapData.ServerShape(
                2, 1, -1, MapData.ServerShape.ELLIPSE, 9.0, 9.0, 2.0, 2.0);
        assertTrue(ball.checkCollision(ellipse));
    }

    @Test
    void checkCollision_ellipseFarAway_returnsFalse() {
        ServerBall ball = new ServerBall(10, 10);
        MapData.ServerShape ellipse = new MapData.ServerShape(
                2, 1, -1, MapData.ServerShape.ELLIPSE, 40.0, 40.0, 2.0, 2.0);
        assertFalse(ball.checkCollision(ellipse));
    }

    @Test
    void checkCollision_polygonEdgeWithinRadius_returnsTrue() {
        ServerBall ball = new ServerBall(10, 10);
        // Polygon edge at y=9.8 — ball center (10,10) is 0.2 from that edge,
        // well inside the ball's 0.4 radius.
        double[] pts = {9.5, 9.8, 10.5, 9.8, 10.5, 10.5, 9.5, 10.5};
        MapData.ServerShape polygon = new MapData.ServerShape(
                3, 1, -1, MapData.ServerShape.POLYGON, pts);
        assertTrue(ball.checkCollision(polygon));
    }

    @Test
    void checkCollision_polygonFarAway_returnsFalse() {
        ServerBall ball = new ServerBall(10, 10);
        double[] pts = {40.0, 40.0, 41.0, 40.0, 41.0, 41.0, 40.0, 41.0};
        MapData.ServerShape polygon = new MapData.ServerShape(
                3, 1, -1, MapData.ServerShape.POLYGON, pts);
        assertFalse(ball.checkCollision(polygon));
    }
}
