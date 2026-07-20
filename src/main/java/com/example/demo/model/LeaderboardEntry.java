package com.example.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record LeaderboardEntry(
        String playerName,
        long score,
        GameMode mode,
        LocalDateTime timestamp
) implements Comparable<LeaderboardEntry> {

    @Override
    public int compareTo(LeaderboardEntry other) {
        return Long.compare(other.score(), this.score());
    }

    public String formattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
    }
}
