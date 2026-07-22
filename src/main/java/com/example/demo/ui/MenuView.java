package com.example.demo.ui;

import com.example.demo.model.GameMode;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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

import java.util.function.Consumer;

public class MenuView extends VBox {

    public MenuView(Consumer<GameMode> onModeSelected, Runnable onLeaderboard) {
        super(24);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(60, 80, 60, 80));
        setStyle("-fx-background-color: #1a1a2e;");

        getChildren().addAll(
                buildTitle(),
                buildSubtitle(),
                spacer(20),
                buildModeButton(GameMode.CLASSIC_3X3,   "🟢", onModeSelected),
                buildModeButton(GameMode.CLASSIC_4X4,   "🔵", onModeSelected),
                buildModeButton(GameMode.CLASSIC_5X5,   "🔴", onModeSelected),
                buildModeButton(GameMode.TIME_CHALLENGE, "⏱", onModeSelected),
                spacer(12),
                buildLeaderboardButton(onLeaderboard)
        );
    }

    private Text buildTitle() {
        Text title = new Text("2048");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 96));
        title.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffd700")), new Stop(1, Color.web("#ff6b35"))));
        DropShadow glow = new DropShadow(30, Color.web("#ffd700"));
        glow.setInput(new Glow(0.6));
        title.setEffect(glow);
        return title;
    }

    private Text buildSubtitle() {
        Text sub = new Text("The classic sliding-tile puzzle");
        sub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        sub.setFill(Color.web("#a0a0b0"));
        return sub;
    }

    private Button buildModeButton(GameMode mode, String emoji, Consumer<GameMode> callback) {
        Button btn = styledButton(emoji + "  " + mode.getDisplayName(), "#0f3460", "#e94560");
        btn.setPrefWidth(360); btn.setPrefHeight(52);
        btn.setOnAction(e -> callback.accept(mode));
        btn.setOnMouseEntered(e -> { ScaleTransition st = new ScaleTransition(Duration.millis(120), btn); st.setToX(1.04); st.setToY(1.04); st.play(); });
        btn.setOnMouseExited(e  -> { ScaleTransition st = new ScaleTransition(Duration.millis(120), btn); st.setToX(1.0);  st.setToY(1.0);  st.play(); });
        return btn;
    }

    private Button buildLeaderboardButton(Runnable callback) {
        Button btn = styledButton("🏆  Leaderboard", "#16213e", "#ffd700");
        btn.setPrefWidth(360); btn.setPrefHeight(44);
        btn.setOnAction(e -> callback.run());
        return btn;
    }

    private Button styledButton(String text, String normalBg, String hoverBg) {
        String base  = btnStyle(normalBg);
        String hover = btnStyle(hoverBg);
        Button btn   = new Button(text);
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        btn.setEffect(new DropShadow(8, Color.web("#000000aa")));
        return btn;
    }

    private String btnStyle(String bg) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;" +
               "-fx-font-family: 'Segoe UI'; -fx-background-radius: 10; -fx-cursor: hand;";
    }

    private Region spacer(double h) { Region r = new Region(); r.setPrefHeight(h); return r; }
}
