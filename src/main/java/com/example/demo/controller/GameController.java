package com.example.demo.controller;

import com.example.demo.core.*;
import com.example.demo.model.GameMode;
import com.example.demo.ui.GameView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameController implements BoardObserver {

    private static final int TIME_LIMIT_SECONDS  = 60;
    private static final int MULTIPLIER_INTERVAL = 15;
    private static final int MULTIPLIER_DURATION = 5;

    private final Board          board;
    private final GameView       gameView;
    private final ScoreManager   scoreManager;
    private final UndoManager    undoManager;
    private final MenuController menuController;
    private final GameMode       mode;
    private final Scene          gameScene;

    private Timeline countdownTimer;
    private Timeline multiplierScheduler;
    private Timeline multiplierTimer;
    private int      timeRemaining;
    private boolean  multiplierActive = false;
    private boolean  gameActive       = false;

    public GameController(GameMode mode, MenuController menuController) {
        this.mode           = mode;
        this.menuController = menuController;
        this.board          = new Board(mode.getGridSize(), mode.getWinTile());
        this.gameView       = new GameView(mode);
        this.scoreManager   = ScoreManager.getInstance();
        this.undoManager    = new UndoManager();

        gameView.setOnUndo(this::handleUndo);
        gameView.setOnMenu(menuController::showMenu);

        board.addObserver(gameView);
        board.addObserver(this);

        this.gameScene = new Scene(gameView, 820, 820);
    }

    public Scene createScene() { return gameScene; }

    public void start() {
        scoreManager.reset();
        undoManager.reset();
        gameActive = true;
        board.initialise();

        // addEventFilter fires BEFORE any focused node sees the event,
        // so arrow keys are never swallowed by the UNDO/MENU buttons.
        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        Platform.runLater(gameView::requestFocus);

        if (mode.isTimedMode()) { startCountdown(); startMultiplierScheduler(); }
    }

    public void stop() { gameActive = false; stopTimers(); }

    @Override
    public void onBoardChanged(int[][] grid, GameState state, long scoreDelta, int spawnRow, int spawnCol) {
        long effective = multiplierActive ? scoreDelta * 2 : scoreDelta;
        scoreManager.addScore(effective);
        gameView.updateScoreDisplay(scoreManager.getCurrentScore(), scoreManager.getBestScore());
        if (state == GameState.WON || state == GameState.LOST) Platform.runLater(this::endGame);
    }

    private void handleKeyPress(KeyEvent event) {
        if (!gameActive) return;
        KeyCode code = event.getCode();

        if (code == KeyCode.F11) {
            Stage stage = (Stage) gameScene.getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
            event.consume(); return;
        }

        if (code == KeyCode.Z && event.isControlDown()) {
            Platform.runLater(this::handleUndo);
            event.consume(); return;
        }

        if (code != KeyCode.LEFT && code != KeyCode.RIGHT &&
            code != KeyCode.UP   && code != KeyCode.DOWN) return;

        event.consume();
        Platform.runLater(() -> {
            undoManager.saveState(board.getGrid(), scoreManager.getCurrentScore());
            switch (code) {
                case LEFT  -> board.moveLeft();
                case RIGHT -> board.moveRight();
                case UP    -> board.moveUp();
                case DOWN  -> board.moveDown();
            }
        });
    }

    private void handleUndo() {
        if (!undoManager.canUndo()) return;
        UndoManager.UndoState saved = undoManager.undo();
        if (saved == null) return;
        board.restoreGrid(saved.grid());
        scoreManager.setScore(saved.score());
        gameView.updateScoreDisplay(scoreManager.getCurrentScore(), scoreManager.getBestScore());
        gameView.updateUndoCount(undoManager.getUndosRemaining());
    }

    private void startCountdown() {
        timeRemaining = TIME_LIMIT_SECONDS;
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            gameView.updateTimer(timeRemaining);
            if (timeRemaining <= 0) Platform.runLater(this::endGame);
        }));
        countdownTimer.setCycleCount(TIME_LIMIT_SECONDS);
        countdownTimer.play();
    }

    private void startMultiplierScheduler() {
        multiplierScheduler = new Timeline(new KeyFrame(Duration.seconds(MULTIPLIER_INTERVAL), e -> {
            if (!gameActive) return;
            multiplierActive = true;
            gameView.showMultiplierEffect(true);
            multiplierTimer = new Timeline(new KeyFrame(Duration.seconds(MULTIPLIER_DURATION), e2 -> {
                multiplierActive = false;
                gameView.showMultiplierEffect(false);
            }));
            multiplierTimer.play();
        }));
        multiplierScheduler.setCycleCount(Timeline.INDEFINITE);
        multiplierScheduler.play();
    }

    private void endGame() {
        if (!gameActive) return;
        gameActive = false;
        stopTimers();
        menuController.showEndGame(scoreManager.getCurrentScore(), mode, board.getState() == GameState.WON);
    }

    private void stopTimers() {
        if (countdownTimer      != null) countdownTimer.stop();
        if (multiplierScheduler != null) multiplierScheduler.stop();
        if (multiplierTimer     != null) multiplierTimer.stop();
        multiplierActive = false;
    }
}
