package com.example.demo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Account implements Comparable<Account> {

    private static final List<Account> accounts = new ArrayList<>();

    private long score;
    private final String userName;

    public Account(String userName) { this.userName = userName; this.score = 0; }

    public void addToScore(long delta) { this.score += delta; }
    public long getScore()             { return score; }
    public String getUserName()        { return userName; }

    @Override
    public int compareTo(Account other) { return Long.compare(other.getScore(), this.score); }

    public static Optional<Account> findByName(String userName) {
        return accounts.stream().filter(a -> a.getUserName().equals(userName)).findFirst();
    }

    public static Account getOrCreate(String userName) {
        return findByName(userName).orElseGet(() -> { Account a = new Account(userName); accounts.add(a); return a; });
    }

    public static List<Account> getAllSorted() {
        List<Account> copy = new ArrayList<>(accounts);
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }
}
