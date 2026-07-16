package com.example.demo.core;

/**
 * Singleton that tracks the player's current score and all-time best score
 * for the current application session.
 *
 * <p><b>Design pattern:</b> Singleton — only one instance exists for the
 * lifetime of the JVM session, guaranteeing a single source of truth for
 * score data regardless of how many game sessions are played.</p>
 *
 * <p><b>Bug fixed from original {@code GameScene}:</b> The original code
 * accumulated score by summing <em>all</em> tile values after every keypress,
 * causing the score to grow far beyond the correct value. This class instead
 * accumulates only the score <em>delta</em> reported by {@link Board} for
 * each merge event.</p>
 */
public class ScoreManager {

    // ── Singleton ─────────────────────────────────────────────────────────

    private static ScoreManager instance;

    private ScoreManager() {}

    /**
     * Returns the single {@code ScoreManager} instance, creating it if needed.
     *
     * @return the global {@code ScoreManager}
     */
    public static ScoreManager getInstance() {
        if (instance == null) instance = new ScoreManager();
        return instance;
    }

    // ── State ──────────────────────────────────────────────────────────────

    private long currentScore;
    private long bestScore;

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Adds the given delta to the current score, updating the best score if
     * the current score now exceeds it.
     *
     * @param delta the score to add; must be ≥ 0
     */
    public void addScore(long delta) {
        currentScore += delta;
        if (currentScore > bestScore) bestScore = currentScore;
    }

    /**
     * Resets the current score to zero, without changing the best score.
     * Call this at the start of each new game session.
     */
    public void reset() {
        currentScore = 0;
    }

    /**
     * Directly sets the current score to the given value. Used when restoring
     * an undo state.
     *
     * @param score the score value to restore
     */
    public void setScore(long score) {
        currentScore = score;
    }

    /**
     * Returns the player's score in the current game session.
     *
     * @return current score
     */
    public long getCurrentScore() { return currentScore; }

    /**
     * Returns the highest score achieved across all sessions since the
     * application started.
     *
     * @return best (high) score
     */
    public long getBestScore() { return bestScore; }
}
