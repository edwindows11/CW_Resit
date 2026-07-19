package com.example.demo.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Core data model and game-logic engine for a 2048 board.
 * Uses the Observer pattern (notifies {@link BoardObserver}s) and
 * Strategy pattern (compactLine shared across all four directions).
 *
 * Bug fixes from original GameScene:
 * 1. Spawn covers ALL empty cells (not just rows/cols 0..n-2)
 * 2. Score increments by merge delta only
 * 3. Win detection sets GameState.WON
 * 4. canMove() checks all four neighbours for every cell
 * 5. Loss detected even when move doesn't change the board
 */
public class Board {

    @FunctionalInterface
    private interface MoveStrategy { long execute(); }

    private record CompactResult(int[] line, long scoreDelta) {}

    private static final double SPAWN_TWO_PROBABILITY = 0.9;

    private final int size;
    private final int winTile;
    private int[][] grid;
    private GameState state;
    private final Random random;
    private final List<BoardObserver> observers = new ArrayList<>();
    private int lastSpawnRow = -1;
    private int lastSpawnCol = -1;

    public Board(int size, int winTile) {
        this.size    = size;
        this.winTile = winTile;
        this.grid    = new int[size][size];
        this.state   = GameState.PLAYING;
        this.random  = new Random();
    }

    public void addObserver(BoardObserver observer)    { observers.add(observer); }
    public void removeObserver(BoardObserver observer) { observers.remove(observer); }

    public void initialise() {
        grid  = new int[size][size];
        state = GameState.PLAYING;
        lastSpawnRow = -1;
        lastSpawnCol = -1;
        spawnTile();
        spawnTile();
        notifyObservers(0);
    }

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

    private long executeMove(MoveStrategy strategy) {
        if (state != GameState.PLAYING) return 0;
        int[][] snapshot = copyGrid(grid);
        long delta = strategy.execute();
        if (!gridsEqual(snapshot, grid)) {
            spawnTile();
            updateState();
            notifyObservers(delta);
        } else if (!canMove()) {
            state = GameState.LOST;
            notifyObservers(0);
        }
        return delta;
    }

    private CompactResult compactLine(int[] values) {
        int[] result   = new int[values.length];
        long scoreDelta = 0;
        int writePos   = 0;
        boolean justMerged = false;
        for (int val : values) {
            if (val == 0) { justMerged = false; continue; }
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

    public void spawnTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (grid[i][j] == 0) emptyCells.add(new int[]{i, j});
        if (emptyCells.isEmpty()) { lastSpawnRow = -1; lastSpawnCol = -1; return; }
        int[] pos = emptyCells.get(random.nextInt(emptyCells.size()));
        lastSpawnRow = pos[0]; lastSpawnCol = pos[1];
        grid[lastSpawnRow][lastSpawnCol] = random.nextDouble() < SPAWN_TWO_PROBABILITY ? 2 : 4;
    }

    private void updateState() {
        for (int[] row : grid)
            for (int val : row)
                if (val >= winTile) { state = GameState.WON; return; }
        state = canMove() ? GameState.PLAYING : GameState.LOST;
    }

    public boolean canMove() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == 0) return true;
                if (j + 1 < size && grid[i][j] == grid[i][j + 1]) return true;
                if (i + 1 < size && grid[i][j] == grid[i + 1][j]) return true;
            }
        return false;
    }

    public void restoreGrid(int[][] savedGrid) {
        grid  = copyGrid(savedGrid);
        state = GameState.PLAYING;
        lastSpawnRow = -1; lastSpawnCol = -1;
        notifyObservers(0);
    }

    private void notifyObservers(long scoreDelta) {
        int[][] copy = copyGrid(grid);
        for (BoardObserver o : observers)
            o.onBoardChanged(copy, state, scoreDelta, lastSpawnRow, lastSpawnCol);
    }

    public int[][] getGrid()       { return copyGrid(grid); }
    public int getSize()           { return size; }
    public int getWinTile()        { return winTile; }
    public GameState getState()    { return state; }

    private int[] reverse(int[] arr) {
        int[] rev = new int[arr.length];
        for (int i = 0; i < arr.length; i++) rev[i] = arr[arr.length - 1 - i];
        return rev;
    }

    private int[] getColumn(int col) {
        int[] column = new int[size];
        for (int i = 0; i < size; i++) column[i] = grid[i][col];
        return column;
    }

    private void setColumn(int col, int[] values) {
        for (int i = 0; i < size; i++) grid[i][col] = values[i];
    }

    private int[][] copyGrid(int[][] g) {
        int[][] copy = new int[g.length][];
        for (int i = 0; i < g.length; i++) copy[i] = Arrays.copyOf(g[i], g[i].length);
        return copy;
    }

    private boolean gridsEqual(int[][] a, int[][] b) {
        for (int i = 0; i < a.length; i++)
            if (!Arrays.equals(a[i], b[i])) return false;
        return true;
    }
}
