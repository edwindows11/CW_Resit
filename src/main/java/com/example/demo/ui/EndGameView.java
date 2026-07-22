package com.example.demo.ui;

import com.example.demo.model.GameMode;
import com.example.demo.model.Leaderboard;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.function.BiConsumer;

public class EndGameView extends VBox {

    public EndGameView(long score, GameMode mode, boolean won,
                       BiConsumer<String, Long> onSaveAndMenu, Runnable onPlayAgain) {
        super(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(60, 80, 60, 80));
        setStyle("-fx-background-color: #1a1a2e;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name…");
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-background-color: #16213e; -fx-text-fill: white;" +
                           "-fx-prompt-text-fill: #666; -fx-font-size: 15px;" +
                           "-fx-background-radius: 8; -fx-border-color: #0f3460;" +
                           "-fx-border-radius: 8; -fx-border-width: 1;");

        Button saveBtn  = styledButton("💾  Save & Main Menu", "#0f3460", "#e94560");
        Button againBtn = styledButton("▶  Play Again",       "#533483", "#7c3aed");

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "Anonymous";
            onSaveAndMenu.accept(name, score);
        });
        againBtn.setOnAction(e -> onPlayAgain.run());

        setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), this);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        getChildren().addAll(buildHeadline(won), buildScoreText(score), buildModeText(mode),
                spacer(16), smallText("Save your score to the leaderboard:"), nameField,
                spacer(8), saveBtn, againBtn);
    }

    private Text buildHeadline(boolean won) {
        Text t = new Text(won ? "You Win! 🎉" : "Game Over");
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 72));
        Color from = won ? Color.web("#ffd700") : Color.web("#e94560");
        Color to   = won ? Color.web("#ff6b35") : Color.web("#c93030");
        t.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, from), new Stop(1, to)));
        DropShadow glow = new DropShadow(25, from); glow.setInput(new Glow(0.5)); t.setEffect(glow);
        return t;
    }

    private Text buildScoreText(long score) {
        Text t = new Text(String.valueOf(score));
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 56));
        t.setFill(Color.web("#eaeaea")); return t;
    }

    private Text buildModeText(GameMode mode) {
        Text t = new Text(mode.getDisplayName());
        t.setFont(Font.font("Segoe UI", 16)); t.setFill(Color.web("#a0a0b0")); return t;
    }

    private Text smallText(String s) {
        Text t = new Text(s); t.setFont(Font.font("Segoe UI", 14)); t.setFill(Color.web("#a0a0b0")); return t;
    }

    private Button styledButton(String text, String normal, String hover) {
        String base = btnStyle(normal); String hov = btnStyle(hover);
        Button btn = new Button(text); btn.setStyle(base);
        btn.setPrefWidth(280); btn.setPrefHeight(46);
        btn.setOnMouseEntered(e -> btn.setStyle(hov));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private String btnStyle(String bg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-font-size: 15px;" +
               "-fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-background-radius: 10; -fx-cursor: hand;";
    }

    private Region spacer(double h) { Region r = new Region(); r.setPrefHeight(h); return r; }
}
