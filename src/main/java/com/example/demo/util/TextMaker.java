package com.example.demo.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Utility singleton for creating styled {@link Text} nodes.
 *
 * <p>Refactored from the original {@code com.example.demo.TextMaker}: the old
 * class mixed layout concerns (relocating text, querying {@code GameScene.getLENGTH()})
 * with text creation. This version is a pure factory — it creates and styles
 * {@link Text} nodes only; positioning is the caller's responsibility.</p>
 *
 * <p><b>Design pattern:</b> Singleton.</p>
 */
public class TextMaker {

    // ── Singleton ─────────────────────────────────────────────────────────

    private static TextMaker instance;

    private TextMaker() {}

    /**
     * Returns the single {@code TextMaker} instance.
     *
     * @return the global {@code TextMaker}
     */
    public static TextMaker getInstance() {
        if (instance == null) instance = new TextMaker();
        return instance;
    }

    // ── Factory methods ────────────────────────────────────────────────────

    /**
     * Creates a bold, white {@link Text} node suitable for tile labels.
     *
     * @param content  the text string to display
     * @param fontSize the desired font size in points
     * @return a styled {@link Text} node
     */
    public Text createTileText(String content, double fontSize) {
        return styledText(content, fontSize, Color.WHITE);
    }

    /**
     * Creates a bold {@link Text} node with the specified colour.
     *
     * @param content  the text string to display
     * @param fontSize the desired font size in points
     * @param color    the text fill colour
     * @return a styled {@link Text} node
     */
    public Text createHeaderText(String content, double fontSize, Color color) {
        return styledText(content, fontSize, color);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private Text styledText(String content, double fontSize, Color color) {
        Text text = new Text(content);
        text.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        text.setFill(color);
        return text;
    }
}
