package com.example.demo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a named player profile that accumulates score across game sessions.
 *
 * <p>Refactored from the original {@code com.example.demo.Account}: access modifiers
 * are now consistent, {@code getScore()} is public, and a stream-based lookup
 * replaces the manual for-loop in the original.</p>
 */
public class Account implements Comparable<Account> {

    // ── Static registry ────────────────────────────────────────────────────

    private static final List<Account> accounts = new ArrayList<>();

    // ── Instance state ─────────────────────────────────────────────────────

    private long score;
    private final String userName;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Creates a new account with the given username and a score of zero.
     *
     * @param userName the player's display name
     */
    public Account(String userName) {
        this.userName = userName;
        this.score    = 0;
    }

    // ── Score management ───────────────────────────────────────────────────

    /**
     * Adds the given amount to this account's cumulative score.
     *
     * @param delta the score to add; must be ≥ 0
     */
    public void addToScore(long delta) { this.score += delta; }

    /**
     * Returns this account's cumulative score.
     *
     * @return accumulated score
     */
    public long getScore() { return score; }

    /**
     * Returns the player's username.
     *
     * @return username string
     */
    public String getUserName() { return userName; }

    // ── Ordering ───────────────────────────────────────────────────────────

    /**
     * Compares accounts in descending score order (highest score first).
     *
     * @param other the account to compare against
     * @return negative if {@code this} has a higher score than {@code other}
     */
    @Override
    public int compareTo(Account other) {
        return Long.compare(other.getScore(), this.score);
    }

    // ── Static factory helpers ─────────────────────────────────────────────

    /**
     * Finds an existing account by username, or returns {@link Optional#empty()}
     * if no account with that name has been registered.
     *
     * @param userName the username to search for
     * @return an {@link Optional} containing the matching account, if found
     */
    public static Optional<Account> findByName(String userName) {
        return accounts.stream()
                .filter(a -> a.getUserName().equals(userName))
                .findFirst();
    }

    /**
     * Returns an existing account for the given username, or creates and
     * registers a new one if none exists.
     *
     * @param userName the username to look up or create
     * @return the existing or newly created account
     */
    public static Account getOrCreate(String userName) {
        return findByName(userName).orElseGet(() -> {
            Account a = new Account(userName);
            accounts.add(a);
            return a;
        });
    }

    /**
     * Returns an unmodifiable sorted view of all registered accounts.
     *
     * @return list of all accounts sorted by descending score
     */
    public static List<Account> getAllSorted() {
        List<Account> copy = new ArrayList<>(accounts);
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }
}
