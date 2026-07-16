package com.example.demo.model;

/**
 * Enumerates every playable game mode offered by the 2048 application.
 *
 * <p>Each constant carries the display-ready name, grid dimensions, and the tile
 * value that constitutes a win for that mode.</p>
 */
public enum GameMode {

    /** 3 × 3 grid — win by reaching tile 512. Suitable for beginners. */
    CLASSIC_3X3("Easy  3×3", 3, 512),

    /** 4 × 4 grid — win by reaching tile 2048. The classic experience. */
    CLASSIC_4X4("Classic  4×4", 4, 2048),

    /** 5 × 5 grid — win by reaching tile 4096. For expert players. */
    CLASSIC_5X5("Hard  5×5", 5, 4096),

    /**
     * 4 × 4 grid with a 60-second countdown — win by reaching 2048 before time expires.
     * Every 15 seconds a ×2 score-multiplier activates for 5 seconds.
     */
    TIME_CHALLENGE("Time Challenge  ⏱", 4, 2048);

    // ── Fields ─────────────────────────────────────────────────────────────

    private final String displayName;
    private final int gridSize;
    private final int winTile;

    // ── Constructor ────────────────────────────────────────────────────────

    GameMode(String displayName, int gridSize, int winTile) {
        this.displayName = displayName;
        this.gridSize    = gridSize;
        this.winTile     = winTile;
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    /**
     * Returns the human-readable name shown in the main menu.
     *
     * @return display name string
     */
    public String getDisplayName() { return displayName; }

    /**
     * Returns the number of rows and columns for this mode's board.
     *
     * @return grid dimension (e.g. 4 for a 4 × 4 board)
     */
    public int getGridSize() { return gridSize; }

    /**
     * Returns the tile value that triggers the win condition.
     *
     * @return win tile value
     */
    public int getWinTile() { return winTile; }

    /**
     * Returns {@code true} if this mode has an active countdown timer.
     *
     * @return {@code true} for {@link #TIME_CHALLENGE} only
     */
    public boolean isTimedMode() { return this == TIME_CHALLENGE; }
}
