package com.example.demo.core;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class UndoManager {

    public static final int MAX_UNDOS = 3;

    public record UndoState(int[][] grid, long score) {}

    private final Deque<UndoState> history = new ArrayDeque<>();
    private int undosUsed = 0;

    public void saveState(int[][] grid, long score) {
        history.push(new UndoState(deepCopy(grid), score));
        while (history.size() > MAX_UNDOS) history.pollLast();
    }

    public boolean canUndo() {
        return !history.isEmpty() && undosUsed < MAX_UNDOS;
    }

    public int getUndosRemaining() { return MAX_UNDOS - undosUsed; }

    public UndoState undo() {
        if (!canUndo()) return null;
        undosUsed++;
        return history.pop();
    }

    public void reset() {
        history.clear();
        undosUsed = 0;
    }

    private int[][] deepCopy(int[][] grid) {
        int[][] copy = new int[grid.length][];
        for (int i = 0; i < grid.length; i++) copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        return copy;
    }
}
