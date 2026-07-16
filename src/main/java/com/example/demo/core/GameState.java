package com.example.demo.core;

/**
 * Represents the possible states of a 2048 game session.
 *
 * <p>The game always begins in {@link #PLAYING} and transitions to either
 * {@link #WON} when the player reaches the target tile, or {@link #LOST}
 * when the board is full and no adjacent tiles can be merged.</p>
 */
public enum GameState {

    /** The game is actively being played and moves are accepted. */
    PLAYING,

    /** The player has reached (or exceeded) the target tile value. */
    WON,

    /** No valid moves remain; the board is full and no adjacent tiles match. */
    LOST
}
