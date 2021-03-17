package com.wsu.towerdefense;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WavesTest extends TestCase {
    @Test
    //test initial value
    public void popType1() {
        Waves w = new Waves(a1, d1, t1, 3);
        Enemy.Type expected = Enemy.Type.S1;
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);

    }

    @Test
    //test first pop
    public void popType2() {
        Waves w = new Waves(a1, d1, t1, 3);
        Enemy.Type expected = Enemy.Type.S1;
        w.next();
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    //test first pop of second set
    public void popType3() {
        Waves w = new Waves(a1, d1, t1, 3);
        Enemy.Type expected = Enemy.Type.S2;
        w.next();
        w.next();
        w.next();
        w.next();
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    //test first pop of second wave
    public void popType4() {
        Waves w = new Waves(a1, d1, t1, 3);
        Enemy.Type expected = Enemy.Type.S1;
        for(int i = 0; i < 6; i++){
            w.next();
        }
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    @Test
    //test last pop of last wave
    public void popType5() {
        Waves w = new Waves(a1, d1, t1, 3);
        Enemy.Type expected = Enemy.Type.S3;
        for(int i = 0; i < 20; i++){
            w.next();
        }
        Enemy.Type actual = w.next();
        assertEquals(expected, actual);
    }

    List<List<Integer>> a1 = Arrays.asList(
            Arrays.asList(3, 2),
            Arrays.asList(5, 4, 3),
            Arrays.asList(0, 1, 2)
    );

    List<List<Double>> d1 = Arrays.asList(
            Arrays.asList(0.5, 0.6),
            Arrays.asList(0.1, 0.2, 0.3),
            Arrays.asList(0.03, 0.0, 0.02)
    );

    List<List<Enemy.Type>> t1 = Arrays.asList(
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2),
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2, Enemy.Type.S3),
            Arrays.asList(Enemy.Type.S1, Enemy.Type.S2, Enemy.Type.S3)
    );
}