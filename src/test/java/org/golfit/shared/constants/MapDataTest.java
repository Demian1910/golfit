package org.golfit.shared.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapDataTest {

    @Test
    void shapeConstants_haveDistinctValues() {
        assertNotEquals(MapData.ServerShape.RECT, MapData.ServerShape.ELLIPSE);
        assertNotEquals(MapData.ServerShape.RECT, MapData.ServerShape.POLYGON);
        assertNotEquals(MapData.ServerShape.ELLIPSE, MapData.ServerShape.POLYGON);
    }

    @Test
    void rectConstructor_storesAllFields() {
        MapData.ServerShape s = new MapData.ServerShape(
                7, 1, -1, MapData.ServerShape.RECT, 10.0, 20.0, 5.0, 3.0);
        assertEquals(7, s.id);
        assertEquals(1, s.terrainType);
        assertEquals(-1, s.partnerId);
        assertEquals(MapData.ServerShape.RECT, s.formType);
        assertEquals(10.0, s.x, 0.001);
        assertEquals(20.0, s.y, 0.001);
        assertEquals(5.0, s.w, 0.001);
        assertEquals(3.0, s.h, 0.001);
        assertNull(s.points);
    }

    @Test
    void polygonConstructor_storesPoints() {
        double[] pts = {0.0, 0.0, 1.0, 0.0, 1.0, 1.0};
        MapData.ServerShape s = new MapData.ServerShape(
                42, 5, 99, MapData.ServerShape.POLYGON, pts);
        assertEquals(42, s.id);
        assertEquals(5, s.terrainType);
        assertEquals(99, s.partnerId);
        assertEquals(MapData.ServerShape.POLYGON, s.formType);
        assertSame(pts, s.points);
    }

    @Test
    void gridDimensions_arePositive() {
        assertTrue(MapData.COLS > 0);
        assertTrue(MapData.ROWS > 0);
    }

    @Test
    void ballSpawn_isInsideGrid() {
        assertTrue(MapData.BALL_START_X >= 0 && MapData.BALL_START_X < MapData.COLS);
        assertTrue(MapData.BALL_START_Y >= 0 && MapData.BALL_START_Y < MapData.ROWS);
    }
}
