package com.example.demo.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UndoManagerTest {

    private UndoManager undoManager;

    @BeforeEach
    public void setUp() {
        undoManager = new UndoManager();
    }

    @Test
    public void testInitiallyCannotUndo() {
        assertFalse(undoManager.canUndo());
        assertEquals(3, undoManager.getUndosRemaining());
    }

    @Test
    public void testSaveAndUndo() {
        int[][] grid = {
            {2, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
        };
        undoManager.saveState(grid, 10);
        assertTrue(undoManager.canUndo());
        assertEquals(3, undoManager.getUndosRemaining());

        UndoManager.UndoState state = undoManager.undo();
        assertNotNull(state);
        assertEquals(10, state.score());
        assertArrayEquals(grid[0], state.grid()[0]);
        assertEquals(2, undoManager.getUndosRemaining());
        assertFalse(undoManager.canUndo()); // Can only undo once since we only saved one state
    }

    @Test
    public void testMaxUndosLimit() {
        int[][] grid = new int[4][4];
        
        // Save 4 states (limit is 3)
        undoManager.saveState(grid, 10);
        undoManager.saveState(grid, 20);
        undoManager.saveState(grid, 30);
        undoManager.saveState(grid, 40);

        // First undo should pop the latest state (40)
        UndoManager.UndoState state = undoManager.undo();
        assertNotNull(state);
        assertEquals(40, state.score());
        assertEquals(2, undoManager.getUndosRemaining());

        // Second undo pops 30
        state = undoManager.undo();
        assertNotNull(state);
        assertEquals(30, state.score());
        assertEquals(1, undoManager.getUndosRemaining());

        // Third undo pops 20
        state = undoManager.undo();
        assertNotNull(state);
        assertEquals(20, state.score());
        assertEquals(0, undoManager.getUndosRemaining());

        // Fourth undo is blocked because we consumed all 3 undo tokens
        assertFalse(undoManager.canUndo());
        assertNull(undoManager.undo());
    }

    @Test
    public void testReset() {
        int[][] grid = new int[4][4];
        undoManager.saveState(grid, 10);
        undoManager.undo();
        
        // Used 1 undo, 2 left. Reset should restore to 3 undos and clear history.
        undoManager.reset();
        assertEquals(3, undoManager.getUndosRemaining());
        assertFalse(undoManager.canUndo());
    }
}
