package com.example.demo.core;

public class ScoreManager {

    private static ScoreManager instance;
    private ScoreManager() {}
    public static ScoreManager getInstance() {
        if (instance == null) instance = new ScoreManager();
        return instance;
    }

    private long currentScore;
    private long bestScore;

    public void addScore(long delta) {
        currentScore += delta;
        if (currentScore > bestScore) bestScore = currentScore;
    }

    public void reset()              { currentScore = 0; }
    public void setScore(long score) { currentScore = score; }
    public long getCurrentScore()    { return currentScore; }
    public long getBestScore()       { return bestScore; }
}
