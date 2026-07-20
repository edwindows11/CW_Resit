package com.example.demo.model;

public enum GameMode {
    CLASSIC_3X3("Easy  3×3", 3, 512),
    CLASSIC_4X4("Classic  4×4", 4, 2048),
    CLASSIC_5X5("Hard  5×5", 5, 4096),
    TIME_CHALLENGE("Time Challenge  ⏱", 4, 2048);

    private final String displayName;
    private final int gridSize;
    private final int winTile;

    GameMode(String displayName, int gridSize, int winTile) {
        this.displayName = displayName;
        this.gridSize    = gridSize;
        this.winTile     = winTile;
    }

    public String getDisplayName() { return displayName; }
    public int getGridSize()       { return gridSize; }
    public int getWinTile()        { return winTile; }
    public boolean isTimedMode()   { return this == TIME_CHALLENGE; }
}
