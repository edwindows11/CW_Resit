package com.example.demo.util;

import javafx.scene.paint.Color;
import java.util.Map;

/**
 * Utility class that maps tile values to their dark-mode {@link Color}s.
 *
 * <p>All colours are hand-tuned for the dark {@code #1a1a2e} background so
 * that tiles remain legible and visually distinct across the full value range.</p>
 *
 * <p>This class cannot be instantiated; use the static accessor methods.</p>
 */
public final class ColorMapper {

    // ── Dark-mode colour palette ───────────────────────────────────────────

    /** Background colour of the entire application window. */
    public static final Color APP_BACKGROUND   = Color.web("#1a1a2e");

    /** Background colour of the grid panel. */
    public static final Color GRID_BACKGROUND  = Color.web("#16213e");

    /** Colour of an empty (zero-value) tile. */
    public static final Color EMPTY_TILE       = Color.web("#0f3460");

    /** Default colour for any tile value not explicitly listed. */
    private static final Color DEFAULT_HIGH    = Color.web("#cc1111");

    /**
     * Per-value tile colours.  Keys must be powers of two (2, 4, 8, …, 8192).
     */
    private static final Map<Integer, Color> TILE_COLORS = Map.ofEntries(
            Map.entry(2,    Color.web("#4a90d9")),
            Map.entry(4,    Color.web("#357abd")),
            Map.entry(8,    Color.web("#f07b3f")),
            Map.entry(16,   Color.web("#e8602c")),
            Map.entry(32,   Color.web("#d4422f")),
            Map.entry(64,   Color.web("#c93030")),
            Map.entry(128,  Color.web("#e8c030")),
            Map.entry(256,  Color.web("#e0a820")),
            Map.entry(512,  Color.web("#d89010")),
            Map.entry(1024, Color.web("#c87800")),
            Map.entry(2048, Color.web("#ffd700")),
            Map.entry(4096, Color.web("#ff6b35")),
            Map.entry(8192, Color.web("#ff3300"))
    );

    // ── Constructor (prevent instantiation) ────────────────────────────────

    private ColorMapper() {
        throw new UnsupportedOperationException("ColorMapper is a utility class.");
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns the display colour for the given tile value.
     *
     * @param value the tile value (0 returns {@link #EMPTY_TILE})
     * @return the corresponding {@link Color}
     */
    public static Color forTile(int value) {
        if (value == 0) return EMPTY_TILE;
        return TILE_COLORS.getOrDefault(value, DEFAULT_HIGH);
    }

    /**
     * Returns the font colour to use on a tile with the given value.
     * High-value gold tiles use a dark text colour for contrast.
     *
     * @param value the tile value
     * @return {@link Color#WHITE} or {@link Color#web(String)} dark colour
     */
    public static Color textColorFor(int value) {
        // Gold tiles (128+) are bright — use darker text for readability
        return value >= 128 ? Color.web("#1a1a2e") : Color.WHITE;
    }
}
