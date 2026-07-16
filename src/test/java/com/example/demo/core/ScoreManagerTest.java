package com.example.demo.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreManagerTest {

    private ScoreManager scoreManager;

    @BeforeEach
    public void setUp() {
        scoreManager = ScoreManager.getInstance();
        scoreManager.reset();
    }

    @Test
    public void testInitialState() {
        assertEquals(0, scoreManager.getCurrentScore());
    }

    @Test
    public void testAddScoreAndBestScore() {
        scoreManager.addScore(100);
        assertEquals(100, scoreManager.getCurrentScore());
        assertEquals(100, scoreManager.getBestScore());

        scoreManager.addScore(50);
        assertEquals(150, scoreManager.getCurrentScore());
        assertEquals(150, scoreManager.getBestScore());
    }

    @Test
    public void testResetKeepBestScore() {
        scoreManager.addScore(200);
        scoreManager.reset();
        assertEquals(0, scoreManager.getCurrentScore());
        assertEquals(200, scoreManager.getBestScore());
    }

    @Test
    public void testSetScore() {
        scoreManager.setScore(500);
        assertEquals(500, scoreManager.getCurrentScore());
    }
}
