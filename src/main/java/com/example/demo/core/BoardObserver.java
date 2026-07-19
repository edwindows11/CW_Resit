package com.example.demo.core;

public interface BoardObserver {
    void onBoardChanged(int[][] grid, GameState state, long scoreDelta, int spawnRow, int spawnCol);
}
