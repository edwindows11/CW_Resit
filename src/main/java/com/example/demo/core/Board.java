package com.example.demo.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Core data model and game-logic engine for a 2048 board.
 *
 * <p>Maintains an {@code int[][]} grid of tile values and exposes four directional
 * move operations. After each successful move a new tile is spawned and all
 * registered {@link BoardObserver}s are notified.</p>
 *
 * <p><b>Design patterns used:</b></p>
 * <ul>
 *   <li><b>Observer</b> — registered {@link BoardObserver}s are notified via
 *       {@link #addObserver(BoardObserver)} after every state change.</li>
 *   <li><b>Strategy</b> — directional moves are expressed as {@link MoveStrategy}
 *       lambdas; each delegates to the same {@link #compactLine(int[])} primitive
 *       so there is <em>zero</em> duplicated slide/merge logic.</li>
 * </ul>
 *
 * <p><b>Bugs fixed from the original {@code GameScene}:</b></p>
 * <ul>
 *   <li>Random tile spawn now correctly covers <em>every</em> empty cell,
 *       not just cells in rows 0..n-2 and columns 0..n-2.</li>
 *   <li>Score is now incremented only by merge values, not by summing all tiles.</li>
 *   <li>Adjacent-tile check now covers all four neighbours for every cell,
 *       so the game-over condition is never triggered prematurely.</li>
 *   <li>Win condition now correctly transitions to {@link GameState#WON}.</li>
 * </ul>
 */
public class Board {

    // ── Inner types ────────────────────────────────────────────────────────

    /** Functional interface for the Strategy pattern — one per move direction. */
    @FunctionalInterface
    private interface MoveStrategy { long execute(); }

    /** Holds the result of compacting a single row or column. */
    private record CompactResult(int[] line, long scoreDelta) {}

    // ── Constants ──────────────────────────────────────────────────────────

    /** Probability that a newly spawned tile has value 2 (vs 4). */
    private static final double SPAWN_TWO_PROBABILITY = 0.9;

    // ── State ──────────────────────────────────────────────────────────────

    private final int size;
    private final int winTile;
    private int[][] grid;
    private GameState state;
    private final Random random;

    // ── Observer support ───────────────────────────────────────────────────

    private final List<BoardObserver> observers = new ArrayList<>();
    private int lastSpawnRow = -1;
    private int lastSpawnCol = -1;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Creates a new empty board with the given dimensions and win condition.
     *
     * @param size    number of rows and columns (e.g. 4 for classic 2048)
     * @param winTile tile value that triggers {@link GameState#WON}
     */
    public Board(int size, int winTile) {
        this.size    = size;
        this.winTile = winTile;
        this.grid    = new int[size][size];
        this.state   = GameState.PLAYING;
        this.random  = new Random();
    }

    // ── Observer registration ──────────────────────────────────────────────

    /**
     * Registers an observer to receive board-change notifications.
     *
     * @param observer the observer to add; must not be {@code null}
     */
    public void addObserver(BoardObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes a previously registered observer.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(BoardObserver observer) {
        observers.remove(observer);
    }

    // ── Initialisation ─────────────────────────────────────────────────────

    /**
     * Resets the board to an empty grid, spawns two starter tiles, and
     * notifies all observers of the initial state.
     */
    public void initialise() {
        grid  = new int[size][size];
        state = GameState.PLAYING;
        lastSpawnRow = -1;
        lastSpawnCol = -1;
        spawnTile();
        spawnTile();
        notifyObservers(0);
    }

    // ── Move operations ────────────────────────────────────────────────────

    /**
     * Slides all tiles to the left, merging where possible.
     *
     * @return the cumulative score gained from merges, or 0 if nothing moved
     */
    public long moveLeft() {
        return executeMove(() -> {
            long total = 0;
            for (int i = 0; i < size; i++) {
                CompactResult r = compactLine(grid[i]);
                grid[i] = r.line();
                total  += r.scoreDelta();
            }
            return total;
        });
    }

    /**
     * Slides all tiles to the right, merging where possible.
     *
     * @return the cumulative score gained from merges, or 0 if nothing moved
     */
    public long moveRight() {
        return executeMove(() -> {
            long total = 0;
            for (int i = 0; i < size; i++) {
                CompactResult r = compactLine(reverse(grid[i]));
                grid[i] = reverse(r.line());
                total  += r.scoreDelta();
            }
            return total;
        });
    }

    /**
     * Slides all tiles upward, merging where possible.
     *
     * @return the cumulative score gained from merges, or 0 if nothing moved
     */
    public long moveUp() {
        return executeMove(() -> {
            long total = 0;
            for (int j = 0; j < size; j++) {
                CompactResult r = compactLine(getColumn(j));
                setColumn(j, r.line());
                total += r.scoreDelta();
            }
            return total;
        });
    }

    /**
     * Slides all tiles downward, merging where possible.
     *
     * @return the cumulative score gained from merges, or 0 if nothing moved
     */
    public long moveDown() {
        return executeMove(() -> {
            long total = 0;
            for (int j = 0; j < size; j++) {
                CompactResult r = compactLine(reverse(getColumn(j)));
                setColumn(j, reverse(r.line()));
                total += r.scoreDelta();
            }
            return total;
        });
    }

    // ── Move execution template (Strategy pattern) ─────────────────────────

    /**
     * Executes a move strategy, spawning a tile and notifying observers only
     * if the board state actually changed.
     *
     * @param strategy the directional move to apply
     * @return score delta from merges, or 0 if the board did not change
     */
    private long executeMove(MoveStrategy strategy) {
        if (state != GameState.PLAYING) return 0;

        int[][] snapshot = copyGrid(grid);
        long delta = strategy.execute();

        if (!gridsEqual(snapshot, grid)) {
            spawnTile();
            updateState();
            notifyObservers(delta);
        } else if (!canMove()) {
            // Board did not change AND no valid moves remain → game over
            state = GameState.LOST;
            notifyObservers(0);
        }
        return delta;
    }

    // ── Compact primitive (shared by all four directions) ─────────────────

    /**
     * Compacts a single line (row or column) leftward: removes zeros, then
     * merges adjacent equal tiles once per pair.
     *
     * <p>This single method provides the complete slide-and-merge logic used
     * by all four move directions via the Strategy pattern.</p>
     *
     * @param values the line values to compact (not modified)
     * @return a {@link CompactResult} containing the new line and the score delta
     */
    private CompactResult compactLine(int[] values) {
        int[] result   = new int[values.length];
        long scoreDelta = 0;
        int writePos   = 0;
        boolean justMerged = false;

        for (int val : values) {
            if (val == 0) {
                justMerged = false;
                continue;
            }
            if (!justMerged && writePos > 0 && result[writePos - 1] == val) {
                result[writePos - 1] *= 2;
                scoreDelta += result[writePos - 1];
                justMerged = true;
            } else {
                result[writePos++] = val;
                justMerged = false;
            }
        }

        return new CompactResult(result, scoreDelta);
    }

    // ── Tile spawning ──────────────────────────────────────────────────────

    /**
     * Spawns a new tile (90 % chance of 2, 10 % chance of 4) in a randomly
     * chosen empty cell. Records the spawn position for observer notification.
     *
     * <p><b>Bug fix:</b> The original implementation used a 2-D array index
     * approach that excluded the last row and last column from being selected.
     * This method uses a flat list of empty cells to guarantee uniform
     * coverage of the entire board.</p>
     */
    public void spawnTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == 0) emptyCells.add(new int[]{i, j});
            }
        }

        if (emptyCells.isEmpty()) {
            lastSpawnRow = -1;
            lastSpawnCol = -1;
            return;
        }

        int[] pos = emptyCells.get(random.nextInt(emptyCells.size()));
        lastSpawnRow = pos[0];
        lastSpawnCol = pos[1];
        grid[lastSpawnRow][lastSpawnCol] = random.nextDouble() < SPAWN_TWO_PROBABILITY ? 2 : 4;
    }

    // ── State management ───────────────────────────────────────────────────

    /**
     * Updates the internal game state: checks for a win (any tile ≥ winTile),
     * then checks for a loss (no empty cells and no mergeable neighbours).
     *
     * <p><b>Bug fix:</b> The original code never set a proper win state.
     * This method correctly transitions to {@link GameState#WON} or
     * {@link GameState#LOST} as appropriate.</p>
     */
    private void updateState() {
        for (int[] row : grid) {
            for (int val : row) {
                if (val >= winTile) {
                    state = GameState.WON;
                    return;
                }
            }
        }
        state = canMove() ? GameState.PLAYING : GameState.LOST;
    }

    /**
     * Returns {@code true} if at least one valid move exists (an empty cell,
     * or two adjacent tiles with the same value in any direction).
     *
     * <p><b>Bug fix:</b> The original {@code haveSameNumberNearly} only checked
     * the down-right neighbour and skipped the last row and column entirely.
     * This method checks all four neighbours for every cell.</p>
     *
     * @return {@code true} if a move is available
     */
    public boolean canMove() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == 0) return true;
                if (j + 1 < size && grid[i][j] == grid[i][j + 1]) return true;
                if (i + 1 < size && grid[i][j] == grid[i + 1][j]) return true;
            }
        }
        return false;
    }

    // ── Undo support ───────────────────────────────────────────────────────

    /**
     * Restores the board to a previously saved grid and resets the state to
     * {@link GameState#PLAYING}. Notifies observers of the restored state.
     *
     * @param savedGrid the grid snapshot to restore (a deep copy is made)
     */
    public void restoreGrid(int[][] savedGrid) {
        grid  = copyGrid(savedGrid);
        state = GameState.PLAYING;
        lastSpawnRow = -1;
        lastSpawnCol = -1;
        notifyObservers(0);
    }

    // ── Observer notification ──────────────────────────────────────────────

    /** Notifies all registered observers with the current board state. */
    private void notifyObservers(long scoreDelta) {
        int[][] copy = copyGrid(grid);
        for (BoardObserver observer : observers) {
            observer.onBoardChanged(copy, state, scoreDelta, lastSpawnRow, lastSpawnCol);
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    /**
     * Returns a deep copy of the current grid.
     *
     * @return a new 2-D array with the same values as the internal grid
     */
    public int[][] getGrid() { return copyGrid(grid); }

    /**
     * Returns the number of rows and columns on this board.
     *
     * @return board dimension
     */
    public int getSize() { return size; }

    /**
     * Returns the tile value that constitutes a win.
     *
     * @return win tile value
     */
    public int getWinTile() { return winTile; }

    /**
     * Returns the current game state.
     *
     * @return the current {@link GameState}
     */
    public GameState getState() { return state; }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Returns a reversed copy of the given array.
     *
     * @param arr the array to reverse
     * @return new reversed array
     */
    private int[] reverse(int[] arr) {
        int[] rev = new int[arr.length];
        for (int i = 0; i < arr.length; i++) rev[i] = arr[arr.length - 1 - i];
        return rev;
    }

    /**
     * Returns the values of the given column as a one-dimensional array.
     *
     * @param col the column index
     * @return array of column values (top to bottom)
     */
    private int[] getColumn(int col) {
        int[] column = new int[size];
        for (int i = 0; i < size; i++) column[i] = grid[i][col];
        return column;
    }

    /**
     * Writes a one-dimensional array of values into the specified column.
     *
     * @param col    the column index
     * @param values the values to write (top to bottom)
     */
    private void setColumn(int col, int[] values) {
        for (int i = 0; i < size; i++) grid[i][col] = values[i];
    }

    /**
     * Creates a deep copy of the given 2-D grid.
     *
     * @param g the grid to copy
     * @return a new 2-D array with the same values
     */
    private int[][] copyGrid(int[][] g) {
        int[][] copy = new int[g.length][];
        for (int i = 0; i < g.length; i++) copy[i] = Arrays.copyOf(g[i], g[i].length);
        return copy;
    }

    /**
     * Returns {@code true} if two grids contain identical values.
     *
     * @param a first grid
     * @param b second grid
     * @return {@code true} if all elements are equal
     */
    private boolean gridsEqual(int[][] a, int[][] b) {
        for (int i = 0; i < a.length; i++) {
            if (!Arrays.equals(a[i], b[i])) return false;
        }
        return true;
    }
}
