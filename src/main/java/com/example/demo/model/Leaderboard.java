package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory, session-scoped leaderboard that stores the top
 * {@value #MAX_ENTRIES} scores achieved during the current run.
 *
 * <p><b>Design pattern:</b> Singleton — one leaderboard per application session
 * that is shared by all game modes.</p>
 *
 * <p>Entries are kept sorted in descending score order automatically after
 * each insertion.</p>
 */
public class Leaderboard {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Maximum number of entries retained in the leaderboard. */
    public static final int MAX_ENTRIES = 10;

    // ── Singleton ─────────────────────────────────────────────────────────

    private static Leaderboard instance;

    private Leaderboard() {}

    /**
     * Returns the global {@code Leaderboard} instance.
     *
     * @return the singleton leaderboard
     */
    public static Leaderboard getInstance() {
        if (instance == null) instance = new Leaderboard();
        return instance;
    }

    // ── State ──────────────────────────────────────────────────────────────

    private final List<LeaderboardEntry> entries = new ArrayList<>();

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Adds a new score entry for the given player and mode.
     * After insertion the list is sorted and trimmed to {@value #MAX_ENTRIES}.
     *
     * @param playerName display name of the player (non-null)
     * @param score      the final score achieved
     * @param mode       the {@link GameMode} in which the score was set
     */
    public void addEntry(String playerName, long score, GameMode mode) {
        entries.add(new LeaderboardEntry(playerName, score, mode, LocalDateTime.now()));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) {
            entries.subList(MAX_ENTRIES, entries.size()).clear();
        }
    }

    /**
     * Returns an unmodifiable view of the current leaderboard entries in
     * descending score order.
     *
     * @return sorted, read-only list of {@link LeaderboardEntry} objects
     */
    public List<LeaderboardEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns {@code true} if no entries have been recorded yet.
     *
     * @return {@code true} if the leaderboard is empty
     */
    public boolean isEmpty() { return entries.isEmpty(); }
}
