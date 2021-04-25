package com.wsu.towerdefense;

import com.wsu.towerdefense.Model.Enemy;
import com.wsu.towerdefense.Model.Waves;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class WavesTest extends TestCase {
    List<List<Integer>> a1 = Arrays.asList(
            Arrays.asList(3, 2),
            Arrays.asList(5, 4, 3),
            Arrays.asList(2, 1, 2)
    );

    List<List<Double>> d1 = Arrays.asList(
            Arrays.asList(0.5, 0.6),
            Arrays.asList(0.1, 0.2, 0.3),
            Arrays.asList(0.03, 0.07, 0.02)
    );

    List<List<Enemy.Type>> t1 = Arrays.asList(
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2),
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2, Enemy.Type.S3),
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2, Enemy.Type.S3)
    );

    @Test
    public void firstNext() {
        Waves w = new Waves(a1, d1, t1, 3);
        w.nextWave();
        Enemy.Type expected = Enemy.Type.S1;
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    public void secondNext() {
        Waves w = new Waves(a1, d1, t1, 3);
        w.nextWave();
        Enemy.Type expected = Enemy.Type.S1;
        w.next();
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    public void firstNextOfSecondSet() {
        Waves w = new Waves(a1, d1, t1, 3);
        w.nextWave();
        Enemy.Type expected = Enemy.Type.S2;
        w.next();
        w.next();
        w.next();
        w.next();
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    public void firstNextOfSecondWave() {
        Waves w = new Waves(a1, d1, t1, 3);
        w.nextWave();
        Enemy.Type expected = Enemy.Type.S1;
        for(int i = 0; i < 5; i++){
            w.next();
        }
        w.nextWave();
        w.next();
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    public void lastNextOfLastWave() {
        Waves w = new Waves(a1, d1, t1, 3);
        w.nextWave();
        Enemy.Type expected = Enemy.Type.S3;
        for(int i = 0; i < 5; i++){
            w.next();
        }
        w.nextWave();
        for(int i = 0; i < 12; i++){
            w.next();
        }
        w.nextWave();
        for(int i = 0; i < 3; i++){
            w.next();
        }
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }
}