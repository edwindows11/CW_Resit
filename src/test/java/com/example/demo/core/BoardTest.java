package com.example.demo.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    private Board board;

    @BeforeEach
    public void setUp() {
        // Create a classic 4x4 board with 2048 win condition
        board = new Board(4, 2048);
    }

    @Test
    public void testInitialise() {
        board.initialise();
        assertEquals(4, board.getSize());
        assertEquals(2048, board.getWinTile());
        assertEquals(GameState.PLAYING, board.getState());

        int[][] grid = board.getGrid();
        int nonZeroCount = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] != 0) {
                    nonZeroCount++;
                    assertTrue(grid[i][j] == 2 || grid[i][j] == 4);
                }
            }
        }
        // Initialisation should spawn exactly 2 tiles
        assertEquals(2, nonZeroCount);
    }

    @Test
    public void testCanMoveInitially() {
        board.initialise();
        assertTrue(board.canMove());
    }

    @Test
    public void testWinState() {
        // Manually place 2048 to trigger win condition
        board.initialise();
        // Set a grid containing 2048
        int[][] winGrid = {
            {1024, 1024, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        };
        board.restoreGrid(winGrid);
        assertEquals(GameState.PLAYING, board.getState());

        // Perform a move to trigger merging 1024 + 1024 = 2048
        board.moveLeft();
        assertEquals(GameState.WON, board.getState());
    }

    @Test
    public void testLoseState() {
        board.initialise();
        // A completely full grid with no adjacent merges
        int[][] fullGrid = {
            {2, 4, 2, 4},
            {4, 2, 4, 2},
            {2, 4, 2, 4},
            {4, 2, 4, 2}
        };
        board.restoreGrid(fullGrid);
        assertFalse(board.canMove());
        
        // Try to move, which updates the state to LOST
        board.moveLeft();
        assertEquals(GameState.LOST, board.getState());
    }
}
