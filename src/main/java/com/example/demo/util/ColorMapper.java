package com.example.demo.util;

import javafx.scene.paint.Color;
import java.util.Map;

public final class ColorMapper {

    public static final Color APP_BACKGROUND  = Color.web("#1a1a2e");
    public static final Color GRID_BACKGROUND = Color.web("#16213e");
    public static final Color EMPTY_TILE      = Color.web("#0f3460");
    private static final Color DEFAULT_HIGH   = Color.web("#cc1111");

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

    private ColorMapper() {}

    public static Color forTile(int value) {
        if (value == 0) return EMPTY_TILE;
        return TILE_COLORS.getOrDefault(value, DEFAULT_HIGH);
    }

    public static Color textColorFor(int value) {
        return value >= 128 ? Color.web("#1a1a2e") : Color.WHITE;
    }
}
