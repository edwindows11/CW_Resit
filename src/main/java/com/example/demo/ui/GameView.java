package com.example.demo.ui;

import com.example.demo.core.BoardObserver;
import com.example.demo.core.GameState;
import com.example.demo.model.GameMode;
import com.example.demo.util.ColorMapper;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class GameView extends BorderPane implements BoardObserver {

    private static final double TILE_GAP           = 8;
    private static final double TILE_CORNER_RADIUS = 10;
    /**
     * Logical grid canvas size: header (~78) + container (580+32) + footer (~72) = ~762px,
     * comfortably inside the 820px minimum window height.
     */
    private static final double VIEW_SIZE          = 580;

    private final int gridSize;
    private final double tileSize;
    private final StackPane[][] tileNodes;
    private int[][] previousGrid;

    private final Label scoreValueLabel;
    private final Label bestValueLabel;
    private final Label timerLabel;
    private final Button undoButton;

    private GridPane gridPane;

    private Runnable onUndo;
    private Runnable onMenu;

    public GameView(GameMode mode) {
        this.gridSize     = mode.getGridSize();
        this.tileSize     = (VIEW_SIZE - (gridSize + 1) * TILE_GAP) / gridSize;
        this.tileNodes    = new StackPane[gridSize][gridSize];
        this.previousGrid = new int[gridSize][gridSize];

        this.scoreValueLabel = createValueLabel("0");
        this.bestValueLabel  = createValueLabel("0");
        this.timerLabel      = createValueLabel("1:00");
        this.undoButton      = createUndoButton();

        setStyle("-fx-background-color: #1a1a2e;");
        setTop(buildHeader(mode));
        setCenter(buildGridArea());
        setBottom(buildFooter());
        setFocusTraversable(true);
    }

    @Override
    public void onBoardChanged(int[][] grid, GameState state, long scoreDelta, int spawnRow, int spawnCol) {
        Platform.runLater(() -> {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    int newVal = grid[i][j];
                    int oldVal = previousGrid[i][j];
                    if (newVal != oldVal) {
                        updateTileVisual(i, j, newVal);
                        if (i == spawnRow && j == spawnCol)           animateSpawn(tileNodes[i][j]);
                        else if (oldVal != 0 && newVal > oldVal)      animateMerge(tileNodes[i][j]);
                    }
                }
            }
            previousGrid = grid;
        });
    }

    public void updateScoreDisplay(long score, long best) {
        Platform.runLater(() -> { scoreValueLabel.setText(String.valueOf(score)); bestValueLabel.setText(String.valueOf(best)); });
    }

    public void updateTimer(int secondsRemaining) {
        Platform.runLater(() -> {
            timerLabel.setText(String.format("%d:%02d", secondsRemaining / 60, secondsRemaining % 60));
            if (secondsRemaining <= 10) timerLabel.setTextFill(Color.web("#e94560"));
        });
    }

    public void updateUndoCount(int remaining) {
        Platform.runLater(() -> { undoButton.setText("↩ UNDO  (" + remaining + ")"); undoButton.setDisable(remaining == 0); });
    }

    public void showMultiplierEffect(boolean active) {
        Platform.runLater(() -> {
            if (active) {
                DropShadow glow = new DropShadow(20, Color.web("#ffd700")); glow.setInput(new Glow(0.8));
                gridPane.setEffect(glow);
                gridPane.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12;" +
                                  "-fx-border-color: #ffd700; -fx-border-width: 3; -fx-border-radius: 12;");
            } else {
                gridPane.setEffect(null);
                gridPane.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12;");
            }
        });
    }

    public void setOnUndo(Runnable onUndo) { this.onUndo = onUndo; }
    public void setOnMenu(Runnable onMenu) { this.onMenu = onMenu; }

    // ── Layout ────────────────────────────────────────────────────────────

    private HBox buildHeader(GameMode mode) {
        VBox scoreBox = buildStatBox("SCORE", scoreValueLabel);
        VBox bestBox  = buildStatBox("BEST",  bestValueLabel);

        Label title = new Label("2048");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#ffd700"));
        title.setEffect(new DropShadow(15, Color.web("#ffd700")));

        HBox left = new HBox(12, scoreBox, bestBox); left.setAlignment(Pos.CENTER_LEFT);
        Region spacerL = new Region(); Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS); HBox.setHgrow(spacerR, Priority.ALWAYS);

        HBox header;
        if (mode.isTimedMode()) {
            VBox timerBox = buildStatBox("TIME", timerLabel);
            header = new HBox(8, left, spacerL, title, spacerR, timerBox);
        } else {
            header = new HBox(8, left, spacerL, title, spacerR);
        }
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 10, 24));
        header.setStyle("-fx-background-color: #16213e;");
        return header;
    }

    private VBox buildStatBox(String caption, Label valueLabel) {
        Label cap = new Label(caption);
        cap.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        cap.setTextFill(Color.web("#a0a0b0"));
        VBox box = new VBox(2, cap, valueLabel); box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8, 18, 8, 18));
        box.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8;");
        return box;
    }

    private Label createValueLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lbl.setTextFill(Color.web("#eaeaea")); return lbl;
    }

    private ScrollPane buildGridArea() {
        gridPane = new GridPane();
        gridPane.setHgap(TILE_GAP); gridPane.setVgap(TILE_GAP);
        gridPane.setPadding(new Insets(TILE_GAP));
        gridPane.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12;");

        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++) {
                tileNodes[i][j] = createTileNode(0);
                gridPane.add(tileNodes[i][j], j, i);
            }

        StackPane container = new StackPane(gridPane);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color: #1a1a2e;");

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.setFocusTraversable(false);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    private StackPane createTileNode(int value) {
        Rectangle rect = new Rectangle(tileSize, tileSize);
        rect.setArcWidth(TILE_CORNER_RADIUS); rect.setArcHeight(TILE_CORNER_RADIUS);
        rect.setFill(ColorMapper.forTile(value));
        Label lbl = new Label(value == 0 ? "" : String.valueOf(value));
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSizeFor(value)));
        lbl.setTextFill(ColorMapper.textColorFor(value));
        StackPane tile = new StackPane(rect, lbl); tile.setPrefSize(tileSize, tileSize);
        return tile;
    }

    private HBox buildFooter() {
        undoButton.setOnAction(e -> { if (onUndo != null) onUndo.run(); });
        Button menuBtn = createFooterButton("☰  MENU", "#0f3460", "#16213e");
        menuBtn.setOnAction(e -> { if (onMenu != null) onMenu.run(); });
        HBox footer = new HBox(16, undoButton, menuBtn); footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 24, 18, 24));
        footer.setStyle("-fx-background-color: #16213e;"); return footer;
    }

    private Button createUndoButton() { return createFooterButton("↩  UNDO  (3)", "#533483", "#7c3aed"); }

    private Button createFooterButton(String text, String normalBg, String hoverBg) {
        String base = footerBtnStyle(normalBg); String hover = footerBtnStyle(hoverBg);
        Button btn = new Button(text); btn.setStyle(base); btn.setPrefHeight(40);
        btn.setOnMouseEntered(e -> btn.setStyle(hover)); btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private String footerBtnStyle(String bg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-font-size: 14px;" +
               "-fx-font-weight: bold; -fx-font-family: 'Segoe UI'; -fx-background-radius: 8;" +
               "-fx-padding: 8 22 8 22; -fx-cursor: hand;";
    }

    private void updateTileVisual(int row, int col, int value) {
        StackPane tile = tileNodes[row][col];
        ((Rectangle) tile.getChildren().get(0)).setFill(ColorMapper.forTile(value));
        Label lbl = (Label) tile.getChildren().get(1);
        lbl.setTextFill(ColorMapper.textColorFor(value));
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSizeFor(value)));
        lbl.setText(value == 0 ? "" : String.valueOf(value));
    }

    private void animateMerge(StackPane tile) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), tile);
        st.setFromX(1.0); st.setFromY(1.0); st.setToX(1.14); st.setToY(1.14);
        st.setAutoReverse(true); st.setCycleCount(2); st.play();
    }

    private void animateSpawn(StackPane tile) {
        tile.setScaleX(0.1); tile.setScaleY(0.1); tile.setOpacity(0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(180), tile);
        scale.setFromX(0.1); scale.setFromY(0.1); scale.setToX(1.0); scale.setToY(1.0);
        FadeTransition fade = new FadeTransition(Duration.millis(180), tile);
        fade.setFromValue(0); fade.setToValue(1);
        scale.play(); fade.play();
    }

    private double fontSizeFor(int value) {
        if (value < 100)  return tileSize * 0.45;
        if (value < 1000) return tileSize * 0.36;
        return tileSize * 0.28;
    }
}
