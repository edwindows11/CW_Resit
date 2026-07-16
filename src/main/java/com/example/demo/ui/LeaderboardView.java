package com.example.demo.ui;

import com.example.demo.model.Leaderboard;
import com.example.demo.model.LeaderboardEntry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Screen that displays the top-10 in-memory leaderboard in a styled table.
 *
 * <p>If no entries exist yet, a friendly placeholder message is shown instead.</p>
 */
public class LeaderboardView extends VBox {

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Builds the leaderboard screen.
     *
     * @param onBack callback invoked when the player returns to the main menu
     */
    public LeaderboardView(Runnable onBack) {
        super(16);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(40, 60, 40, 60));
        setStyle("-fx-background-color: #1a1a2e;");

        getChildren().addAll(
                buildTitle(),
                buildTable(),
                buildBackButton(onBack)
        );
    }

    // ── Builders ───────────────────────────────────────────────────────────

    private Text buildTitle() {
        Text t = new Text("🏆  Leaderboard");
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        t.setFill(Color.web("#ffd700"));
        DropShadow glow = new DropShadow(20, Color.web("#ffd700"));
        t.setEffect(glow);
        return t;
    }

    private VBox buildTable() {
        VBox table = new VBox(6);
        table.setPadding(new Insets(20, 0, 20, 0));

        List<LeaderboardEntry> entries = Leaderboard.getInstance().getEntries();

        if (entries.isEmpty()) {
            Text empty = new Text("No scores yet — play a game first!");
            empty.setFont(Font.font("Segoe UI", 18));
            empty.setFill(Color.web("#a0a0b0"));
            table.getChildren().add(empty);
            table.setAlignment(Pos.CENTER);
            return table;
        }

        // Header row
        table.getChildren().add(buildRow("#", "Name", "Score", "Mode", "Date", true));

        // Data rows
        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            table.getChildren().add(buildRow(
                    String.valueOf(rank++),
                    entry.playerName(),
                    String.valueOf(entry.score()),
                    entry.mode().getDisplayName(),
                    entry.formattedTimestamp(),
                    false
            ));
        }

        return table;
    }

    private HBox buildRow(String rank, String name, String score,
                          String mode, String date, boolean isHeader) {
        Color textColor = isHeader ? Color.web("#a0a0b0") : Color.web("#eaeaea");
        String bg       = isHeader ? "#16213e" : "transparent";
        FontWeight wt   = isHeader ? FontWeight.BOLD : FontWeight.NORMAL;

        Label rankLbl  = cell(rank,  50,  wt, textColor);
        Label nameLbl  = cell(name,  160, wt, textColor);
        Label scoreLbl = cell(score, 120, wt, isHeader ? textColor : Color.web("#ffd700"));
        Label modeLbl  = cell(mode,  180, wt, textColor);
        Label dateLbl  = cell(date,  140, wt, textColor);

        HBox row = new HBox(12, rankLbl, nameLbl, scoreLbl, modeLbl, dateLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setStyle("-fx-background-color: " + bg + ";" +
                     "-fx-background-radius: 6;");
        return row;
    }

    private Label cell(String text, double width, FontWeight weight, Color color) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", weight, 14));
        lbl.setTextFill(color);
        lbl.setPrefWidth(width);
        lbl.setMinWidth(width);
        return lbl;
    }

    private Button buildBackButton(Runnable onBack) {
        String base  = btnStyle("#0f3460");
        String hover = btnStyle("#e94560");
        Button btn   = new Button("← Back to Menu");
        btn.setStyle(base);
        btn.setPrefWidth(220);
        btn.setPrefHeight(44);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        btn.setOnAction(e -> onBack.run());
        return btn;
    }

    private String btnStyle(String bg) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: white;" +
               "-fx-font-size: 15px;" +
               "-fx-font-weight: bold;" +
               "-fx-font-family: 'Segoe UI';" +
               "-fx-background-radius: 10;" +
               "-fx-cursor: hand;";
    }
}
