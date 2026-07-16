package com.example.demo.core;

/**
 * Observer interface for receiving state-change notifications from a {@link Board}.
 *
 * <p>Implement this interface and register with {@link Board#addObserver(BoardObserver)}
 * to be notified after every board change (move, tile spawn, or game-state update).</p>
 *
 * <p><b>Design Pattern:</b> This interface forms the <em>Observer</em> role in the
 * Observer pattern, with {@link Board} acting as the Subject.</p>
 */
public interface BoardObserver {

    /**
     * Invoked after the board state has changed.
     *
     * @param grid        a deep copy of the current board grid values (row-major order)
     * @param state       the {@link GameState} after this change
     * @param scoreDelta  the score gained from tile merges during the triggering move;
     *                    0 on initialisation or undo restores
     * @param spawnRow    the row index of the newly spawned tile, or {@code -1} if none
     * @param spawnCol    the column index of the newly spawned tile, or {@code -1} if none
     */
    void onBoardChanged(int[][] grid, GameState state, long scoreDelta, int spawnRow, int spawnCol);
}
