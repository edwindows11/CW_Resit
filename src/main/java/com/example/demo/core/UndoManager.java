package com.example.demo.core;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Manages a limited undo history for a game session.
 *
 * <p>Before each move the controller saves the current board state via
 * {@link #saveState(int[][], long)}. The player may then call {@link #undo()}
 * up to {@value #MAX_UNDOS} times per game to revert to a previous state.</p>
 *
 * <p>Each saved state bundles the complete grid snapshot together with the
 * score at that point, so both are restored atomically on undo.</p>
 */
public class UndoManager {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Maximum number of undo operations available per game session. */
    public static final int MAX_UNDOS = 3;

    // ── Inner type ─────────────────────────────────────────────────────────

    /**
     * Immutable snapshot used to restore a previous game state.
     *
     * @param grid  deep copy of the board grid at the time of saving
     * @param score player score at the time of saving
     */
    public record UndoState(int[][] grid, long score) {}

    // ── State ──────────────────────────────────────────────────────────────

    private final Deque<UndoState> history = new ArrayDeque<>();
    private int undosUsed = 0;

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Saves the current board and score so they can be restored via
     * {@link #undo()}. At most {@value #MAX_UNDOS} states are retained;
     * older states are discarded automatically.
     *
     * @param grid  the current grid (a deep copy is stored internally)
     * @param score the current player score
     */
    public void saveState(int[][] grid, long score) {
        history.push(new UndoState(deepCopy(grid), score));
        while (history.size() > MAX_UNDOS) history.pollLast();
    }

    /**
     * Returns {@code true} if an undo operation is available.
     * Both a saved state and a remaining undo token are required.
     *
     * @return {@code true} if {@link #undo()} may be called
     */
    public boolean canUndo() {
        return !history.isEmpty() && undosUsed < MAX_UNDOS;
    }

    /**
     * Returns the number of undo operations the player may still use.
     *
     * @return remaining undos (0 to {@value #MAX_UNDOS})
     */
    public int getUndosRemaining() {
        return MAX_UNDOS - undosUsed;
    }

    /**
     * Pops and returns the most recent saved state, consuming one undo token.
     *
     * @return the {@link UndoState} to restore, or {@code null} if not possible
     * @see #canUndo()
     */
    public UndoState undo() {
        if (!canUndo()) return null;
        undosUsed++;
        return history.pop();
    }

    /**
     * Resets this manager to its initial state (no history, no tokens used).
     * Call this at the start of each new game session.
     */
    public void reset() {
        history.clear();
        undosUsed = 0;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Creates a deep copy of the given 2-D grid.
     *
     * @param grid the grid to copy
     * @return a new 2-D array with the same values
     */
    private int[][] deepCopy(int[][] grid) {
        int[][] copy = new int[grid.length][];
        for (int i = 0; i < grid.length; i++) copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        return copy;
    }
}
