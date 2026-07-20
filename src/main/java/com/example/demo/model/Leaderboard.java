package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Leaderboard {

    public static final int MAX_ENTRIES = 10;

    private static Leaderboard instance;
    private Leaderboard() {}
    public static Leaderboard getInstance() {
        if (instance == null) instance = new Leaderboard();
        return instance;
    }

    private final List<LeaderboardEntry> entries = new ArrayList<>();

    public void addEntry(String playerName, long score, GameMode mode) {
        entries.add(new LeaderboardEntry(playerName, score, mode, LocalDateTime.now()));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) entries.subList(MAX_ENTRIES, entries.size()).clear();
    }

    public List<LeaderboardEntry> getEntries() { return Collections.unmodifiableList(entries); }
    public boolean isEmpty() { return entries.isEmpty(); }
}
