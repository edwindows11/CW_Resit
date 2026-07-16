package com.example.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record representing a single leaderboard entry.
 *
 * <p>Implements {@link Comparable} so that a sorted collection naturally
 * orders entries from highest to lowest score.</p>
 *
 * @param playerName the display name of the player
 * @param score      the final score achieved
 * @param mode       the {@link GameMode} in which the score was set
 * @param timestamp  the date and time at which the game ended
 */
public record LeaderboardEntry(
        String playerName,
        long score,
        GameMode mode,
        LocalDateTime timestamp
) implements Comparable<LeaderboardEntry> {

    /**
     * Compares two entries in descending score order (highest score first).
     *
     * @param other the other entry to compare against
     * @return a negative value if {@code this} has a higher score than {@code other}
     */
    @Override
    public int compareTo(LeaderboardEntry other) {
        return Long.compare(other.score(), this.score());
    }

    /**
     * Returns a formatted timestamp string suitable for display.
     *
     * @return timestamp formatted as {@code "dd MMM HH:mm"} (e.g. "16 Jul 14:30")
     */
    public String formattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
    }
}
