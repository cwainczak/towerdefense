package com.wsu.towerdefense;

import android.graphics.PointF;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UtilTest extends TestCase {

    @Test
    public void getNewVelocityMovingX() {
        PointF l  = new PointF(0, 0);
        int speed = 500;
        PointF t = new PointF(2000, 0);

        PointF expected =new PointF(speed, 0);
        PointF actual = Util.velocityTowardsPoint(l, t, speed);

        assertEquals(expected, actual);
    }

    @Test
    public void getNewVelocityMovingY() {
        PointF l  = new PointF(0, 0);
        int speed = 500;
        PointF t = new PointF(0, 2000);

        PointF expected =new PointF(0, speed);
        PointF actual = Util.velocityTowardsPoint(l, t, speed);

        assertEquals(expected, actual);
    }

    @Test
    public void getNewLocXVelocity() {
        PointF l  = new PointF(0, 0);
        PointF t = new PointF(2000, 0);
        int speed = 500;
        PointF vel = Util.velocityTowardsPoint(l, t, speed);

        PointF expected = new PointF(500, 0);
        PointF actual = Util.getNewLoc(l, vel.x, vel.y, 1);

        assertEquals(expected, actual);
    }

    @Test
    public void getNewLocYVelocity() {
        PointF l  = new PointF(0, 0);
        PointF t = new PointF(0, 2000);
        int speed = 500;
        PointF vel = Util.velocityTowardsPoint(l, t, speed);

        PointF expected = new PointF(0, 500);
        PointF actual = Util.getNewLoc(l, vel.x, vel.y, 1);

        assertEquals(expected, actual);
    }

    @Test
    public void getAngleBetweenPointsRight() {
        PointF l  = new PointF(0, 0);
        PointF t = new PointF(2000, 0);

        double expected = 0;
        double actual  = Util.getAngleBetweenPoints(l, t);

        assertEquals(expected, actual);
    }

    @Test
    public void getAngleBetweenPointsUp() {
        PointF l  = new PointF(0, 0);
        PointF t = new PointF(0, 2000);

        double expected = 90;
        double actual  = Util.getAngleBetweenPoints(l, t);

        assertEquals(expected, actual);
    }
}