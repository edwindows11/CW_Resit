package com.example.demo.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TextMaker {

    private static TextMaker instance;
    private TextMaker() {}
    public static TextMaker getInstance() {
        if (instance == null) instance = new TextMaker();
        return instance;
    }

    public Text createTileText(String content, double fontSize) {
        return styledText(content, fontSize, Color.WHITE);
    }

    public Text createHeaderText(String content, double fontSize, Color color) {
        return styledText(content, fontSize, color);
    }

    private Text styledText(String content, double fontSize, Color color) {
        Text text = new Text(content);
        text.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        text.setFill(color);
        return text;
    }
}
